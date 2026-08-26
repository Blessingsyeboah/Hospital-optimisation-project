package com.hospitaloptimizer.pod4;

import java.util.ArrayList;
import java.util.List;

/**
 * Pod 4 -- Left-Leaning Red-Black Tree (Sedgewick & Wayne's simplified
 * formulation of a red-black BST). Guarantees O(log n) height via rotations
 * and recolouring performed on every insert.
 *
 * Invariants maintained:
 *  1. No node has two red links (no two reds in a row on any path).
 *  2. Every path from root to a null link has the same number of black links (perfect black balance).
 *  3. Red links lean left only.
 */
public class RedBlackTree<K extends Comparable<K>, V> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class Node {
        K key; V value;
        Node left, right;
        boolean color; // color of the link FROM the parent to this node
        int size;

        Node(K key, V value, boolean color, int size) {
            this.key = key; this.value = value; this.color = color; this.size = size;
        }
    }

    private Node root;
    /** Records every rotation performed during the most recent insert, e.g. "LEFT-ROTATE@42". */
    private final List<String> lastInsertTrace = new ArrayList<>();

    public int size() { return size(root); }
    private int size(Node x) { return x == null ? 0 : x.size; }
    public boolean isEmpty() { return root == null; }

    private boolean isRed(Node x) { return x != null && x.color == RED; }

    public V search(K key) {
        Node x = root;
        while (x != null) {
            int c = key.compareTo(x.key);
            if (c == 0) return x.value;
            x = c < 0 ? x.left : x.right;
        }
        return null;
    }

    public boolean contains(K key) { return search(key) != null; }

    public void insert(K key, V value) {
        lastInsertTrace.clear();
        root = insert(root, key, value);
        root.color = BLACK;
    }

    /** Rotations/recolouring performed by the most recent insert() call -- before/after diagram evidence. */
    public List<String> lastInsertTrace() { return lastInsertTrace; }

    private Node insert(Node h, K key, V value) {
        if (h == null) return new Node(key, value, RED, 1);

        int c = key.compareTo(h.key);
        if (c < 0) h.left = insert(h.left, key, value);
        else if (c > 0) h.right = insert(h.right, key, value);
        else h.value = value;

        // fix-up: standard LLRB balancing sequence
        if (isRed(h.right) && !isRed(h.left)) { h = rotateLeft(h); lastInsertTrace.add("LEFT-ROTATE@" + h.key); }
        if (isRed(h.left) && isRed(h.left.left)) { h = rotateRight(h); lastInsertTrace.add("RIGHT-ROTATE@" + h.key); }
        if (isRed(h.left) && isRed(h.right)) { flipColors(h); lastInsertTrace.add("FLIP-COLORS@" + h.key); }

        h.size = size(h.left) + size(h.right) + 1;
        return h;
    }

    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        x.size = h.size;
        h.size = size(h.left) + size(h.right) + 1;
        return x;
    }

    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        x.size = h.size;
        h.size = size(h.left) + size(h.right) + 1;
        return x;
    }

    private void flipColors(Node h) {
        h.color = !h.color;
        h.left.color = !h.left.color;
        h.right.color = !h.right.color;
    }

    public int height() { return height(root); }
    private int height(Node x) {
        if (x == null) return -1;
        return 1 + Math.max(height(x.left), height(x.right));
    }

    public List<K> inorderKeys() {
        List<K> out = new ArrayList<>();
        inorder(root, out);
        return out;
    }

    private void inorder(Node x, List<K> out) {
        if (x == null) return;
        inorder(x.left, out);
        out.add(x.key);
        inorder(x.right, out);
    }

    /** Verifies invariant #2 (perfect black balance) -- used by tests as a correctness proof-sketch check. */
    public boolean isBalanced() {
        int blackCount = 0;
        Node x = root;
        while (x != null) { if (!isRed(x)) blackCount++; x = x.left; }
        return isBalanced(root, blackCount);
    }

    private boolean isBalanced(Node x, int blackCount) {
        if (x == null) return blackCount == 0;
        if (!isRed(x)) blackCount--;
        return isBalanced(x.left, blackCount) && isBalanced(x.right, blackCount);
    }
}
