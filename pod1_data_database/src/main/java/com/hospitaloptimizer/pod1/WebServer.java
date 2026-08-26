package com.hospitaloptimizer.pod1;

import com.hospitaloptimizer.pod2.SearchAlgorithms;
import com.hospitaloptimizer.pod2.SortAlgorithms;
import com.hospitaloptimizer.pod3.SchedulingEngine;
import com.hospitaloptimizer.pod4.DynamicProgramming;
import com.hospitaloptimizer.pod4.IndexEngine;
import com.hospitaloptimizer.pod5.Graph;
import com.hospitaloptimizer.pod5.GraphAlgorithms;
import com.hospitaloptimizer.shared.db.Database;
import com.hospitaloptimizer.shared.model.Location;
import com.hospitaloptimizer.shared.model.Resource;
import com.hospitaloptimizer.shared.model.Road;
import com.hospitaloptimizer.shared.model.ServiceRequest;
import com.hospitaloptimizer.shared.util.Params;
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
import java.util.*;
import java.util.stream.Collectors;

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
        server.createContext("/api/sort", app::sortRequests);
        server.createContext("/api/search", app::searchRequests);
        server.createContext("/api/dispatch", app::dispatch);
        server.createContext("/api/undo", app::undoDispatch);
        server.createContext("/api/audit-log", app::auditLog);
        server.createContext("/api/benchmarks", app::benchmarks);
        server.createContext("/api/graph-traversal", app::graphTraversal);
        server.createContext("/api/mst-detail", app::mstDetail);
        server.createContext("/api/index-detail", app::indexDetail);
        server.createContext("/", app::staticFile);
        server.start();
        System.out.println("CareRoute frontend: http://127.0.0.1:" + port + "/");
    }

    // ---- Core endpoints ----

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

    // ---- Algorithm endpoints ----

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

    // ---- Pod 2: Sort endpoints ----

    private void sortRequests(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        Map<String, String> q = query(exchange.getRequestURI().getRawQuery());
        List<ServiceRequest> all = loader.loadServiceRequests();
        String algorithm = q.getOrDefault("algorithm", "quickSort");
        String direction = q.getOrDefault("direction", "descending");
        int topN;
        try { topN = Math.min(Integer.parseInt(q.getOrDefault("topN", "10")), all.size()); } catch (NumberFormatException e) { topN = Math.min(10, all.size()); }
        topN = Math.max(1, topN);

        ServiceRequest[] arr = all.toArray(new ServiceRequest[0]);
        Comparator<ServiceRequest> cmp = "ascending".equals(direction)
                ? Comparator.comparingInt(ServiceRequest::getUrgency)
                : Comparator.comparingInt(ServiceRequest::getUrgency).reversed();

        long startNs = System.nanoTime();
        switch (algorithm) {
            case "selectionSort" -> SortAlgorithms.selectionSort(arr, cmp);
            case "insertionSort" -> SortAlgorithms.insertionSort(arr, cmp);
            case "mergeSort" -> SortAlgorithms.mergeSort(arr, cmp);
            default -> SortAlgorithms.quickSort(arr, cmp);
        }
        long elapsedNs = System.nanoTime() - startNs;

        List<ServiceRequest> sorted = Arrays.asList(arr).subList(0, topN);
        sendJson(exchange, 200, "{\"algorithm\":\"" + algorithm + "\",\"direction\":\"" + direction
                + "\",\"timeNs\":" + elapsedNs + ",\"totalSorted\":" + arr.length
                + ",\"requests\":" + requestsJson(sorted) + "}");
    }

    // ---- Pod 2: Search endpoints ----

    private void searchRequests(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        Map<String, String> q = query(exchange.getRequestURI().getRawQuery());
        String targetId = q.get("requestId");
        if (blank(targetId)) { sendJson(exchange, 400, "{\"error\":\"requestId is required.\"}"); return; }

        List<ServiceRequest> all = loader.loadServiceRequests();
        ServiceRequest target = null;
        for (ServiceRequest r : all) { if (r.getRequestId().equalsIgnoreCase(targetId)) { target = r; break; } }
        if (target == null) { sendJson(exchange, 404, "{\"error\":\"Request not found.\",\"requestId\":\"" + esc(targetId.toUpperCase()) + "\"}"); return; }

        ServiceRequest[] arr = all.toArray(new ServiceRequest[0]);
        Comparator<ServiceRequest> byId = Comparator.comparing(ServiceRequest::getRequestId);
        SortAlgorithms.mergeSort(arr, byId);

        long startLinear = System.nanoTime();
        int linearIdx = SearchAlgorithms.linearSearch(all.toArray(new ServiceRequest[0]), target, byId);
        long linearNs = System.nanoTime() - startLinear;

        long startBinary = System.nanoTime();
        int[] binaryResult = SearchAlgorithms.binarySearchWithTrace(arr, target, byId);
        long binaryNs = System.nanoTime() - startBinary;

        sendJson(exchange, 200, "{\"request\":" + requestsJson(List.of(target))
                + ",\"linear\":{\"index\":" + linearIdx + ",\"timeNs\":" + linearNs + "}"
                + ",\"binary\":{\"index\":" + binaryResult[0] + ",\"comparisons\":" + binaryResult[1] + ",\"timeNs\":" + binaryNs + "}"
                + ",\"sortedArraySize\":" + arr.length + "}");
    }

    // ---- Pod 1: Dispatch and Undo endpoints ----

    private void dispatch(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "POST")) return;
        List<ServiceRequest> pending = loader.loadServiceRequests().stream()
                .filter(r -> "PENDING".equals(r.getStatus())).toList();
        List<Resource> allResources = loader.loadResources();

        Map<String, String> assignment = delivery.assignResources(pending, allResources);

        List<Map<String, String>> assignments = new ArrayList<>();
        assignment.forEach((req, res) -> {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("requestId", req);
            entry.put("resourceId", res);
            assignments.add(entry);
        });

        String json = "{\"assignments\":" + assignments.stream().map(a ->
                "{\"requestId\":\"" + a.get("requestId") + "\",\"resourceId\":\"" + a.get("resourceId") + "\"}"
        ).collect(Collectors.joining(",", "[", "]"))
                + ",\"auditLogSize\":" + delivery.auditLogSize()
                + ",\"pendingCount\":" + pending.size()
                + ",\"availableResources\":" + allResources.stream().filter(r -> "AVAILABLE".equals(r.getAvailabilityStatus())).count()
                + "}";
        sendJson(exchange, 200, json);
    }

    private void undoDispatch(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "POST")) return;
        String undone = delivery.undoLastDispatch();
        if (undone == null) {
            sendJson(exchange, 200, "{\"ok\":false,\"message\":\"Nothing to undo.\",\"auditLogSize\":" + delivery.auditLogSize() + "}");
        } else {
            sendJson(exchange, 200, "{\"ok\":true,\"undone\":\"" + esc(undone) + "\",\"auditLogSize\":" + delivery.auditLogSize() + "}");
        }
    }

    private void auditLog(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        sendJson(exchange, 200, "{\"auditLogSize\":" + delivery.auditLogSize() + "}");
    }

    // ---- Benchmarks endpoint ----

    private void benchmarks(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        int[] generalSizes = {100, 500, 1000};
        int[] smallSizes = {100, 500, 1000};
        int[] graphSizes = {50, 100};

        Benchmark bench = new Benchmark(loader, "docs/performance/results.csv");
        bench.runSearchComparison(generalSizes);
        bench.runSortingComparison(generalSizes);
        bench.runHashLoadFactorExperiment(smallSizes);
        bench.runHeapDispatchExperiment(smallSizes);
        bench.runGraphAlgorithmsExperiment(graphSizes);

        sendJson(exchange, 200, "{\"ok\":true,\"message\":\"Benchmarks completed. Results saved to algorithm_runs table and docs/performance/results.csv.\"}");
    }

    // ---- Pod 5: Graph traversal endpoint ----

    private void graphTraversal(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        Map<String, String> q = query(exchange.getRequestURI().getRawQuery());
        String start = q.getOrDefault("start", "L001");
        String algorithm = q.getOrDefault("algorithm", "bfs");
        Graph graph = delivery.buildRoadGraph(loader.loadRoads());

        List<String> order;
        long startNs = System.nanoTime();
        switch (algorithm) {
            case "dfs" -> order = GraphAlgorithms.dfs(graph, start);
            default -> order = GraphAlgorithms.bfs(graph, start);
        }
        long elapsedNs = System.nanoTime() - startNs;

        sendJson(exchange, 200, "{\"algorithm\":\"" + algorithm + "\",\"start\":\"" + esc(start)
                + "\",\"timeNs\":" + elapsedNs + ",\"visitedCount\":" + order.size()
                + ",\"order\":" + stringsJson(order) + "}");
    }

    // ---- Pod 5: MST with full edge details ----

    private void mstDetail(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        Graph graph = delivery.buildRoadGraph(loader.loadRoads());

        List<GraphAlgorithms.WeightedEdge> primEdges = GraphAlgorithms.prim(graph);
        List<GraphAlgorithms.WeightedEdge> kruskalEdges = GraphAlgorithms.kruskal(graph);

        String primEdgesJson = primEdges.stream().map(e ->
                "{\"from\":\"" + esc(e.from) + "\",\"to\":\"" + esc(e.to) + "\",\"weight\":" + String.format(java.util.Locale.US, "%.2f", e.weight) + "}"
        ).collect(Collectors.joining(",", "[", "]"));

        String kruskalEdgesJson = kruskalEdges.stream().map(e ->
                "{\"from\":\"" + esc(e.from) + "\",\"to\":\"" + esc(e.to) + "\",\"weight\":" + String.format(java.util.Locale.US, "%.2f", e.weight) + "}"
        ).collect(Collectors.joining(",", "[", "]"));

        sendJson(exchange, 200, "{\"primWeight\":" + String.format(java.util.Locale.US, "%.2f", GraphAlgorithms.totalWeight(primEdges))
                + ",\"primEdgeCount\":" + primEdges.size()
                + ",\"primEdges\":" + primEdgesJson
                + ",\"kruskalWeight\":" + String.format(java.util.Locale.US, "%.2f", GraphAlgorithms.totalWeight(kruskalEdges))
                + ",\"kruskalEdgeCount\":" + kruskalEdges.size()
                + ",\"kruskalEdges\":" + kruskalEdgesJson + "}");
    }

    // ---- Pod 4: Full index detail endpoint ----

    private void indexDetail(HttpExchange exchange) throws IOException {
        if (!requireMethod(exchange, "GET")) return;
        Map<String, String> q = query(exchange.getRequestURI().getRawQuery());
        String targetId = q.get("requestId");
        if (blank(targetId)) { sendJson(exchange, 400, "{\"error\":\"requestId is required.\"}"); return; }

        List<ServiceRequest> all = loader.loadServiceRequests();
        IndexEngine index = new IndexEngine();
        index.buildAll(all);

        ServiceRequest bstResult = index.findByBST(targetId);
        ServiceRequest rbResult = index.findByRedBlackTree(targetId);
        ServiceRequest btResult = index.findByBTree(targetId);

        String bstJson = bstResult == null ? "null" : requestsJson(List.of(bstResult)).replaceFirst("^\\[", "").replaceFirst("\\]$", "");
        String rbJson = rbResult == null ? "null" : requestsJson(List.of(rbResult)).replaceFirst("^\\[", "").replaceFirst("\\]$", "");
        String btJson = btResult == null ? "null" : requestsJson(List.of(btResult)).replaceFirst("^\\[", "").replaceFirst("\\]$", "");

        sendJson(exchange, 200, "{\"requestId\":\"" + esc(targetId.toUpperCase())
                + "\",\"bstResult\":" + bstJson
                + ",\"rbResult\":" + rbJson
                + ",\"btResult\":" + btJson
                + ",\"bstHeight\":" + index.bstHeight()
                + ",\"redBlackHeight\":" + index.redBlackHeight()
                + ",\"bTreeHeight\":" + index.bTreeHeight()
                + ",\"totalRecords\":" + all.size() + "}");
    }

    // ---- Static file serving ----

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

    // ---- Utility methods ----

    private boolean requireMethod(HttpExchange e, String method) throws IOException { if (method.equals(e.getRequestMethod())) return true; sendJson(e, 405, "{\"error\":\"Method not allowed.\"}"); return false; }
    private Map<String, String> form(HttpExchange e) throws IOException { return query(new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)); }
    private Map<String, String> query(String raw) { Map<String, String> values = new LinkedHashMap<>(); if (raw == null) return values; for (String pair : raw.split("&")) { String[] p = pair.split("=", 2); values.put(decode(p[0]), p.length > 1 ? decode(p[1]) : ""); } return values; }
    private String decode(String value) { return URLDecoder.decode(value, StandardCharsets.UTF_8); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private void sendJson(HttpExchange e, int code, String json) throws IOException { sendText(e, code, json, "application/json"); }
    private void sendText(HttpExchange e, int code, String text, String type) throws IOException { byte[] body = text.getBytes(StandardCharsets.UTF_8); e.getResponseHeaders().set("Content-Type", type + "; charset=utf-8"); e.sendResponseHeaders(code, body.length); e.getResponseBody().write(body); e.close(); }
    private String requestsJson(List<ServiceRequest> list) { return list.stream().map(r -> "{\"requestId\":\""+esc(r.getRequestId())+"\",\"source\":\""+esc(r.getSource())+"\",\"destination\":\""+esc(r.getDestination())+"\",\"category\":\""+esc(r.getCategory())+"\",\"urgency\":"+r.getUrgency()+",\"timeSubmitted\":\""+esc(r.getTimeSubmitted())+"\",\"deadline\":\""+esc(r.getDeadline())+"\",\"status\":\""+esc(r.getStatus())+"\"}").collect(Collectors.joining(",", "[", "]")); }
    private String resourcesJson(List<Resource> list) { return list.stream().map(r -> "{\"resourceId\":\""+esc(r.getResourceId())+"\",\"type\":\""+esc(r.getType())+"\",\"homeLocation\":\""+esc(r.getHomeLocation())+"\",\"capacity\":"+r.getCapacity()+",\"availabilityStatus\":\""+esc(r.getAvailabilityStatus())+"\"}").collect(Collectors.joining(",", "[", "]")); }
    private String locationsJson(List<Location> list) { return list.stream().map(l -> "{\"locationId\":\""+esc(l.getLocationId())+"\",\"name\":\""+esc(l.getName())+"\",\"area\":\""+esc(l.getArea())+"\"}").collect(Collectors.joining(",", "[", "]")); }
    private String stringsJson(List<String> list) { return list.stream().map(s -> "\"" + esc(s) + "\"").collect(Collectors.joining(",", "[", "]")); }
    private String esc(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
