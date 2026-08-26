package com.hospitaloptimizer.pod1;

import com.hospitaloptimizer.pod3.CustomStack;
import com.hospitaloptimizer.pod3.SchedulingEngine;
import com.hospitaloptimizer.pod5.Graph;
import com.hospitaloptimizer.pod5.GraphAlgorithms;
import com.hospitaloptimizer.shared.model.Resource;
import com.hospitaloptimizer.shared.model.Road;
import com.hospitaloptimizer.shared.model.ServiceRequest;

import java.util.List;
import java.util.Map;

/**
 * Pod 1 -- "Delivery": the top-level service that ties the whole system
 * together for a demo run -- build the road graph, dispatch requests via
 * Pod 3's scheduler, route each assignment via Pod 5's Dijkstra, and record
 * every dispatch action on an undo/audit stack (Pod 3's CustomStack).
 */
public class DispatchDeliveryService {

    private final SchedulingEngine scheduler = new SchedulingEngine();
    private final CustomStack<String> auditLog = new CustomStack<>();

    public Graph buildRoadGraph(List<Road> roads) {
        Graph g = new Graph();
        for (Road r : roads) {
            g.addEdge(r.getFromLocationId(), r.getToLocationId(), r.effectiveWeight());
        }
        return g;
    }

    public List<ServiceRequest> planDispatchOrder(List<ServiceRequest> pending) {
        return scheduler.dispatchByPriorityScore(pending);
    }

    public Map<String, String> assignResources(List<ServiceRequest> pending, List<Resource> resources) {
        Map<String, String> assignment = scheduler.greedyAssignResources(pending, resources);
        for (Map.Entry<String, String> e : assignment.entrySet()) {
            String entry = "ASSIGN " + e.getValue() + " -> " + e.getKey();
            auditLog.push(entry);
        }
        return assignment;
    }

    public GraphAlgorithms.DijkstraResult routeFrom(Graph roadGraph, String origin) {
        return GraphAlgorithms.dijkstra(roadGraph, origin);
    }

    /** Undoes the most recent dispatch action (pops the audit stack). */
    public String undoLastDispatch() {
        if (auditLog.isEmpty()) return null;
        return auditLog.pop();
    }

    public int auditLogSize() { return auditLog.size(); }
}
