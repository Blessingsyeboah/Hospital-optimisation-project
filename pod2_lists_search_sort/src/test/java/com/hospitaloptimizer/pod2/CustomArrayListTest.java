package com.hospitaloptimizer.pod2;

import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class CustomArrayListTest {

    public void testAddAndGet_normalCase() {
        CustomArrayList<String> list = new CustomArrayList<>();
        list.add("Korle-Bu");
        list.add("Ridge");
        assertEquals(2, list.size());
        assertEquals("Korle-Bu", list.get(0));
        assertEquals("Ridge", list.get(1));
    }

    public void testResizeTrace_capacityGrows() {
        CustomArrayList<Integer> list = new CustomArrayList<>();
        int startCap = list.capacity();
        for (int i = 0; i < 9; i++) list.add(i); // default capacity is 8 -> must resize
        assertTrue(list.capacity() > startCap, "capacity should have grown past " + startCap);
        assertEquals(9, list.size());
    }

    public void testRemove_boundaryCase_lastElement() {
        CustomArrayList<Integer> list = new CustomArrayList<>();
        list.add(1);
        int removed = list.remove(0);
        assertEquals(1, removed);
        assertTrue(list.isEmpty(), "list should be empty after removing the only element");
    }

    public void testGet_invalidInput_throws() {
        CustomArrayList<Integer> list = new CustomArrayList<>();
        list.add(1);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
    }

    public void testInsertAndIterator() {
        CustomArrayList<String> list = new CustomArrayList<>();
        list.add("A");
        list.add("C");
        list.insert(1, "B");
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s);
        assertEquals("ABC", sb.toString());
    }

    public static void main(String[] args) {
        run(CustomArrayListTest.class);
    }
}
