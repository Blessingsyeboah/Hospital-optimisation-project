package com.hospitaloptimizer.pod4;

import com.hospitaloptimizer.shared.model.ServiceRequest;

import java.util.List;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class DynamicProgrammingTest {

    private ServiceRequest req(String id, int urgency) {
        return new ServiceRequest(id, "A", "B", "Emergency Transfer", urgency, "2026-06-01 08:00", "2026-06-01 09:00", "PENDING");
    }

    public void testSelectUnderBudget_normalCase() {
        List<ServiceRequest> requests = List.of(
                req("R1", 5), // cost 10, value 5
                req("R2", 4), // cost 15, value 4
                req("R3", 3), // cost 25, value 3
                req("R4", 1)  // cost 45, value 1
        );
        DynamicProgramming.Result result = DynamicProgramming.selectRequestsUnderBudget(requests, 25);
        // best combo within 25 minutes: R1(10)+R2(15)=25 minutes, value 5+4=9
        assertEquals(9, result.totalValue);
        assertTrue(result.totalCost <= 25, "must respect the budget");
        assertEquals(2, result.chosen.size());
    }

    public void testSelectUnderBudget_boundaryCase_zeroBudget() {
        List<ServiceRequest> requests = List.of(req("R1", 5));
        DynamicProgramming.Result result = DynamicProgramming.selectRequestsUnderBudget(requests, 0);
        assertEquals(0, result.totalValue);
        assertTrue(result.chosen.isEmpty(), "zero budget selects nothing");
    }

    public void testSelectUnderBudget_boundaryCase_emptyRequestList() {
        DynamicProgramming.Result result = DynamicProgramming.selectRequestsUnderBudget(List.of(), 100);
        assertEquals(0, result.totalValue);
        assertTrue(result.chosen.isEmpty(), "no requests means nothing selected");
    }

    public void testSelectUnderBudget_reconstructionMatchesCost() {
        List<ServiceRequest> requests = List.of(req("R1", 5), req("R2", 4), req("R3", 3));
        DynamicProgramming.Result result = DynamicProgramming.selectRequestsUnderBudget(requests, 40);
        int recomputedCost = 0;
        for (ServiceRequest r : result.chosen) recomputedCost += DynamicProgramming.estimateCostMinutes(r);
        assertEquals(result.totalCost, recomputedCost);
    }

    public void testSelectUnderBudget_usesIndexDerivedDefaultBudget() {
        List<ServiceRequest> requests = List.of(req("R1", 5), req("R2", 2));
        DynamicProgramming.Result result = DynamicProgramming.selectRequestsUnderBudget(requests);
        assertTrue(result.totalValue >= 0, "default-budget run should complete without error");
    }

    public static void main(String[] args) {
        run(DynamicProgrammingTest.class);
    }
}
