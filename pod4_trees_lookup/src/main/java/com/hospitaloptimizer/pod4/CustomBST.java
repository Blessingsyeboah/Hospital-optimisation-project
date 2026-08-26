package com.hospitaloptimizer.pod4;

import java.util.ArrayList;
import java.util.List;

/**
 * Pod 4 -- "Trees, Fast Lookup & Planning Ahead".
 * A plain (unbalanced) Binary Search Tree, built from scratch. Used as the
 * baseline that RedBlackTree is compared against for height/search-time.
 */
public class CustomBST<K extends Comparable<K>, V> {

    private static class Node<K, V> {
        K key; V value;
        Node<K, V> left, right;
        Node(K key, V value) { this.key = key; this.value = value; }
    }

    private Node<K, V> root;
    private int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void insert(K key, V value) {
        root = insert(root, key, value);
    }

    private Node<K, V> insert(Node<K, V> node, K key, V value) {
        if (node == null) { size++; return new Node<>(key, value); }
        int c = key.compareTo(node.key);
        if (c < 0) node.left = insert(node.left, key, value);
        else if (c > 0) node.right = insert(node.right, key, value);
        else node.value = value; // update
        return node;
    }

    /** Returns the value for key, or null. Also records the search path (trace evidence). */
    public V search(K key) {
        Node<K, V> cur = root;
        while (cur != null) {
            int c = key.compareTo(cur.key);
            if (c == 0) return cur.value;
            cur = c < 0 ? cur.left : cur.right;
        }
        return null;
    }

    /** Returns the sequence of keys visited while searching for key (search-path trace). */
    public List<K> searchPath(K key) {
        List<K> path = new ArrayList<>();
        Node<K, V> cur = root;
        while (cur != null) {
            path.add(cur.key);
            int c = key.compareTo(cur.key);
            if (c == 0) break;
            cur = c < 0 ? cur.left : cur.right;
        }
        return path;
    }

    public boolean contains(K key) { return search(key) != null; }

    /** In-order traversal always yields keys in ascending sorted order. */
    public List<K> inorderKeys() {
        List<K> out = new ArrayList<>();
        inorder(root, out);
        return out;
    }

    private void inorder(Node<K, V> node, List<K> out) {
        if (node == null) return;
        inorder(node.left, out);
        out.add(node.key);
        inorder(node.right, out);
    }

    public int height() { return height(root); }

    private int height(Node<K, V> node) {
        if (node == null) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }
}
