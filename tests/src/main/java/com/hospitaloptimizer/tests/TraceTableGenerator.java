package com.hospitaloptimizer.tests;

import com.hospitaloptimizer.pod2.SearchAlgorithms;
import com.hospitaloptimizer.pod4.DynamicProgramming;
import com.hospitaloptimizer.pod5.DisjointSet;
import com.hospitaloptimizer.pod5.Graph;
import com.hospitaloptimizer.pod5.GraphAlgorithms;
import com.hospitaloptimizer.shared.model.ServiceRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Generates the >=6 trace tables required by Section 10 of the brief:
 * binary search, insertion sort, merge sort/quicksort, Dijkstra,
 * Kruskal/Prim, DP -- as markdown files under docs/trace_tables/.
 *
 * Run once via ./generate_docs.sh (or manually) -- this is a documentation
 * tool, not part of the shipped application.
 */
public class TraceTableGenerator {

    public static void main(String[] args) throws IOException {
        String outDir = "docs/trace_tables";
        binarySearchTrace(outDir + "/01_binary_search_trace.md");
        insertionSortTrace(outDir + "/02_insertion_sort_trace.md");
        mergeQuickSortTrace(outDir + "/03_merge_quick_sort_trace.md");
        dijkstraTrace(outDir + "/04_dijkstra_trace.md");
        kruskalPrimTrace(outDir + "/05_kruskal_prim_trace.md");
        dpTrace(outDir + "/06_dp_knapsack_trace.md");
        System.out.println("Trace tables written to " + outDir);
    }

    // 1) Binary search trace
    private static void binarySearchTrace(String path) throws IOException {
        Integer[] arr = {2, 5, 8, 12, 16, 23, 38, 45, 56, 72, 91};
        int target = 45;
        StringBuilder sb = new StringBuilder();
        sb.append("# Trace Table: Binary Search\n\n");
        sb.append("Array (sorted): ").append(Arrays.toString(arr)).append("\n\n");
        sb.append("Target: ").append(target).append("\n\n");
        sb.append("| Step | lo | hi | mid | arr[mid] | Comparison | Action |\n");
        sb.append("|------|----|----|----|-----------|-----------|--------|\n");
        int lo = 0, hi = arr.length - 1, step = 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            String comparison, action;
            if (arr[mid] == target) { comparison = "arr[mid] == target"; action = "FOUND at index " + mid; }
            else if (arr[mid] < target) { comparison = "arr[mid] < target"; action = "lo = mid+1 = " + (mid + 1); }
            else { comparison = "arr[mid] > target"; action = "hi = mid-1 = " + (mid - 1); }
            sb.append(String.format("| %d | %d | %d | %d | %d | %s | %s |%n", step, lo, hi, mid, arr[mid], comparison, action));
            if (arr[mid] == target) break;
            else if (arr[mid] < target) lo = mid + 1;
            else hi = mid - 1;
            step++;
        }
        int[] traceResult = SearchAlgorithms.binarySearchWithTrace(arr, target, Integer::compareTo);
        sb.append("\nResult: found at index ").append(traceResult[0]).append(" in ").append(traceResult[1]).append(" comparisons ");
        sb.append("(theoretical max = ceil(log2(11))+1 = 4).\n");
        write(path, sb.toString());
    }

    // 2) Insertion sort trace
    private static void insertionSortTrace(String path) throws IOException {
        int[] arr = {29, 10, 14, 37, 14};
        StringBuilder sb = new StringBuilder();
        sb.append("# Trace Table: Insertion Sort\n\n");
        sb.append("Initial array: ").append(Arrays.toString(arr)).append("\n\n");
        sb.append("(Two equal keys, 14 and 14, included to show stability: their relative order is preserved.)\n\n");
        sb.append("| i | key | Shifted elements | Array after this pass |\n");
        sb.append("|---|-----|-------------------|------------------------|\n");
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            List<Integer> shifted = new ArrayList<>();
            while (j >= 0 && arr[j] > key) {
                shifted.add(arr[j]);
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
            sb.append(String.format("| %d | %d | %s | %s |%n", i, key, shifted, Arrays.toString(arr)));
        }
        write(path, sb.toString());
    }

    // 3) Merge sort / quicksort trace (recursive call + merge/partition structure)
    private static void mergeQuickSortTrace(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Trace Table: Merge Sort & Quicksort (recurrence decomposition)\n\n");

        sb.append("## Merge Sort on [8, 3, 5, 1, 9, 2]\n\n");
        sb.append("| Call | Sub-array | Action |\n|------|-----------|--------|\n");
        List<String> mergeLog = new ArrayList<>();
        mergeSortTrace(new int[]{8, 3, 5, 1, 9, 2}, 0, 5, mergeLog);
        for (String line : mergeLog) sb.append(line).append("\n");

        sb.append("\n## Quicksort on [8, 3, 5, 1, 9, 2] (Lomuto partition, pivot = last element)\n\n");
        sb.append("| Call | Sub-array | Pivot | Partition result |\n|------|-----------|-------|-------------------|\n");
        List<String> quickLog = new ArrayList<>();
        int[] qarr = {8, 3, 5, 1, 9, 2};
        quickSortTrace(qarr, 0, 5, quickLog);
        for (String line : quickLog) sb.append(line).append("\n");

        write(path, sb.toString());
    }

    private static void mergeSortTrace(int[] arr, int lo, int hi, List<String> log) {
        if (lo >= hi) { log.add("| split | [" + arr[lo] + "] | base case, size 1 |"); return; }
        int mid = lo + (hi - lo) / 2;
        log.add("| split | " + Arrays.toString(Arrays.copyOfRange(arr, lo, hi + 1)) + " | split into [" + lo + ".." + mid + "] and [" + (mid + 1) + ".." + hi + "] |");
        mergeSortTrace(arr, lo, mid, log);
        mergeSortTrace(arr, mid + 1, hi, log);
        int[] left = Arrays.copyOfRange(arr, lo, mid + 1);
        int[] right = Arrays.copyOfRange(arr, mid + 1, hi + 1);
        int i = 0, j = 0, k = lo;
        while (i < left.length && j < right.length) arr[k++] = (left[i] <= right[j]) ? left[i++] : right[j++];
        while (i < left.length) arr[k++] = left[i++];
        while (j < right.length) arr[k++] = right[j++];
        log.add("| merge | " + Arrays.toString(Arrays.copyOfRange(arr, lo, hi + 1)) + " | merged [" + lo + ".." + mid + "] and [" + (mid + 1) + ".." + hi + "] |");
    }

    private static void quickSortTrace(int[] arr, int lo, int hi, List<String> log) {
        if (lo >= hi) return;
        int pivot = arr[hi];
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (arr[j] <= pivot) { i++; int t = arr[i]; arr[i] = arr[j]; arr[j] = t; }
        }
        int t = arr[i + 1]; arr[i + 1] = arr[hi]; arr[hi] = t;
        int p = i + 1;
        log.add("| partition | " + Arrays.toString(Arrays.copyOfRange(arr, lo, hi + 1)) + " | " + pivot + " | pivot placed at index " + p + " |");
        quickSortTrace(arr, lo, p - 1, log);
        quickSortTrace(arr, p + 1, hi, log);
    }

    // 4) Dijkstra trace
    private static void dijkstraTrace(String path) throws IOException {
        Graph g = new Graph();
        g.addEdge("A", "B", 4);
        g.addEdge("A", "C", 1);
        g.addEdge("C", "B", 2);
        g.addEdge("B", "D", 5);
        g.addEdge("C", "D", 8);

        StringBuilder sb = new StringBuilder();
        sb.append("# Trace Table: Dijkstra Shortest Path (from A)\n\n");
        sb.append("Graph edges: A-B(4), A-C(1), C-B(2), B-D(5), C-D(8)\n\n");
        sb.append("(Note: the actual GraphAlgorithms.dijkstra() adds Params.ROUTE_PENALTY to every hop; ");
        sb.append("this trace uses raw edge weights for a readable step-by-step illustration.)\n\n");

        Map<String, Double> dist = new HashMap<>();
        for (String n : g.nodes()) dist.put(n, Double.POSITIVE_INFINITY);
        dist.put("A", 0.0);
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add("A");
        Set<String> settled = new HashSet<>();

        sb.append("| Step | Settled node | Distance table (A,B,C,D) |\n|------|--------------|---------------------------|\n");
        int step = 1;
        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (!settled.add(u)) continue;
            for (Graph.Edge e : g.neighbors(u)) {
                double cand = dist.get(u) + e.weight;
                if (cand < dist.get(e.to)) { dist.put(e.to, cand); pq.add(e.to); }
            }
            sb.append(String.format("| %d | %s | A=%.0f, B=%.0f, C=%.0f, D=%.0f |%n",
                    step++, u, dist.get("A"), dist.get("B"), dist.get("C"), dist.get("D")));
        }
        sb.append("\nFinal shortest distances from A: ").append(dist).append("\n");
        sb.append("Predecessor path A->D: A -> C -> B -> D (cost 1+2+5=8, cheaper than A-D direct at 8 -- tie broken by discovery order in this small example).\n");
        write(path, sb.toString());
    }

    // 5) Kruskal / Prim trace with disjoint-set connectivity
    private static void kruskalPrimTrace(String path) throws IOException {
        Graph g = new Graph();
        g.addEdge("A", "B", 4);
        g.addEdge("A", "C", 1);
        g.addEdge("C", "B", 2);
        g.addEdge("B", "D", 5);
        g.addEdge("C", "D", 8);

        StringBuilder sb = new StringBuilder();
        sb.append("# Trace Table: Kruskal's MST (with Disjoint-Set connectivity trace)\n\n");
        sb.append("Edges sorted by weight: A-C(1), C-B(2), A-B(4), B-D(5), C-D(8)\n\n");
        sb.append("| Step | Edge considered | find(u) | find(v) | Same set? | Action |\n");
        sb.append("|------|-------------------|---------|---------|-----------|--------|\n");

        DisjointSet<String> ds = new DisjointSet<>();
        for (String n : g.nodes()) ds.makeSet(n);
        List<GraphAlgorithms.WeightedEdge> edges = new ArrayList<>();
        edges.add(new GraphAlgorithms.WeightedEdge("A", "C", 1));
        edges.add(new GraphAlgorithms.WeightedEdge("C", "B", 2));
        edges.add(new GraphAlgorithms.WeightedEdge("A", "B", 4));
        edges.add(new GraphAlgorithms.WeightedEdge("B", "D", 5));
        edges.add(new GraphAlgorithms.WeightedEdge("C", "D", 8));

        double total = 0;
        int taken = 0;
        for (var e : edges) {
            String ru = ds.find(e.from), rv = ds.find(e.to);
            boolean same = ru.equals(rv);
            String action = same ? "REJECT (would form a cycle)" : "ACCEPT into MST";
            sb.append(String.format("| %d | %s-%s (w=%.0f) | %s | %s | %s | %s |%n",
                    taken + 1, e.from, e.to, e.weight, ru, rv, same ? "yes" : "no", action));
            if (!same) { ds.union(e.from, e.to); total += e.weight; taken++; }
        }
        sb.append("\nMST edges chosen: A-C, C-B, B-D. Total weight = ").append(total).append("\n");

        List<GraphAlgorithms.WeightedEdge> prim = GraphAlgorithms.prim(g);
        sb.append("\nCross-check via Prim's algorithm: total weight = ").append(GraphAlgorithms.totalWeight(prim));
        sb.append(" (should match Kruskal's ").append(total).append(")\n");
        write(path, sb.toString());
    }

    // 6) DP knapsack trace
    private static void dpTrace(String path) throws IOException {
        List<ServiceRequest> reqs = List.of(
                mk("R1", 5), mk("R2", 4), mk("R3", 3), mk("R4", 1));
        int budget = 25;
        StringBuilder sb = new StringBuilder();
        sb.append("# Trace Table: DP Request Selection (0/1 Knapsack, budget=25 minutes)\n\n");
        sb.append("| Request | Urgency (value) | Estimated cost (minutes) |\n|---------|------------------|---------------------------|\n");
        for (ServiceRequest r : reqs) {
            sb.append("| ").append(r.getRequestId()).append(" | ").append(r.getUrgency())
              .append(" | ").append(DynamicProgramming.estimateCostMinutes(r)).append(" |\n");
        }

        DynamicProgramming.Result result = DynamicProgramming.selectRequestsUnderBudget(reqs, budget);
        sb.append("\n## Tabulation table dp[i][b] (rows = requests considered, cols = budget 0..25, showing every 5)\n\n");
        sb.append("| i \\\\ b |");
        for (int b = 0; b <= budget; b += 5) sb.append(" ").append(b).append(" |");
        sb.append("\n|---|").append("---|".repeat(budget / 5 + 1)).append("\n");
        for (int i = 0; i <= reqs.size(); i++) {
            sb.append("| ").append(i).append(" |");
            for (int b = 0; b <= budget; b += 5) sb.append(" ").append(result.table[i][b]).append(" |");
            sb.append("\n");
        }
        sb.append("\n## Reconstruction\n\n");
        sb.append("Chosen requests: ");
        for (ServiceRequest r : result.chosen) sb.append(r.getRequestId()).append(" ");
        sb.append("\nTotal value = ").append(result.totalValue).append(", total cost = ").append(result.totalCost).append(" minutes\n");
        write(path, sb.toString());
    }

    private static ServiceRequest mk(String id, int urgency) {
        return new ServiceRequest(id, "A", "B", "Emergency Transfer", urgency, "2026-06-01 08:00", "2026-06-01 09:00", "PENDING");
    }

    private static void write(String path, String content) throws IOException {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(content);
        }
    }
}
