package com.hospitaloptimizer.pod5;

import java.util.*;

/**
 * Pod 5 -- Graph built from scratch with BOTH an adjacency list (for
 * efficient BFS/DFS/Dijkstra/Prim) and an adjacency matrix (for O(1)
 * edge-existence checks and to satisfy Section 6's "adjacency list and
 * matrix" requirement). Undirected, weighted (roads work both ways).
 */
public class Graph {

    public static class Edge {
        public final String to;
        public final double weight;
        public Edge(String to, double weight) { this.to = to; this.weight = weight; }
    }

    private final CustomHashMap<String, List<Edge>> adjacencyList = new CustomHashMap<>();
    private final List<String> nodeOrder = new ArrayList<>(); // stable ordering for the matrix
    private final CustomHashMap<String, Integer> nodeIndex = new CustomHashMap<>();
    private double[][] adjacencyMatrix; // rebuilt lazily
    private boolean matrixDirty = true;

    public void addNode(String id) {
        if (!adjacencyList.containsKey(id)) {
            adjacencyList.put(id, new ArrayList<>());
            nodeIndex.put(id, nodeOrder.size());
            nodeOrder.add(id);
            matrixDirty = true;
        }
    }

    public void addEdge(String from, String to, double weight) {
        addNode(from);
        addNode(to);
        adjacencyList.get(from).add(new Edge(to, weight));
        adjacencyList.get(to).add(new Edge(from, weight)); // undirected
        matrixDirty = true;
    }

    public List<Edge> neighbors(String node) {
        List<Edge> edges = adjacencyList.get(node);
        return edges == null ? List.of() : edges;
    }

    public List<String> nodes() { return nodeOrder; }

    public int nodeCount() { return nodeOrder.size(); }

    /** Builds (or returns cached) adjacency matrix; matrix[i][j] = weight, or Double.POSITIVE_INFINITY if no edge. */
    public double[][] adjacencyMatrix() {
        if (matrixDirty) {
            int n = nodeOrder.size();
            adjacencyMatrix = new double[n][n];
            for (double[] row : adjacencyMatrix) Arrays.fill(row, Double.POSITIVE_INFINITY);
            for (int i = 0; i < n; i++) adjacencyMatrix[i][i] = 0;
            for (String from : nodeOrder) {
                int fi = nodeIndex.get(from);
                for (Edge e : adjacencyList.get(from)) {
                    int ti = nodeIndex.get(e.to);
                    adjacencyMatrix[fi][ti] = e.weight;
                }
            }
            matrixDirty = false;
        }
        return adjacencyMatrix;
    }

    public int indexOf(String node) { return nodeIndex.get(node); }
}
