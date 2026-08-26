package com.hospitaloptimizer.pod1;

import com.hospitaloptimizer.pod5.Graph;
import com.hospitaloptimizer.pod5.GraphAlgorithms;
import com.hospitaloptimizer.pod3.SchedulingEngine;
import com.hospitaloptimizer.pod4.DynamicProgramming;
import com.hospitaloptimizer.pod4.IndexEngine;
import com.hospitaloptimizer.shared.db.Database;
import com.hospitaloptimizer.shared.model.Location;
import com.hospitaloptimizer.shared.model.Resource;
import com.hospitaloptimizer.shared.model.Road;
import com.hospitaloptimizer.shared.model.ServiceRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local-only web entry point for the operations console. Run after ./build.sh. */
public final class WebServer {
    private static final int DEFAULT_PORT = 8091;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Database db;
    private final DataLoader loader;
    private final DispatchDeliveryService delivery = new DispatchDeliveryService();

    private WebServer() {
        db = new Database("hospital_optimizer.db");
        db.initSchema("schema.sql");
        loader = new DataLoader(db);
        loader.seedDatabaseIfEmpty();
    }

    public static void main(String[] args) throws IOException {
        WebServer app = new WebServer();
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/bootstrap", app::bootstrap);
        server.createContext("/api/assign", app::assign);
        server.createContext("/api/route", app::route);
        server.createContext("/api/requests", app::createRequest);
        server.createContext("/api/algorithm", app::algorithm);
        server.createContext("/", app::staticFile);
        server.start();
        System.out.println("CareRoute frontend: http://127.0.0.1:" + port + "/");
    }

    private void bootstrap(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        String json = "{\"requests\":" + requestsJson(loader.loadServiceRequests())
                + ",\"resources\":" + resourcesJson(loader.loadResources())
                + ",\"locations\":" + locationsJson(loader.loadLocations()) + "}";
        sendJson(exchange, 200, json);
    }

    private void assign(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "POST")) return;
        Map<String, String> form = form(exchange);
        String requestId = form.get("requestId");
        String resourceId = form.get("resourceId");
        if (blank(requestId) || blank(resourceId)) { sendJson(exchange, 400, "{\"error\":\"Request and resource are required.\"}"); return; }
        try (PreparedStatement request = db.getConnection().prepareStatement("UPDATE service_requests SET status='ASSIGNED' WHERE requestId=?");
             PreparedStatement resource = db.getConnection().prepareStatement("UPDATE resources SET availabilityStatus='BUSY' WHERE resourceId=?")) {
            request.setString(1, requestId); resource.setString(1, resourceId);
            if (request.executeUpdate() != 1 || resource.executeUpdate() != 1) { sendJson(exchange, 404, "{\"error\":\"Request or resource was not found.\"}"); return; }
            loader.logAuditEvent("ASSIGN", resourceId + " assigned to " + requestId, TIME.format(LocalDateTime.now()));
            sendJson(exchange, 200, "{\"ok\":true,\"message\":\"Resource assigned and audit event recorded.\"}");
        } catch (SQLException e) { sendJson(exchange, 500, "{\"error\":\"Could not save assignment.\"}"); }
    }

    private void route(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        Map<String, String> query = query(exchange.getRequestURI().getRawQuery());
        String from = query.get("from"), to = query.get("to");
        if (blank(from) || blank(to)) { sendJson(exchange, 400, "{\"error\":\"Both locations are required.\"}"); return; }
        Graph graph = delivery.buildRoadGraph(loader.loadRoads());
        GraphAlgorithms.DijkstraResult result = delivery.routeFrom(graph, from);
        Double distance = result.distance.get(to);
        if (distance == null || distance.isInfinite()) { sendJson(exchange, 404, "{\"error\":\"No connected route was found.\"}"); return; }
        sendJson(exchange, 200, "{\"minutes\":" + String.format(java.util.Locale.US, "%.2f", distance)
                + ",\"path\":" + stringsJson(result.pathTo(to)) + "}");
    }

    private void createRequest(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "POST")) return;
        Map<String, String> form = form(exchange);
        String source = form.get("source"), destination = form.get("destination"), category = form.get("category");
        int urgency;
        try { urgency = Integer.parseInt(form.getOrDefault("urgency", "0")); } catch (NumberFormatException e) { urgency = 0; }
        if (blank(source) || blank(destination) || blank(category) || urgency < 1 || urgency > 5) { sendJson(exchange, 400, "{\"error\":\"Complete all request fields.\"}"); return; }
        String id = String.format("REQ%04d", db.count("service_requests") + 1);
        String now = TIME.format(LocalDateTime.now()), deadline = TIME.format(LocalDateTime.now().plusHours(urgency >= 4 ? 2 : 12));
        try (PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO service_requests VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, id); ps.setString(2, source); ps.setString(3, destination); ps.setString(4, category); ps.setInt(5, urgency);
            ps.setString(6, now); ps.setString(7, deadline); ps.setString(8, "PENDING"); ps.executeUpdate();
            loader.logAuditEvent("CREATE_REQUEST", id + " created from web console", now);
            sendJson(exchange, 201, "{\"ok\":true,\"requestId\":\"" + id + "\"}");
        } catch (SQLException e) { sendJson(exchange, 500, "{\"error\":\"Could not create request.\"}"); }
    }

    /** Exposes the remaining console demonstrations to the browser. */
    private void algorithm(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        Map<String, String> q = query(exchange.getRequestURI().getRawQuery());
        List<ServiceRequest> all = loader.loadServiceRequests();
        String action = q.get("action");
        if ("schedule".equals(action)) {
            List<ServiceRequest> active = all.stream().filter(r -> !"COMPLETED".equals(r.getStatus()) && !"CANCELLED".equals(r.getStatus())).toList();
            SchedulingEngine e = new SchedulingEngine(); String mode = q.getOrDefault("mode", "fifo");
            List<ServiceRequest> result = "urgency".equals(mode) ? e.dispatchByUrgency(active) : "score".equals(mode) ? e.dispatchByPriorityScore(active) : e.dispatchFIFO(active);
            sendJson(exchange, 200, "{\"requests\":" + requestsJson(result) + "}"); return;
        }
        if ("budget".equals(action)) {
            int minutes; try { minutes = Integer.parseInt(q.getOrDefault("minutes", "300")); } catch (NumberFormatException ex) { minutes = 300; }
            DynamicProgramming.Result r = DynamicProgramming.selectRequestsUnderBudget(all.stream().filter(x -> "PENDING".equals(x.getStatus())).toList(), Math.max(0, Math.min(minutes, 10000)));
            sendJson(exchange, 200, "{\"totalValue\":" + r.totalValue + ",\"totalCost\":" + r.totalCost + ",\"requests\":" + requestsJson(r.chosen) + "}"); return;
        }
        if ("index".equals(action)) {
            IndexEngine index = new IndexEngine(); index.buildAll(all); ServiceRequest found = index.findByRedBlackTree(q.get("requestId"));
            String request = found == null ? "null" : requestsJson(List.of(found)).replaceFirst("^\\[", "").replaceFirst("\\]$", "");
            sendJson(exchange, 200, "{\"request\":" + request + ",\"bstHeight\":" + index.bstHeight() + ",\"redBlackHeight\":" + index.redBlackHeight() + ",\"bTreeHeight\":" + index.bTreeHeight() + "}"); return;
        }
        if ("mst".equals(action)) {
            Graph graph = delivery.buildRoadGraph(loader.loadRoads());
            sendJson(exchange, 200, "{\"prim\":" + GraphAlgorithms.totalWeight(GraphAlgorithms.prim(graph)) + ",\"kruskal\":" + GraphAlgorithms.totalWeight(GraphAlgorithms.kruskal(graph)) + "}"); return;
        }
        sendJson(exchange, 400, "{\"error\":\"Unknown algorithm action.\"}");
    }

    private void staticFile(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        String requested = exchange.getRequestURI().getPath();
        String file = requested.equals("/") ? "index.html" : requested.substring(1);
        if (file.contains("..") || file.contains("\\")) { sendText(exchange, 403, "Forbidden", "text/plain"); return; }
        Path path = Path.of("frontend", file);
        if (!Files.isRegularFile(path)) { sendText(exchange, 404, "Not found", "text/plain"); return; }
        String type = file.endsWith(".js") ? "application/javascript" : file.endsWith(".css") ? "text/css" : "text/html";
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        byte[] body = Files.readAllBytes(path); exchange.sendResponseHeaders(200, body.length); exchange.getResponseBody().write(body); exchange.close();
    }

    private boolean requireMethod(HttpExchange e, String method) throws IOException { if (method.equals(e.getRequestMethod())) return true; sendJson(e, 405, "{\"error\":\"Method not allowed.\"}"); return false; }
    private Map<String, String> form(HttpExchange e) throws IOException { return query(new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)); }
    private Map<String, String> query(String raw) { Map<String, String> values = new LinkedHashMap<>(); if (raw == null) return values; for (String pair : raw.split("&")) { String[] p = pair.split("=", 2); values.put(decode(p[0]), p.length > 1 ? decode(p[1]) : ""); } return values; }
    private String decode(String value) { return URLDecoder.decode(value, StandardCharsets.UTF_8); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private void sendJson(HttpExchange e, int code, String json) throws IOException { sendText(e, code, json, "application/json"); }
    private void sendText(HttpExchange e, int code, String text, String type) throws IOException { byte[] body = text.getBytes(StandardCharsets.UTF_8); e.getResponseHeaders().set("Content-Type", type + "; charset=utf-8"); e.sendResponseHeaders(code, body.length); e.getResponseBody().write(body); e.close(); }
    private String requestsJson(List<ServiceRequest> list) { return list.stream().map(r -> "{\"requestId\":\""+esc(r.getRequestId())+"\",\"source\":\""+esc(r.getSource())+"\",\"destination\":\""+esc(r.getDestination())+"\",\"category\":\""+esc(r.getCategory())+"\",\"urgency\":"+r.getUrgency()+",\"timeSubmitted\":\""+esc(r.getTimeSubmitted())+"\",\"deadline\":\""+esc(r.getDeadline())+"\",\"status\":\""+esc(r.getStatus())+"\"}").collect(java.util.stream.Collectors.joining(",", "[", "]")); }
    private String resourcesJson(List<Resource> list) { return list.stream().map(r -> "{\"resourceId\":\""+esc(r.getResourceId())+"\",\"type\":\""+esc(r.getType())+"\",\"homeLocation\":\""+esc(r.getHomeLocation())+"\",\"capacity\":"+r.getCapacity()+",\"availabilityStatus\":\""+esc(r.getAvailabilityStatus())+"\"}").collect(java.util.stream.Collectors.joining(",", "[", "]")); }
    private String locationsJson(List<Location> list) { return list.stream().map(l -> "{\"locationId\":\""+esc(l.getLocationId())+"\",\"name\":\""+esc(l.getName())+"\",\"area\":\""+esc(l.getArea())+"\"}").collect(java.util.stream.Collectors.joining(",", "[", "]")); }
    private String stringsJson(List<String> list) { return list.stream().map(s -> "\"" + esc(s) + "\"").collect(java.util.stream.Collectors.joining(",", "[", "]")); }
    private String esc(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
