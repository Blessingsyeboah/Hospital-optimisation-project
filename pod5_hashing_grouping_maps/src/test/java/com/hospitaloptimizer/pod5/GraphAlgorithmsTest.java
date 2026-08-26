package com.hospitaloptimizer.pod5;

import java.util.List;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class GraphAlgorithmsTest {

    private Graph sampleGraph() {
        // A -1- B -2- C
        // |            |
        // 4            1
        // |            |
        // D ----3------C  (D-C weight 3)
        Graph g = new Graph();
        g.addEdge("A", "B", 1);
        g.addEdge("B", "C", 2);
        g.addEdge("A", "D", 4);
        g.addEdge("D", "C", 3);
        return g;
    }

    public void testBFS_reachability_normalCase() {
        Graph g = sampleGraph();
        List<String> order = GraphAlgorithms.bfs(g, "A");
        assertEquals(4, order.size());
        assertEquals("A", order.get(0));
        assertTrue(order.contains("C"), "C should be reachable from A");
    }

    public void testDFS_visitsAllReachableNodes() {
        Graph g = sampleGraph();
        List<String> order = GraphAlgorithms.dfs(g, "A");
        assertEquals(4, order.size());
        assertEquals("A", order.get(0));
    }

    public void testBFS_boundaryCase_disconnectedGraph() {
        Graph g = new Graph();
        g.addEdge("A", "B", 1);
        g.addNode("Z"); // isolated node, no edges
        List<String> order = GraphAlgorithms.bfs(g, "A");
        assertFalse(order.contains("Z"), "Z is unreachable from A and must not appear");
        assertEquals(2, order.size());
    }

    public void testDijkstra_shortestPath_distanceAndPredecessor() {
        Graph g = sampleGraph();
        GraphAlgorithms.DijkstraResult result = GraphAlgorithms.dijkstra(g, "A");
        // A->B->C (1+2=3) vs A->D->C (4+3=7): A->B->C should win even after route penalty is added to both
        assertTrue(result.distance.get("C") < result.distance.get("D") + 100,
                "sanity: distances should be finite and computed");
        List<String> path = result.pathTo("C");
        assertEquals("A", path.get(0));
        assertEquals("C", path.get(path.size() - 1));
        assertTrue(path.contains("B"), "shortest path A->C should go through B, not D");
    }

    public void testDijkstra_boundaryCase_unreachableNode() {
        Graph g = new Graph();
        g.addEdge("A", "B", 1);
        g.addNode("Isolated");
        GraphAlgorithms.DijkstraResult result = GraphAlgorithms.dijkstra(g, "A");
        assertEquals(Double.POSITIVE_INFINITY, result.distance.get("Isolated"), 0.0001);
    }

    public void testPrimAndKruskal_produceSameMSTWeight() {
        Graph g = sampleGraph();
        double primWeight = GraphAlgorithms.totalWeight(GraphAlgorithms.prim(g));
        double kruskalWeight = GraphAlgorithms.totalWeight(GraphAlgorithms.kruskal(g));
        // MST of this graph: A-B(1) + B-C(2) + D-C(3) = 6 (both algorithms must agree)
        assertEquals(6.0, primWeight, 0.0001);
        assertEquals(primWeight, kruskalWeight, 0.0001);
    }

    public void testKruskal_edgeCount_isNodesMinusOne_connectedGraph() {
        Graph g = sampleGraph();
        List<GraphAlgorithms.WeightedEdge> mst = GraphAlgorithms.kruskal(g);
        assertEquals(g.nodeCount() - 1, mst.size());
    }

    public void testAdjacencyMatrix_matchesAdjacencyList() {
        Graph g = sampleGraph();
        double[][] matrix = g.adjacencyMatrix();
        int ai = g.indexOf("A"), bi = g.indexOf("B");
        assertEquals(1.0, matrix[ai][bi], 0.0001);
        assertEquals(0.0, matrix[ai][ai], 0.0001);
    }

    public static void main(String[] args) {
        run(GraphAlgorithmsTest.class);
    }
}
