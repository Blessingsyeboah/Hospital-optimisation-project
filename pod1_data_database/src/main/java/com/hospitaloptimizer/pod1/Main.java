package com.hospitaloptimizer.pod1;

import com.hospitaloptimizer.pod2.SearchAlgorithms;
import com.hospitaloptimizer.pod2.SortAlgorithms;
import com.hospitaloptimizer.pod3.SchedulingEngine;
import com.hospitaloptimizer.pod4.DynamicProgramming;
import com.hospitaloptimizer.pod4.IndexEngine;
import com.hospitaloptimizer.pod5.Graph;
import com.hospitaloptimizer.pod5.GraphAlgorithms;
import com.hospitaloptimizer.shared.db.Database;
import com.hospitaloptimizer.shared.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Hospital Smart Service Operations Optimizer -- console entry point.
 * This is the "console menu that allows an examiner to run demonstrations
 * without editing source code" required by Section 8 of the brief.
 *
 * Every demo below reads real choices from the examiner (a location pair to
 * route, a requestId to look up, a sample size, a custom budget, etc.) so
 * the output reflects what THIS run's user asked for, instead of always
 * printing the same fixed sample no matter what.
 *
 * Run: ./run.sh   (from the repo root, after ./build.sh)
 */
public class Main {

    private static Database db;
    private static DataLoader loader;
    private static List<Location> locations;
    private static List<Road> roads;
    private static List<Resource> resources;
    private static List<ServiceRequest> requests;
    private static final DispatchDeliveryService delivery = new DispatchDeliveryService();

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println(" Hospital Smart Service Operations Optimizer (Ghana)");
        System.out.println(" DCIT 204/308 Joint DSA Semester Project");
        System.out.println("=====================================================");

        db = new Database("hospital_optimizer.db");
        db.initSchema("schema.sql");
        loader = new DataLoader(db);
        loader.seedDatabaseIfEmpty();

        locations = loader.loadLocations();
        roads = loader.loadRoads();
        resources = loader.loadResources();
        requests = loader.loadServiceRequests();

        System.out.printf("Loaded from database: %d locations, %d roads, %d resources, %d service requests%n%n",
                locations.size(), roads.size(), resources.size(), requests.size());

        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> demoSearchAndSort(sc);
                case "2" -> demoScheduling(sc);
                case "3" -> demoIndexing(sc);
                case "4" -> demoRouting(sc);
                case "5" -> demoMST(sc);
                case "6" -> demoBudgetSelection(sc);
                case "7" -> demoDispatchAndUndo(sc);
                case "8" -> runBenchmarks(sc);
                case "0" -> { running = false; System.out.println("Goodbye."); }
                default -> System.out.println("Unrecognised option, try again.");
            }
            System.out.println();
        }
        db.close();
    }

    private static void printMenu() {
        System.out.println("---- MAIN MENU ----");
        System.out.println("1. Pod 2 demo: search & sort service requests by urgency");
        System.out.println("2. Pod 3 demo: FIFO vs urgency vs priority-score dispatch order");
        System.out.println("3. Pod 4 demo: BST / Red-Black / B-Tree index lookup by requestId");
        System.out.println("4. Pod 5 demo: Dijkstra shortest route between two locations");
        System.out.println("5. Pod 5 demo: minimum-cost road network (Prim vs Kruskal)");
        System.out.println("6. Pod 4 demo: DP request selection under a response-time budget");
        System.out.println("7. Pod 1 demo: greedy assignment + audit-stack undo");
        System.out.println("8. Run performance benchmarks (writes CSV + algorithm_runs table)");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    // ---------------------------------------------------------------
    // Small input helpers -- keep every demo interactive & defensive
    // against bad input instead of crashing or silently ignoring it.
    // ---------------------------------------------------------------

    /** Reads an int in [min,max]; blank input returns defaultVal; re-prompts on bad input. */
    private static int readInt(Scanner sc, String prompt, int defaultVal, int min, int max) {
        while (true) {
            System.out.print(prompt + " [default " + defaultVal + "]: ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return defaultVal;
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    System.out.println("  Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  Not a number, try again.");
            }
        }
    }

    /** Reads a line; blank input returns defaultVal. */
    private static String readLine(Scanner sc, String prompt, String defaultVal) {
        System.out.print(prompt + (defaultVal != null ? " [default " + defaultVal + "]" : "") + ": ");
        String line = sc.nextLine().trim();
        return line.isEmpty() ? defaultVal : line;
    }

    /** Reads y/n; blank input returns defaultVal. */
    private static boolean readYesNo(Scanner sc, String prompt, boolean defaultVal) {
        String d = defaultVal ? "Y/n" : "y/N";
        System.out.print(prompt + " (" + d + "): ");
        String line = sc.nextLine().trim().toLowerCase();
        if (line.isEmpty()) return defaultVal;
        return line.startsWith("y");
    }

    private static Location findLocationById(String id) {
        for (Location l : locations) if (l.getLocationId().equalsIgnoreCase(id)) return l;
        return null;
    }

    private static ServiceRequest findRequestById(String id) {
        for (ServiceRequest r : requests) if (r.getRequestId().equalsIgnoreCase(id)) return r;
        return null;
    }

    /** Prompts for a location id, showing the sample list and re-asking until a real id is entered. */
    private static Location promptForLocation(Scanner sc, String label, String defaultId) {
        while (true) {
            String id = readLine(sc, "Enter " + label + " location id (or 'list' to see all)", defaultId);
            if (id.equalsIgnoreCase("list")) {
                for (Location l : locations) System.out.println("  " + l);
                continue;
            }
            Location loc = findLocationById(id);
            if (loc != null) return loc;
            System.out.println("  No location with id '" + id + "'. Type 'list' to see valid ids.");
        }
    }

    // ---------------------------------------------------------------
    // Demos
    // ---------------------------------------------------------------

    private static void demoSearchAndSort(Scanner sc) {
        int topN = readInt(sc, "How many sorted requests to display?", 5, 1, requests.size());
        System.out.println("Sort direction: 1=ascending urgency, 2=descending urgency");
        String dirChoice = readLine(sc, "Choose direction", "1");
        boolean ascending = !dirChoice.trim().equals("2");

        ServiceRequest[] arr = requests.toArray(new ServiceRequest[0]);
        Comparator<ServiceRequest> byUrgency = ascending
                ? (a, b) -> Integer.compare(a.getUrgency(), b.getUrgency())
                : (a, b) -> Integer.compare(b.getUrgency(), a.getUrgency());
        SortAlgorithms.quickSort(arr, byUrgency);

        System.out.println("Top " + topN + " requests by urgency (" + (ascending ? "ascending" : "descending") + "):");
        for (int i = 0; i < Math.min(topN, arr.length); i++) System.out.println("  " + arr[i]);

        String searchId = readLine(sc, "Enter a requestId to binary-search for (blank to skip)", null);
        if (searchId != null) {
            ServiceRequest target = findRequestById(searchId);
            if (target == null) {
                System.out.println("  No such requestId in the dataset.");
            } else {
                int idx = SearchAlgorithms.binarySearch(arr, target, byUrgency);
                System.out.println("Binary search for " + target.getRequestId()
                        + " found at sorted index " + idx + " (" + (idx >= 0 ? "hit" : "not present in this ordering") + ")");
            }
        }
    }

    private static void demoScheduling(Scanner sc) {
        int sampleSize = readInt(sc, "How many requests to include in the dispatch sample?", 8, 1, requests.size());
        SchedulingEngine engine = new SchedulingEngine();
        List<ServiceRequest> sample = requests.subList(0, sampleSize);

        System.out.println("FIFO order (submission order): ");
        engine.dispatchFIFO(sample).forEach(r -> System.out.println("  " + r.getRequestId() + " urgency=" + r.getUrgency()));
        System.out.println("Urgency order (highest first): ");
        engine.dispatchByUrgency(sample).forEach(r -> System.out.println("  " + r.getRequestId() + " urgency=" + r.getUrgency()));

        if (readYesNo(sc, "Also show weighted priority-score order?", false)) {
            System.out.println("Priority-score order (urgency x Params.PRIORITY_WEIGHT, highest first): ");
            engine.dispatchByPriorityScore(sample).forEach(r ->
                    System.out.println("  " + r.getRequestId() + " urgency=" + r.getUrgency()
                            + " score=" + String.format("%.2f", engine.priorityScore(r))));
        }
    }

    private static void demoIndexing(Scanner sc) {
        IndexEngine idx = new IndexEngine();
        idx.buildAll(requests);

        String defaultId = requests.get(requests.size() / 2).getRequestId();
        String sampleId;
        while (true) {
            sampleId = readLine(sc, "Enter a requestId to look up (or 'list' to see a few ids)", defaultId);
            if (sampleId.equalsIgnoreCase("list")) {
                for (int i = 0; i < Math.min(10, requests.size()); i++) System.out.println("  " + requests.get(i).getRequestId());
                continue;
            }
            if (findRequestById(sampleId) != null) break;
            System.out.println("  No such requestId. Type 'list' to see valid ids.");
        }

        System.out.println("Looking up " + sampleId + " ...");
        System.out.println("  via BST:             " + idx.findByBST(sampleId));
        System.out.println("  via Red-Black Tree:  " + idx.findByRedBlackTree(sampleId));
        System.out.println("  via B-Tree:          " + idx.findByBTree(sampleId));
        System.out.println("Tree heights over " + requests.size() + " requests -- BST: " + idx.bstHeight()
                + ", Red-Black: " + idx.redBlackHeight() + ", B-Tree: " + idx.bTreeHeight());
    }

    private static void demoRouting(Scanner sc) {
        Graph g = delivery.buildRoadGraph(roads);
        System.out.println("Sample locations: ");
        for (int i = 0; i < Math.min(5, locations.size()); i++) {
            System.out.println("  " + locations.get(i).getLocationId() + " = " + locations.get(i).getName());
        }

        String defaultFrom = locations.get(0).getLocationId();
        String defaultTo = locations.get(Math.min(10, locations.size() - 1)).getLocationId();
        Location fromLoc = promptForLocation(sc, "FROM", defaultFrom);
        Location toLoc = promptForLocation(sc, "TO", defaultTo);
        String from = fromLoc.getLocationId();
        String to = toLoc.getLocationId();

        System.out.println("Computing shortest route " + from + " (" + fromLoc.getName() + ") -> "
                + to + " (" + toLoc.getName() + ") ...");
        GraphAlgorithms.DijkstraResult result = delivery.routeFrom(g, from);
        Double distance = result.distance.get(to);
        if (distance == null) {
            System.out.println("  No path exists between these two locations in the road graph.");
        } else {
            System.out.println("  distance (weighted minutes incl. route penalty): " + distance);
            System.out.println("  path: " + result.pathTo(to));
        }
    }

    private static void demoMST(Scanner sc) {
        Graph g = delivery.buildRoadGraph(roads);
        var prim = GraphAlgorithms.prim(g);
        var kruskal = GraphAlgorithms.kruskal(g);
        System.out.println("Prim MST total weight:    " + GraphAlgorithms.totalWeight(prim) + " (" + prim.size() + " edges)");
        System.out.println("Kruskal MST total weight: " + GraphAlgorithms.totalWeight(kruskal) + " (" + kruskal.size() + " edges)");

        if (readYesNo(sc, "Print the full edge list for both trees?", false)) {
            System.out.println("Prim edges:");
            prim.forEach(e -> System.out.println("  " + e));
            System.out.println("Kruskal edges:");
            kruskal.forEach(e -> System.out.println("  " + e));
        }
    }

    private static void demoBudgetSelection(Scanner sc) {
        List<ServiceRequest> pending = requests.stream().filter(r -> "PENDING".equals(r.getStatus())).toList();
        int defaultBudget = com.hospitaloptimizer.shared.util.Params.BUDGET_MINUTES;
        int budget = readInt(sc, "Response-time budget in minutes for this run", defaultBudget, 0, 100000);

        DynamicProgramming.Result result = DynamicProgramming.selectRequestsUnderBudget(pending, budget);
        System.out.println("Budget = " + budget + " minutes (" + pending.size() + " pending requests considered)");
        System.out.println("Selected " + result.chosen.size() + " requests, total urgency value = " + result.totalValue
                + ", total cost = " + result.totalCost + " minutes");
        result.chosen.forEach(r -> System.out.println("  " + r.getRequestId() + " urgency=" + r.getUrgency()));
    }

    private static void demoDispatchAndUndo(Scanner sc) {
        List<ServiceRequest> pendingAll = requests.stream().filter(r -> "PENDING".equals(r.getStatus())).toList();
        if (pendingAll.isEmpty()) {
            System.out.println("No PENDING requests available to dispatch right now.");
            return;
        }
        int count = readInt(sc, "How many pending requests to assign this run?", Math.min(5, pendingAll.size()), 1, pendingAll.size());
        List<ServiceRequest> pending = pendingAll.subList(0, count);

        Map<String, String> assignment = delivery.assignResources(pending, resources);
        System.out.println("Greedy assignment (request -> resource):");
        if (assignment.isEmpty()) {
            System.out.println("  (no available resources to assign)");
        } else {
            assignment.forEach((req, res) -> System.out.println("  " + req + " -> " + res));
        }
        System.out.println("Audit log size: " + delivery.auditLogSize());

        if (readYesNo(sc, "Undo the most recent dispatch action now?", true)) {
            String undone = delivery.undoLastDispatch();
            System.out.println("Undid: " + undone + " (audit log size now " + delivery.auditLogSize() + ")");
        } else {
            System.out.println("Kept all " + delivery.auditLogSize() + " dispatch action(s) on the audit log.");
        }
    }

    private static void runBenchmarks(Scanner sc) {
        boolean useCustom = readYesNo(sc, "Use custom input sizes instead of the defaults?", false);
        int[] generalSizes = {100, 500, 1000, 5000};
        int[] smallSizes = {100, 1000, 5000};
        int[] graphSizes = {50, 100, 200};

        if (useCustom) {
            generalSizes = readSizeList(sc, "Sizes for search/sort comparison", generalSizes);
            smallSizes = readSizeList(sc, "Sizes for hash-load-factor / heap-dispatch experiments", smallSizes);
            graphSizes = readSizeList(sc, "Sizes for graph-algorithm experiment", graphSizes);
        }

        System.out.println("Running performance experiments (this may take a few seconds)...");
        Benchmark bench = new Benchmark(loader, "docs/performance/results.csv");
        bench.runSearchComparison(generalSizes);
        bench.runSortingComparison(generalSizes);
        bench.runHashLoadFactorExperiment(smallSizes);
        bench.runHeapDispatchExperiment(smallSizes);
        bench.runGraphAlgorithmsExperiment(graphSizes);
        System.out.println("Done. Results appended to docs/performance/results.csv and the algorithm_runs table.");
    }

    /** Reads a comma-separated list of positive ints, e.g. "100,500,2000"; blank keeps the default. */
    private static int[] readSizeList(Scanner sc, String label, int[] defaultSizes) {
        StringBuilder def = new StringBuilder();
        for (int i = 0; i < defaultSizes.length; i++) {
            if (i > 0) def.append(",");
            def.append(defaultSizes[i]);
        }
        while (true) {
            String line = readLine(sc, label + " (comma-separated)", def.toString());
            try {
                String[] parts = line.split(",");
                List<Integer> sizes = new ArrayList<>();
                for (String p : parts) {
                    int v = Integer.parseInt(p.trim());
                    if (v <= 0) throw new NumberFormatException("must be positive");
                    sizes.add(v);
                }
                int[] result = new int[sizes.size()];
                for (int i = 0; i < result.length; i++) result[i] = sizes.get(i);
                return result;
            } catch (NumberFormatException e) {
                System.out.println("  Please enter positive whole numbers separated by commas, e.g. 100,500,1000");
            }
        }
    }
}
