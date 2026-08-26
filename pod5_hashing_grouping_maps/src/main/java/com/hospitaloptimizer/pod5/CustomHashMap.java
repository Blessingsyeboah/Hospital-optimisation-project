package com.hospitaloptimizer.pod5;

import com.hospitaloptimizer.shared.util.Params;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Pod 5 -- "Hashing, Grouping & Maps".
 * Hash table with separate chaining (java.util.HashMap is not used for core
 * logic). Initial capacity comes from Params.HASH_TABLE_SIZE (index-number
 * derived). Resizes (doubles) once the load factor exceeds 0.75, and
 * exposes collision statistics for the empirical load-factor experiment.
 */
public class CustomHashMap<K, V> {

    private static class Entry<K, V> {
        K key; V value;
        Entry<K, V> next;
        Entry(K key, V value) { this.key = key; this.value = value; }
    }

    private Entry<?, ?>[] buckets;
    private int size;
    private long totalCollisions; // cumulative count, for load-factor experiments

    public CustomHashMap() {
        this(Params.HASH_TABLE_SIZE);
    }

    public CustomHashMap(int initialCapacity) {
        buckets = new Entry<?, ?>[Math.max(4, initialCapacity)];
        size = 0;
    }

    private int indexFor(K key) {
        int h = (key == null) ? 0 : key.hashCode();
        h ^= (h >>> 16); // spread bits to reduce clustering, same trick as java.util.HashMap
        return (h & 0x7fffffff) % buckets.length;
    }

    @SuppressWarnings("unchecked")
    public void put(K key, V value) {
        if (loadFactor() > 0.75) resize();
        int idx = indexFor(key);
        Entry<K, V> head = (Entry<K, V>) buckets[idx];
        Entry<K, V> cur = head;
        int chainLength = 0;
        while (cur != null) {
            if (java.util.Objects.equals(cur.key, key)) { cur.value = value; return; }
            cur = cur.next;
            chainLength++;
        }
        if (chainLength > 0) totalCollisions++;
        Entry<K, V> node = new Entry<>(key, value);
        node.next = head;
        buckets[idx] = node;
        size++;
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        int idx = indexFor(key);
        Entry<K, V> cur = (Entry<K, V>) buckets[idx];
        while (cur != null) {
            if (java.util.Objects.equals(cur.key, key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean containsKey(K key) {
        int idx = indexFor(key);
        Entry<K, V> cur = (Entry<K, V>) buckets[idx];
        while (cur != null) {
            if (java.util.Objects.equals(cur.key, key)) return true;
            cur = cur.next;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public V remove(K key) {
        int idx = indexFor(key);
        Entry<K, V> cur = (Entry<K, V>) buckets[idx];
        Entry<K, V> prev = null;
        while (cur != null) {
            if (java.util.Objects.equals(cur.key, key)) {
                if (prev == null) buckets[idx] = cur.next;
                else prev.next = cur.next;
                size--;
                return cur.value;
            }
            prev = cur;
            cur = cur.next;
        }
        return null;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public double loadFactor() { return (double) size / buckets.length; }
    public int capacity() { return buckets.length; }

    /** Longest chain currently in the table -- collision statistics evidence. */
    @SuppressWarnings("unchecked")
    public int maxChainLength() {
        int max = 0;
        for (Entry<?, ?> b : buckets) {
            int len = 0;
            Entry<K, V> cur = (Entry<K, V>) b;
            while (cur != null) { len++; cur = cur.next; }
            max = Math.max(max, len);
        }
        return max;
    }

    public long totalCollisions() { return totalCollisions; }

    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<?, ?>[] old = buckets;
        buckets = new Entry<?, ?>[old.length * 2];
        int oldSize = size;
        size = 0;
        totalCollisions = 0;
        for (Entry<?, ?> b : old) {
            Entry<K, V> cur = (Entry<K, V>) b;
            while (cur != null) {
                put(cur.key, cur.value);
                cur = cur.next;
            }
        }
        size = oldSize;
    }

    @SuppressWarnings("unchecked")
    public List<K> keys() {
        List<K> out = new ArrayList<>();
        for (Entry<?, ?> b : buckets) {
            Entry<K, V> cur = (Entry<K, V>) b;
            while (cur != null) { out.add(cur.key); cur = cur.next; }
        }
        return out;
    }
}
