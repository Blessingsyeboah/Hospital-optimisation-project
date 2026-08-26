package com.hospitaloptimizer.pod2;

import java.util.NoSuchElementException;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class CustomLinkedListTest {

    public void testAddFirstAddLast_normalCase() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("B");
        list.addFirst("A");
        list.addLast("C");
        assertEquals("A", list.getFirst());
        assertEquals("C", list.getLast());
        assertEquals(3, list.size());
    }

    public void testInsertAfter() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("A");
        list.addLast("C");
        boolean ok = list.insertAfter("A", "B");
        assertTrue(ok, "insertAfter should find target 'A'");
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s);
        assertEquals("ABC", sb.toString());
    }

    public void testIteratorAndDescendingIterator_demo() {
        CustomLinkedList<Integer> list = new CustomLinkedList<>();
        list.addLast(1); list.addLast(2); list.addLast(3);
        StringBuilder forward = new StringBuilder();
        for (int v : list) forward.append(v);
        assertEquals("123", forward.toString());

        StringBuilder backward = new StringBuilder();
        var it = list.descendingIterator();
        while (it.hasNext()) backward.append(it.next());
        assertEquals("321", backward.toString());
    }

    public void testRemoveFirst_boundaryCase_emptyThrows() {
        CustomLinkedList<Integer> list = new CustomLinkedList<>();
        assertThrows(NoSuchElementException.class, list::removeFirst);
    }

    public void testRemoveByValue() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("X"); list.addLast("Y"); list.addLast("Z");
        assertTrue(list.remove("Y"), "should remove existing value");
        assertFalse(list.contains("Y"), "Y should no longer be present");
        assertEquals(2, list.size());
    }

    public static void main(String[] args) {
        run(CustomLinkedListTest.class);
    }
}
