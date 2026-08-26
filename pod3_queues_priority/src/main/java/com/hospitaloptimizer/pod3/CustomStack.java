package com.hospitaloptimizer.pod3;

import java.util.EmptyStackException;

/**
 * Pod 3 -- array-backed LIFO stack, built from scratch.
 * Used for the audit/undo log (see AuditLog) and can simulate recursion
 * (e.g. iterative DFS in Pod 5 could use this instead of the call stack).
 */
public class CustomStack<T> {

    private Object[] data;
    private int size;

    public CustomStack() {
        data = new Object[8];
        size = 0;
    }

    public void push(T value) {
        if (size == data.length) grow();
        data[size++] = value;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T value = (T) data[--size];
        data[size] = null;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return (T) data[size - 1];
    }

    public boolean isEmpty() { return size == 0; }

    public int size() { return size; }

    private void grow() {
        Object[] bigger = new Object[data.length * 2];
        System.arraycopy(data, 0, bigger, 0, size);
        data = bigger;
    }
}
