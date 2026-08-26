package com.hospitaloptimizer.pod3;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Pod 3 -- binary min-heap-backed priority queue, built from scratch
 * (java.util.PriorityQueue is not used). "Min" is defined by the comparator
 * supplied, so passing a reversed comparator turns it into a max-heap -- the
 * SchedulingEngine uses it as a max-heap on urgency (highest urgency first).
 */
public class CustomPriorityQueue<T> {

    private Object[] heap;
    private int size;
    private final Comparator<T> comparator;

    public CustomPriorityQueue(Comparator<T> comparator) {
        this.comparator = comparator;
        this.heap = new Object[16];
        this.size = 0;
    }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }

    public void insert(T value) {
        if (size == heap.length) grow();
        heap[size] = value;
        siftUp(size);
        size++;
    }

    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (isEmpty()) throw new NoSuchElementException("priority queue is empty");
        T top = (T) heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) siftDown(0);
        return top;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("priority queue is empty");
        return (T) heap[0];
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (comparator.compare((T) heap[i], (T) heap[parent]) < 0) {
                swap(i, parent);
                i = parent;
            } else break;
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
            if (left < size && comparator.compare((T) heap[left], (T) heap[smallest]) < 0) smallest = left;
            if (right < size && comparator.compare((T) heap[right], (T) heap[smallest]) < 0) smallest = right;
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int a, int b) {
        Object tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
    }

    private void grow() {
        Object[] bigger = new Object[heap.length * 2];
        System.arraycopy(heap, 0, bigger, 0, size);
        heap = bigger;
    }

    /** Re-heapifies from an arbitrary array (bottom-up heapify, O(n)). */
    @SuppressWarnings("unchecked")
    public static <T> CustomPriorityQueue<T> heapify(T[] values, Comparator<T> comparator) {
        CustomPriorityQueue<T> pq = new CustomPriorityQueue<>(comparator);
        pq.heap = new Object[Math.max(16, values.length)];
        System.arraycopy(values, 0, pq.heap, 0, values.length);
        pq.size = values.length;
        for (int i = values.length / 2 - 1; i >= 0; i--) {
            pq.siftDown(i);
        }
        return pq;
    }
}
