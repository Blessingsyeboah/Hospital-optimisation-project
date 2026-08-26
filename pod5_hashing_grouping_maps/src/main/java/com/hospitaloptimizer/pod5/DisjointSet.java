package com.hospitaloptimizer.pod5;

import java.util.HashMap;
import java.util.Map;

/**
 * Pod 5 -- Disjoint Set (Union-Find), built from scratch, with union-by-rank
 * and path compression. Used by Kruskal's MST algorithm to detect cycles /
 * track connectivity groups ("Grouping" in the pod's remit).
 */
public class DisjointSet<T> {

    private final Map<T, T> parent = new HashMap<>();
    private final Map<T, Integer> rank = new HashMap<>();

    public void makeSet(T item) {
        if (!parent.containsKey(item)) {
            parent.put(item, item);
            rank.put(item, 0);
        }
    }

    public T find(T item) {
        if (!parent.containsKey(item)) makeSet(item);
        T p = parent.get(item);
        if (!p.equals(item)) {
            T root = find(p);       // path compression
            parent.put(item, root);
            return root;
        }
        return item;
    }

    /** Returns true if the two items were in different sets (i.e. a real union happened). */
    public boolean union(T a, T b) {
        T rootA = find(a);
        T rootB = find(b);
        if (rootA.equals(rootB)) return false; // already connected -- would form a cycle

        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);
        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }
        return true;
    }

    public boolean connected(T a, T b) { return find(a).equals(find(b)); }

    public int setCount() {
        long distinctRoots = parent.keySet().stream().map(this::find).distinct().count();
        return (int) distinctRoots;
    }
}
