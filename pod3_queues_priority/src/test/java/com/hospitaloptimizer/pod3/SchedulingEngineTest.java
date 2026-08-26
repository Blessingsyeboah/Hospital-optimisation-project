package com.hospitaloptimizer.pod3;

import com.hospitaloptimizer.shared.model.Resource;
import com.hospitaloptimizer.shared.model.ServiceRequest;

import java.util.*;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class SchedulingEngineTest {

    private ServiceRequest req(String id, String src, String dst, int urgency) {
        return new ServiceRequest(id, src, dst, "Emergency Transfer", urgency, "2026-06-01 08:00", "2026-06-01 09:00", "PENDING");
    }

    public void testDispatchFIFO_normalCase() {
        SchedulingEngine engine = new SchedulingEngine();
        List<ServiceRequest> submitted = List.of(req("R1", "A", "B", 2), req("R2", "A", "B", 5), req("R3", "A", "B", 1));
        List<ServiceRequest> order = engine.dispatchFIFO(submitted);
        assertEquals("R1", order.get(0).getRequestId());
        assertEquals("R3", order.get(2).getRequestId());
    }

    public void testDispatchByUrgency_highestFirst() {
        SchedulingEngine engine = new SchedulingEngine();
        List<ServiceRequest> reqs = List.of(req("R1", "A", "B", 2), req("R2", "A", "B", 5), req("R3", "A", "B", 4));
        List<ServiceRequest> order = engine.dispatchByUrgency(reqs);
        assertEquals("R2", order.get(0).getRequestId()); // urgency 5
        assertEquals("R3", order.get(1).getRequestId()); // urgency 4
        assertEquals("R1", order.get(2).getRequestId()); // urgency 2
    }

    public void testDispatchByPriorityScore_usesIndexDerivedWeight() {
        SchedulingEngine engine = new SchedulingEngine();
        ServiceRequest r = req("R1", "A", "B", 3);
        double score = engine.priorityScore(r);
        assertTrue(score >= 3.0, "priority score should be at least the raw urgency (weight >= 1.0)");
    }

    public void testGreedyAssignResources_normalCase_exactMatches() {
        SchedulingEngine engine = new SchedulingEngine();
        List<ServiceRequest> reqs = List.of(req("R1", "A", "X", 5), req("R2", "B", "X", 4));
        List<Resource> resources = List.of(
                new Resource("RES1", "Ambulance", "A", 1, "AVAILABLE"),
                new Resource("RES2", "Ambulance", "B", 1, "AVAILABLE"));
        Map<String, String> assignment = engine.greedyAssignResources(reqs, resources);
        assertEquals("RES1", assignment.get("R1"));
        assertEquals("RES2", assignment.get("R2"));
    }

    public void testGreedyAssignResources_boundaryCase_noAvailableResources() {
        SchedulingEngine engine = new SchedulingEngine();
        List<ServiceRequest> reqs = List.of(req("R1", "A", "X", 5));
        List<Resource> resources = List.of(new Resource("RES1", "Ambulance", "A", 1, "BUSY"));
        Map<String, String> assignment = engine.greedyAssignResources(reqs, resources);
        assertTrue(assignment.isEmpty(), "no AVAILABLE resources means nothing gets assigned");
    }

    /**
     * GREEDY FAILURE COUNTEREXAMPLE (Section 10 requirement).
     * One resource (RES1) is home-based at location A. Two requests compete
     * for it: Req-URGENT (source=B, urgency=5, distance A->B = 5) and
     * Req-EXACT (source=A, urgency=1, distance A->A = 0).
     *
     * Greedy processes strictly by urgency, so Req-URGENT is served first and
     * -- having no exact home match -- falls back to taking the ONLY resource
     * anyway, travelling 5 units. Req-EXACT is then left completely unserved.
     * Total travel-distance cost of greedy's choice = 5 (served) + 100 (unserved penalty) = 105.
     *
     * The distance-OPTIMAL assignment instead gives RES1 to Req-EXACT
     * (distance 0) and leaves Req-URGENT unserved: cost = 0 + 100 = 100 < 105.
     *
     * This proves greedy-by-urgency is NOT distance-optimal: it is optimal
     * only for the objective it was actually designed for (serve the most
     * urgent case first), which is a deliberate, defensible trade-off -- but
     * it is a genuine counterexample against "greedy always minimises total
     * travel distance".
     */
    public void testGreedyAssignment_counterexampleIsSuboptimalForDistance() {
        SchedulingEngine engine = new SchedulingEngine();
        Map<String, Double> distanceAB = Map.of("A-A", 0.0, "A-B", 5.0, "B-A", 5.0, "B-B", 0.0);
        double unservedPenalty = 100.0;

        List<ServiceRequest> reqs = List.of(
                req("Req-URGENT", "B", "X", 5),
                req("Req-EXACT", "A", "X", 1));
        List<Resource> resources = List.of(new Resource("RES1", "Ambulance", "A", 1, "AVAILABLE"));

        Map<String, String> greedyAssignment = engine.greedyAssignResources(reqs, resources);
        assertEquals("RES1", greedyAssignment.get("Req-URGENT")); // greedy takes it for the urgent case
        assertNull(greedyAssignment.get("Req-EXACT"));            // exact-match request starves

        double greedyCost = distanceAB.get("A-B") + unservedPenalty; // 5 + 100 = 105

        // Optimal-for-distance alternative computed by exhaustive check over the
        // only two possible single-resource assignments:
        double optionA_cost = distanceAB.get("A-B") + unservedPenalty; // give RES1 to Req-URGENT
        double optionB_cost = distanceAB.get("A-A") + unservedPenalty; // give RES1 to Req-EXACT
        double optimalCost = Math.min(optionA_cost, optionB_cost);

        assertEquals(100.0, optimalCost, 0.001);
        assertTrue(greedyCost > optimalCost, "greedy (" + greedyCost + ") should be strictly worse than optimal (" + optimalCost + ")");
    }

    public static void main(String[] args) {
        run(SchedulingEngineTest.class);
    }
}
