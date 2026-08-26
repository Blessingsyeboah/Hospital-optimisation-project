package com.hospitaloptimizer.pod3;

import java.util.NoSuchElementException;

/**
 * Pod 3 -- doubly linked deque, built from scratch. Supports pushing/popping
 * from both ends -- used to let an URGENT request jump the front of the
 * dispatch line while normal requests still join the rear.
 */
public class CustomDeque<T> {

    private static class Node<T> {
        T value; Node<T> prev, next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head, tail;
    private int size;

    public void addFront(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
    }

    public void addRear(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
    }

    public T removeFront() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        T value = head.value;
        head = head.next;
        if (head != null) head.prev = null; else tail = null;
        size--;
        return value;
    }

    public T removeRear() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        T value = tail.value;
        tail = tail.prev;
        if (tail != null) tail.next = null; else head = null;
        size--;
        return value;
    }

    public T peekFront() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        return head.value;
    }

    public T peekRear() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        return tail.value;
    }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }
}
