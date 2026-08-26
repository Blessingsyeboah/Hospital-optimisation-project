package com.hospitaloptimizer.pod5;

import com.hospitaloptimizer.shared.util.Params;

import java.util.*;

/**
 * Pod 5 -- BFS, DFS, Dijkstra, Prim and Kruskal, all implemented from
 * scratch on top of the custom Graph, CustomHashMap, CustomHashSet and
 * DisjointSet built in this pod. Answers Section 3's routing/reachability/
 * MST questions.
 */
public final class GraphAlgorithms {

    private GraphAlgorithms() {}

    /** Breadth-first traversal order + which nodes are reachable from start. */
    public static List<String> bfs(Graph g, String start) {
        List<String> order = new ArrayList<>();
        CustomHashSet<String> visited = new CustomHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            String node = queue.poll();
            order.add(node);
            for (Graph.Edge e : g.neighbors(node)) {
                if (visited.add(e.to)) {
                    queue.add(e.to);
                }
            }
        }
        return order;
    }

    /** Depth-first traversal order (iterative, using an explicit stack). */
    public static List<String> dfs(Graph g, String start) {
        List<String> order = new ArrayList<>();
        CustomHashSet<String> visited = new CustomHashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            String node = stack.pop();
            if (!visited.add(node)) continue;
            order.add(node);
            // push in reverse so neighbors are visited in original order
            List<Graph.Edge> neighbors = g.neighbors(node);
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                if (!visited.contains(neighbors.get(i).to)) stack.push(neighbors.get(i).to);
            }
        }
        return order;
    }

    public static class DijkstraResult {
        public final Map<String, Double> distance;
        public final Map<String, String> predecessor;
        DijkstraResult(Map<String, Double> distance, Map<String, String> predecessor) {
            this.distance = distance; this.predecessor = predecessor;
        }
        public List<String> pathTo(String target) {
            LinkedList<String> path = new LinkedList<>();
            String cur = target;
            while (cur != null) {
                path.addFirst(cur);
                cur = predecessor.get(cur);
            }
            return path;
        }
    }

    /** Shortest path from start to all nodes, using a Params-derived route penalty added per hop. */
    public static DijkstraResult dijkstra(Graph g, String start) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String n : g.nodes()) dist.put(n, Double.POSITIVE_INFINITY);
        dist.put(start, 0.0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(start);
        CustomHashSet<String> settled = new CustomHashSet<>();

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (!settled.add(u)) continue;
            for (Graph.Edge e : g.neighbors(u)) {
                double candidate = dist.get(u) + e.weight + Params.ROUTE_PENALTY;
                if (candidate < dist.get(e.to)) {
                    dist.put(e.to, candidate);
                    prev.put(e.to, u);
                    pq.add(e.to);
                }
            }
        }
        return new DijkstraResult(dist, prev);
    }

    public static class WeightedEdge {
        public final String from, to;
        public final double weight;
        public WeightedEdge(String from, String to, double weight) { this.from = from; this.to = to; this.weight = weight; }

        @Override
        public String toString() {
            return from + " -- " + to + " (weight=" + weight + ")";
        }
    }

    /** Minimum Spanning Tree via Prim's algorithm, starting from an arbitrary node. */
    public static List<WeightedEdge> prim(Graph g) {
        List<WeightedEdge> mst = new ArrayList<>();
        if (g.nodeCount() == 0) return mst;
        CustomHashSet<String> inTree = new CustomHashSet<>();
        String start = g.nodes().get(0);
        inTree.add(start);

        PriorityQueue<WeightedEdge> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.weight));
        for (Graph.Edge e : g.neighbors(start)) pq.add(new WeightedEdge(start, e.to, e.weight));

        while (!pq.isEmpty() && inTree.size() < g.nodeCount()) {
            WeightedEdge edge = pq.poll();
            if (inTree.contains(edge.to)) continue;
            inTree.add(edge.to);
            mst.add(edge);
            for (Graph.Edge e : g.neighbors(edge.to)) {
                if (!inTree.contains(e.to)) pq.add(new WeightedEdge(edge.to, e.to, e.weight));
            }
        }
        return mst;
    }

    /** Minimum Spanning Tree via Kruskal's algorithm, using the DisjointSet built in this pod. */
    public static List<WeightedEdge> kruskal(Graph g) {
        List<WeightedEdge> allEdges = new ArrayList<>();
        CustomHashSet<String> seenPairs = new CustomHashSet<>();
        for (String from : g.nodes()) {
            for (Graph.Edge e : g.neighbors(from)) {
                String pairKey = from.compareTo(e.to) < 0 ? from + "|" + e.to : e.to + "|" + from;
                if (seenPairs.add(pairKey)) {
                    allEdges.add(new WeightedEdge(from, e.to, e.weight));
                }
            }
        }
        allEdges.sort(Comparator.comparingDouble(e -> e.weight));

        DisjointSet<String> ds = new DisjointSet<>();
        for (String n : g.nodes()) ds.makeSet(n);

        List<WeightedEdge> mst = new ArrayList<>();
        for (WeightedEdge e : allEdges) {
            if (ds.union(e.from, e.to)) { // true => did not already share a root => no cycle
                mst.add(e);
            }
        }
        return mst;
    }

    public static double totalWeight(List<WeightedEdge> edges) {
        double sum = 0;
        for (WeightedEdge e : edges) sum += e.weight;
        return sum;
    }
}
