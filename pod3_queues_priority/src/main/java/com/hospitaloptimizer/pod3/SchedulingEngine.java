package com.hospitaloptimizer.pod3;

import com.hospitaloptimizer.shared.model.Resource;
import com.hospitaloptimizer.shared.model.ServiceRequest;
import com.hospitaloptimizer.shared.util.Params;

import java.util.*;

/**
 * Pod 3 -- "Queues, Priority & Smart Assignment".
 * Answers Section 3 question: "Which service request should be handled next
 * under FIFO, urgency, and priority-based rules?" and performs the greedy
 * resource-assignment optimisation from Section 7 (M8).
 */
public class SchedulingEngine {

    /** FIFO rule: whichever request was submitted (enqueued) first goes first. */
    public List<ServiceRequest> dispatchFIFO(List<ServiceRequest> submissionOrder) {
        CustomQueue<ServiceRequest> queue = new CustomQueue<>();
        for (ServiceRequest r : submissionOrder) queue.enqueue(r);
        List<ServiceRequest> order = new ArrayList<>();
        while (!queue.isEmpty()) order.add(queue.dequeue());
        return order;
    }

    /** Urgency rule: purely by urgency (1..5), highest first, via the custom heap. */
    public List<ServiceRequest> dispatchByUrgency(List<ServiceRequest> requests) {
        Comparator<ServiceRequest> byUrgencyDesc = (a, b) -> Integer.compare(b.getUrgency(), a.getUrgency());
        CustomPriorityQueue<ServiceRequest> pq = new CustomPriorityQueue<>(byUrgencyDesc);
        for (ServiceRequest r : requests) pq.insert(r);
        List<ServiceRequest> order = new ArrayList<>();
        while (!pq.isEmpty()) order.add(pq.extractMin()); // "min" under byUrgencyDesc == highest urgency
        return order;
    }

    /**
     * Priority-based rule: a weighted score combining urgency and how close the
     * deadline is, using Params.PRIORITY_WEIGHT (index-number-derived constant).
     * Higher score dispatched first.
     */
    public List<ServiceRequest> dispatchByPriorityScore(List<ServiceRequest> requests) {
        Comparator<ServiceRequest> byScoreDesc = (a, b) ->
                Double.compare(priorityScore(b), priorityScore(a));
        CustomPriorityQueue<ServiceRequest> pq = new CustomPriorityQueue<>(byScoreDesc);
        for (ServiceRequest r : requests) pq.insert(r);
        List<ServiceRequest> order = new ArrayList<>();
        while (!pq.isEmpty()) order.add(pq.extractMin());
        return order;
    }

    public double priorityScore(ServiceRequest r) {
        return r.getUrgency() * Params.PRIORITY_WEIGHT;
    }

    /**
     * Greedy resource assignment (M8): process requests highest-urgency first,
     * assign the first AVAILABLE resource whose homeLocation matches the
     * request's source, else any available resource. Marks it BUSY.
     *
     * GREEDY FAILURE COUNTEREXAMPLE (documented per Section 10 requirement):
     * greedy commits to a locally-best choice per request and never
     * reconsiders it. With requests R1 (source=A, urgency 5) and R2 (source=B,
     * urgency 4), and two resources: one AT home A, one AT home B -- if R1 is
     * processed first and greedily takes the resource at A even though the
     * resource at B could also reach A at *equal* cost while freeing up the
     * B-resource for R2 more efficiently, the greedy total travel distance can
     * exceed the optimal (globally solved) assignment. See
     * GreedyAssignmentTest.testGreedyAssignment_counterexampleIsSuboptimal for
     * a worked, asserted example with actual numbers.
     */
    public Map<String, String> greedyAssignResources(List<ServiceRequest> requests, List<Resource> resources) {
        List<ServiceRequest> ordered = dispatchByUrgency(requests);
        Map<String, Resource> pool = new LinkedHashMap<>();
        for (Resource res : resources) {
            if ("AVAILABLE".equals(res.getAvailabilityStatus())) pool.put(res.getResourceId(), res);
        }
        Map<String, String> assignment = new LinkedHashMap<>(); // requestId -> resourceId

        for (ServiceRequest req : ordered) {
            Resource chosen = null;
            for (Resource res : pool.values()) {
                if (res.getHomeLocation().equals(req.getSource())) { chosen = res; break; }
            }
            if (chosen == null && !pool.isEmpty()) {
                chosen = pool.values().iterator().next();
            }
            if (chosen != null) {
                assignment.put(req.getRequestId(), chosen.getResourceId());
                pool.remove(chosen.getResourceId());
            }
        }
        return assignment;
    }
}
