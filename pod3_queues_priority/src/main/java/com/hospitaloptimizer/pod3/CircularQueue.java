package com.hospitaloptimizer.pod3;

import java.util.NoSuchElementException;

/**
 * Pod 3 -- fixed-capacity array-backed circular queue, built from scratch.
 * front/rear wrap around using modulo arithmetic instead of shifting elements.
 */
public class CircularQueue<T> {

    private final Object[] data;
    private int front;   // index of the current front element
    private int size;
    private final int capacity;

    public CircularQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.front = 0;
        this.size = 0;
    }

    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == capacity; }
    public int size() { return size; }
    public int capacity() { return capacity; }

    public void enqueue(T value) {
        if (isFull()) throw new IllegalStateException("circular queue is full (capacity " + capacity + ")");
        int rear = (front + size) % capacity;
        data[rear] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("circular queue is empty");
        T value = (T) data[front];
        data[front] = null;
        front = (front + 1) % capacity;
        size--;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("circular queue is empty");
        return (T) data[front];
    }

    /** Trace helper: returns the current front and rear index (for wrap-around evidence). */
    public int[] frontRearIndices() {
        int rear = size == 0 ? -1 : (front + size - 1) % capacity;
        return new int[]{front, rear};
    }
}
