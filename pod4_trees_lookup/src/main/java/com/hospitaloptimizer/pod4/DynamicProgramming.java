package com.hospitaloptimizer.pod4;

import com.hospitaloptimizer.shared.model.ServiceRequest;
import com.hospitaloptimizer.shared.util.Params;

import java.util.ArrayList;
import java.util.List;

/**
 * Pod 4 -- "Planning Ahead": 0/1 knapsack-style dynamic programme.
 * Selects the subset of pending service requests that maximises total
 * urgency "value" without exceeding a limited response-time BUDGET
 * (Params.BUDGET_MINUTES, index-number derived) -- answers Section 3's
 * "Which subset of requests or resources can be selected under a budget,
 * capacity or time constraint?"
 *
 * Cost model: each request "costs" an estimated response time in minutes
 * (a function of urgency: more urgent jobs are dispatched with faster,
 * costlier resources). Value = urgency (could be swapped for a richer score).
 */
public final class DynamicProgramming {

    private DynamicProgramming() {}

    public static int estimateCostMinutes(ServiceRequest r) {
        // higher urgency => a faster (more expensive-to-mobilise) response
        return switch (r.getUrgency()) {
            case 5 -> 10;
            case 4 -> 15;
            case 3 -> 25;
            case 2 -> 35;
            default -> 45;
        };
    }

    public static class Result {
        public final int totalValue;
        public final int totalCost;
        public final List<ServiceRequest> chosen;
        public final int[][] table; // memoisation/tabulation table for trace evidence

        Result(int totalValue, int totalCost, List<ServiceRequest> chosen, int[][] table) {
            this.totalValue = totalValue;
            this.totalCost = totalCost;
            this.chosen = chosen;
            this.table = table;
        }
    }

    /** Bottom-up tabulation DP with reconstruction, budget defaults to Params.BUDGET_MINUTES. */
    public static Result selectRequestsUnderBudget(List<ServiceRequest> requests) {
        return selectRequestsUnderBudget(requests, Params.BUDGET_MINUTES);
    }

    public static Result selectRequestsUnderBudget(List<ServiceRequest> requests, int budgetMinutes) {
        int n = requests.size();
        int[] cost = new int[n];
        int[] value = new int[n];
        for (int i = 0; i < n; i++) {
            cost[i] = estimateCostMinutes(requests.get(i));
            value[i] = requests.get(i).getUrgency();
        }

        // dp[i][b] = best total value using first i requests with budget b
        int[][] dp = new int[n + 1][budgetMinutes + 1];
        for (int i = 1; i <= n; i++) {
            for (int b = 0; b <= budgetMinutes; b++) {
                dp[i][b] = dp[i - 1][b]; // don't take request i-1
                if (cost[i - 1] <= b) {
                    dp[i][b] = Math.max(dp[i][b], dp[i - 1][b - cost[i - 1]] + value[i - 1]);
                }
            }
        }

        // reconstruction: walk back to find which requests were taken
        List<ServiceRequest> chosen = new ArrayList<>();
        int b = budgetMinutes;
        int totalCost = 0;
        for (int i = n; i >= 1; i--) {
            if (dp[i][b] != dp[i - 1][b]) {
                chosen.add(0, requests.get(i - 1));
                totalCost += cost[i - 1];
                b -= cost[i - 1];
            }
        }

        return new Result(dp[n][budgetMinutes], totalCost, chosen, dp);
    }
}
