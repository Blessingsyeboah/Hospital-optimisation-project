package com.hospitaloptimizer.pod1;

import com.hospitaloptimizer.pod2.SearchAlgorithms;
import com.hospitaloptimizer.pod2.SortAlgorithms;
import com.hospitaloptimizer.pod3.CustomPriorityQueue;
import com.hospitaloptimizer.pod5.CustomHashMap;
import com.hospitaloptimizer.pod5.Graph;
import com.hospitaloptimizer.pod5.GraphAlgorithms;
import com.hospitaloptimizer.shared.util.CsvUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Pod 1 -- Section 9 "Required performance experiments". Times each
 * algorithm across the minimum input sizes the brief specifies, averages
 * three runs, and writes results to both the database (algorithm_runs) and
 * a CSV file so the team can plot line graphs in Excel/Python.
 *
 * NOTE ON MACHINE SPEC: all Section 9 performance experiments in this
 * report were run on the following machine, kept constant across every
 * experiment so results are comparable:
 *   Device:        DESKTOP-KNHPH2Q
 *   Processor:     Intel(R) Core(TM) i5-7200U CPU @ 2.50GHz (2.70 GHz)
 *   RAM:           8.00 GB
 *   OS:            Windows 11 Pro, version 22H2, build 22621.4317
 *   System type:   64-bit OS, x64-based processor
 *   JDK version:   21
 */
public class Benchmark {

    private final DataLoader loader;
    private final String csvOutputPath;
    private final Random rnd = new Random(com.hospitaloptimizer.shared.util.Params.RANDOM_SEED);

    public Benchmark(DataLoader loader, String csvOutputPath) {
        this.loader = loader;
        this.csvOutputPath = csvOutputPath;
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void record(String algorithm, int inputSize, long timeNs) {
        long memKb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
        String date = now();
        loader.logAlgorithmRun(algorithm, inputSize, timeNs, memKb, date);
        CsvUtil.appendRow(csvOutputPath, algorithm, String.valueOf(inputSize), String.valueOf(timeNs), String.valueOf(memKb), date);
    }

    private long timeAveraged(int runs, Runnable action) {
        long total = 0;
        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();
            action.run();
            total += (System.nanoTime() - start);
        }
        return total / runs;
    }

    public void runSearchComparison(int[] sizes) {
        for (int n : sizes) {
            Integer[] sorted = randomSortedArray(n);
            int target = sorted[rnd.nextInt(n)];
            long linearTime = timeAveraged(3, () -> SearchAlgorithms.linearSearch(sorted, target, Integer::compareTo));
            long binaryTime = timeAveraged(3, () -> SearchAlgorithms.binarySearch(sorted, target, Integer::compareTo));
            record("LinearSearch", n, linearTime);
            record("BinarySearch", n, binaryTime);
        }
    }

    public void runSortingComparison(int[] sizes) {
        for (int n : sizes) {
            long selTime = timeAveraged(3, () -> SortAlgorithms.selectionSort(randomArray(n), Integer::compareTo));
            long insTime = timeAveraged(3, () -> SortAlgorithms.insertionSort(randomArray(n), Integer::compareTo));
            long mergeTime = timeAveraged(3, () -> SortAlgorithms.mergeSort(randomArray(n), Integer::compareTo));
            long quickTime = timeAveraged(3, () -> SortAlgorithms.quickSort(randomArray(n), Integer::compareTo));
            record("SelectionSort", n, selTime);
            record("InsertionSort", n, insTime);
            record("MergeSort", n, mergeTime);
            record("QuickSort", n, quickTime);
        }
    }

    public void runHashLoadFactorExperiment(int[] sizes) {
        for (int n : sizes) {
            long t = timeAveraged(3, () -> {
                CustomHashMap<Integer, Integer> map = new CustomHashMap<>(16);
                for (int i = 0; i < n; i++) map.put(i, i);
            });
            record("HashMapInsert_loadFactorTest", n, t);
        }
    }

    public void runHeapDispatchExperiment(int[] sizes) {
        for (int n : sizes) {
            long t = timeAveraged(3, () -> {
                CustomPriorityQueue<Integer> pq = new CustomPriorityQueue<>(Integer::compareTo);
                for (int i = 0; i < n; i++) pq.insert(rnd.nextInt());
                while (!pq.isEmpty()) pq.extractMin();
            });
            record("HeapInsertExtract", n, t);
        }
    }

    public void runGraphAlgorithmsExperiment(int[] nodeCounts) {
        for (int n : nodeCounts) {
            Graph g = randomConnectedGraph(n);
            String start = g.nodes().get(0);
            long bfsTime = timeAveraged(3, () -> GraphAlgorithms.bfs(g, start));
            long dfsTime = timeAveraged(3, () -> GraphAlgorithms.dfs(g, start));
            long dijkstraTime = timeAveraged(3, () -> GraphAlgorithms.dijkstra(g, start));
            long primTime = timeAveraged(3, () -> GraphAlgorithms.prim(g));
            record("BFS", n, bfsTime);
            record("DFS", n, dfsTime);
            record("Dijkstra", n, dijkstraTime);
            record("Prim", n, primTime);
        }
    }

    private Integer[] randomArray(int n) {
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt(1_000_000);
        return arr;
    }

    private Integer[] randomSortedArray(int n) {
        Integer[] arr = randomArray(n);
        SortAlgorithms.mergeSort(arr, Integer::compareTo);
        return arr;
    }

    private Graph randomConnectedGraph(int n) {
        Graph g = new Graph();
        String[] ids = new String[n];
        for (int i = 0; i < n; i++) ids[i] = "N" + i;
        for (int i = 1; i < n; i++) {
            g.addEdge(ids[i], ids[rnd.nextInt(i)], 1 + rnd.nextInt(20));
        }
        return g;
    }
}
