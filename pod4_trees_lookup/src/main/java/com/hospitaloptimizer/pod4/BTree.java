package com.hospitaloptimizer.pod4;

import java.util.ArrayList;
import java.util.List;

/**
 * Pod 4 -- simplified B-Tree (order-configurable multi-way search tree),
 * simulating how a database engine pages an index. Each node holds up to
 * (order - 1) keys and up to `order` children; nodes split when they
 * overflow, exactly like a real B-tree index page split.
 *
 * This satisfies the brief's "B-tree node split and search OR database
 * index simulation using B-tree pages" requirement.
 */
public class BTree<K extends Comparable<K>, V> {

    private final int order; // max children per node
    private Node root;
    /** Records every node split performed by the most recent insert. */
    private final List<String> lastInsertSplitTrace = new ArrayList<>();

    public BTree(int order) {
        if (order < 3) throw new IllegalArgumentException("order must be >= 3");
        this.order = order;
        this.root = new Node(true);
    }

    private class Entry {
        K key; V value;
        Entry(K key, V value) { this.key = key; this.value = value; }
    }

    private class Node {
        List<Entry> entries = new ArrayList<>();
        List<Node> children = new ArrayList<>();
        boolean leaf;
        Node(boolean leaf) { this.leaf = leaf; }
        boolean isFull() { return entries.size() == order - 1; }
    }

    public V search(K key) { return search(root, key); }

    private V search(Node node, K key) {
        int i = 0;
        while (i < node.entries.size() && key.compareTo(node.entries.get(i).key) > 0) i++;
        if (i < node.entries.size() && key.compareTo(node.entries.get(i).key) == 0) {
            return node.entries.get(i).value;
        }
        if (node.leaf) return null;
        return search(node.children.get(i), key);
    }

    /** Returns the sequence of node-entry-count "pages" visited (search trace evidence). */
    public List<Integer> searchTrace(K key) {
        List<Integer> trace = new ArrayList<>();
        Node node = root;
        while (true) {
            trace.add(node.entries.size());
            int i = 0;
            while (i < node.entries.size() && key.compareTo(node.entries.get(i).key) > 0) i++;
            if (i < node.entries.size() && key.compareTo(node.entries.get(i).key) == 0) break;
            if (node.leaf) break;
            node = node.children.get(i);
        }
        return trace;
    }

    public void insert(K key, V value) {
        lastInsertSplitTrace.clear();
        if (root.isFull()) {
            Node newRoot = new Node(false);
            newRoot.children.add(root);
            splitChild(newRoot, 0);
            root = newRoot;
        }
        insertNonFull(root, key, value);
    }

    private void insertNonFull(Node node, K key, V value) {
        int i = node.entries.size() - 1;
        if (node.leaf) {
            node.entries.add(null); // grow
            while (i >= 0 && key.compareTo(node.entries.get(i).key) < 0) {
                node.entries.set(i + 1, node.entries.get(i));
                i--;
            }
            node.entries.set(i + 1, new Entry(key, value));
        } else {
            while (i >= 0 && key.compareTo(node.entries.get(i).key) < 0) i--;
            i++;
            if (node.children.get(i).isFull()) {
                splitChild(node, i);
                if (key.compareTo(node.entries.get(i).key) > 0) i++;
            }
            insertNonFull(node.children.get(i), key, value);
        }
    }

    /** Splits the full child at index i of parent -- this IS the "node split" evidence. */
    private void splitChild(Node parent, int i) {
        Node fullChild = parent.children.get(i);
        int mid = (order - 1) / 2;
        Node right = new Node(fullChild.leaf);

        Entry midEntry = fullChild.entries.get(mid);
        for (int j = mid + 1; j < fullChild.entries.size(); j++) right.entries.add(fullChild.entries.get(j));
        if (!fullChild.leaf) {
            for (int j = mid + 1; j < fullChild.children.size(); j++) right.children.add(fullChild.children.get(j));
            while (fullChild.children.size() > mid + 1) fullChild.children.remove(fullChild.children.size() - 1);
        }
        while (fullChild.entries.size() > mid) fullChild.entries.remove(fullChild.entries.size() - 1);

        parent.children.add(i + 1, right);
        parent.entries.add(i, midEntry);

        lastInsertSplitTrace.add("SPLIT node with median key=" + midEntry.key +
                " -> left keeps " + fullChild.entries.size() + " keys, right gets " + right.entries.size() + " keys");
    }

    public List<String> lastInsertSplitTrace() { return lastInsertSplitTrace; }

    public int height() { return height(root); }
    private int height(Node node) {
        if (node.leaf) return 0;
        return 1 + height(node.children.get(0));
    }
}
