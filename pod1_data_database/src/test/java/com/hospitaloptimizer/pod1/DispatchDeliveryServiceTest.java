package com.hospitaloptimizer.pod1;

import com.hospitaloptimizer.pod5.Graph;
import com.hospitaloptimizer.pod5.GraphAlgorithms;
import com.hospitaloptimizer.shared.model.Resource;
import com.hospitaloptimizer.shared.model.Road;
import com.hospitaloptimizer.shared.model.ServiceRequest;

import java.util.List;
import java.util.Map;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class DispatchDeliveryServiceTest {

    public void testBuildRoadGraph_normalCase() {
        DispatchDeliveryService svc = new DispatchDeliveryService();
        List<Road> roads = List.of(new Road("R1", "A", "B", 10, 15, 1.2));
        Graph g = svc.buildRoadGraph(roads);
        assertEquals(2, g.nodeCount());
        assertTrue(g.neighbors("A").size() == 1, "A should have exactly one neighbor");
    }

    public void testAssignResources_pushesToAuditLog() {
        DispatchDeliveryService svc = new DispatchDeliveryService();
        List<ServiceRequest> reqs = List.of(
                new ServiceRequest("R1", "A", "X", "Emergency Transfer", 5, "2026-06-01 08:00", "2026-06-01 09:00", "PENDING"));
        List<Resource> resources = List.of(new Resource("RES1", "Ambulance", "A", 1, "AVAILABLE"));
        Map<String, String> assignment = svc.assignResources(reqs, resources);
        assertEquals(1, assignment.size());
        assertEquals(1, svc.auditLogSize());
    }

    public void testUndoLastDispatch_normalCase() {
        DispatchDeliveryService svc = new DispatchDeliveryService();
        List<ServiceRequest> reqs = List.of(
                new ServiceRequest("R1", "A", "X", "Emergency Transfer", 5, "2026-06-01 08:00", "2026-06-01 09:00", "PENDING"));
        List<Resource> resources = List.of(new Resource("RES1", "Ambulance", "A", 1, "AVAILABLE"));
        svc.assignResources(reqs, resources);
        String undone = svc.undoLastDispatch();
        assertNotNull(undone);
        assertEquals(0, svc.auditLogSize());
    }

    public void testUndoLastDispatch_boundaryCase_emptyLogReturnsNull() {
        DispatchDeliveryService svc = new DispatchDeliveryService();
        assertNull(svc.undoLastDispatch());
    }

    public void testRouteFrom_matchesGraphAlgorithmsDijkstra() {
        DispatchDeliveryService svc = new DispatchDeliveryService();
        List<Road> roads = List.of(new Road("R1", "A", "B", 10, 15, 1.0));
        Graph g = svc.buildRoadGraph(roads);
        GraphAlgorithms.DijkstraResult result = svc.routeFrom(g, "A");
        assertTrue(result.distance.get("B") > 0, "distance A->B should be positive");
    }

    public static void main(String[] args) {
        run(DispatchDeliveryServiceTest.class);
    }
}
