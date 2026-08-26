package com.hospitaloptimizer.pod2;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Pod 2 -- Lists & Search/Sort.
 * A doubly linked list built from scratch: addFirst, addLast, insertAfter,
 * remove, and a working Iterator (the "iterator demo" evidence).
 */
public class CustomLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        T value;
        Node<T> prev, next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void addFirst(T value) {
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

    public void addLast(T value) {
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

    /** Inserts newValue immediately after the first node holding target. */
    public boolean insertAfter(T target, T newValue) {
        Node<T> cur = head;
        while (cur != null) {
            if (java.util.Objects.equals(cur.value, target)) {
                Node<T> node = new Node<>(newValue);
                node.prev = cur;
                node.next = cur.next;
                if (cur.next != null) cur.next.prev = node;
                else tail = node;
                cur.next = node;
                size++;
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    public T removeFirst() {
        if (head == null) throw new NoSuchElementException("list is empty");
        T value = head.value;
        head = head.next;
        if (head != null) head.prev = null;
        else tail = null;
        size--;
        return value;
    }

    public T removeLast() {
        if (tail == null) throw new NoSuchElementException("list is empty");
        T value = tail.value;
        tail = tail.prev;
        if (tail != null) tail.next = null;
        else head = null;
        size--;
        return value;
    }

    public boolean remove(T value) {
        Node<T> cur = head;
        while (cur != null) {
            if (java.util.Objects.equals(cur.value, value)) {
                if (cur.prev != null) cur.prev.next = cur.next;
                else head = cur.next;
                if (cur.next != null) cur.next.prev = cur.prev;
                else tail = cur.prev;
                size--;
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    public boolean contains(T value) {
        Node<T> cur = head;
        while (cur != null) {
            if (java.util.Objects.equals(cur.value, value)) return true;
            cur = cur.next;
        }
        return false;
    }

    public T getFirst() {
        if (head == null) throw new NoSuchElementException("list is empty");
        return head.value;
    }

    public T getLast() {
        if (tail == null) throw new NoSuchElementException("list is empty");
        return tail.value;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> cursor = head;

            @Override
            public boolean hasNext() { return cursor != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T value = cursor.value;
                cursor = cursor.next;
                return value;
            }
        };
    }

    /** Iterate backwards -- extra evidence the doubly-linked structure works both ways. */
    public Iterator<T> descendingIterator() {
        return new Iterator<T>() {
            private Node<T> cursor = tail;

            @Override
            public boolean hasNext() { return cursor != null; }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T value = cursor.value;
                cursor = cursor.prev;
                return value;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> cur = head;
        while (cur != null) {
            sb.append(cur.value);
            if (cur.next != null) sb.append(" <-> ");
            cur = cur.next;
        }
        return sb.append("]").toString();
    }
}
