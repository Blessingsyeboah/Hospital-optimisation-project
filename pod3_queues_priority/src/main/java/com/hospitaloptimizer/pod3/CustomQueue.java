package com.hospitaloptimizer.pod3;

import java.util.NoSuchElementException;

/** Pod 3 -- linked-node FIFO queue, built from scratch. Used for FIFO dispatch rule. */
public class CustomQueue<T> {

    private static class Node<T> {
        T value; Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> front, rear;
    private int size;

    public void enqueue(T value) {
        Node<T> node = new Node<>(value);
        if (rear == null) {
            front = rear = node;
        } else {
            rear.next = node;
            rear = node;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        T value = front.value;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return value;
    }

    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        return front.value;
    }

    public boolean isEmpty() { return size == 0; }

    public int size() { return size; }
}
