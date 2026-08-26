package com.hospitaloptimizer.pod5;

import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class DisjointSetTest {

    public void testUnionFind_normalCase() {
        DisjointSet<String> ds = new DisjointSet<>();
        ds.makeSet("A"); ds.makeSet("B"); ds.makeSet("C");
        assertFalse(ds.connected("A", "B"), "A and B start disconnected");
        ds.union("A", "B");
        assertTrue(ds.connected("A", "B"), "A and B should be connected after union");
        assertFalse(ds.connected("A", "C"), "C is still separate");
    }

    /** Kruskal connectivity trace: unioning an already-connected pair must be rejected (cycle detection). */
    public void testUnion_rejectsCycle() {
        DisjointSet<String> ds = new DisjointSet<>();
        ds.union("A", "B");
        ds.union("B", "C");
        boolean formedCycle = !ds.union("A", "C"); // A and C already connected via B
        assertTrue(formedCycle, "unioning A and C should be a no-op: they're already in the same set");
    }

    public void testSetCount_afterUnions() {
        DisjointSet<String> ds = new DisjointSet<>();
        for (String n : new String[]{"A", "B", "C", "D"}) ds.makeSet(n);
        assertEquals(4, ds.setCount());
        ds.union("A", "B");
        assertEquals(3, ds.setCount());
        ds.union("C", "D");
        assertEquals(2, ds.setCount());
        ds.union("A", "C");
        assertEquals(1, ds.setCount());
    }

    public void testBoundaryCase_findOnUnknownItemAutoCreates() {
        DisjointSet<String> ds = new DisjointSet<>();
        String root = ds.find("never-added-before");
        assertEquals("never-added-before", root);
    }

    public static void main(String[] args) {
        run(DisjointSetTest.class);
    }
}
