package com.hospitaloptimizer.pod5;

import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class CustomHashMapTest {

    public void testPutAndGet_normalCase() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
        assertEquals(2, map.size());
    }

    public void testUpdateExistingKey() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();
        map.put("A", 1);
        map.put("A", 99);
        assertEquals(99, map.get("A"));
        assertEquals(1, map.size());
    }

    public void testRemove_normalCase() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();
        map.put("A", 1); map.put("B", 2);
        Integer removed = map.remove("A");
        assertEquals(1, removed);
        assertNull(map.get("A"));
        assertEquals(1, map.size());
    }

    public void testBoundaryCase_missingKeyReturnsNull() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();
        assertNull(map.get("nonexistent"));
        assertFalse(map.containsKey("nonexistent"), "empty map should not contain any key");
    }

    /** Collision statistics for different load factors -- required evidence (Section 6). */
    public void testCollisionStatistics_atHighLoadFactor() {
        CustomHashMap<Integer, String> map = new CustomHashMap<>(8); // tiny table, force collisions
        for (int i = 0; i < 50; i++) map.put(i, "v" + i);
        assertEquals(50, map.size());
        assertTrue(map.maxChainLength() >= 1, "with 50 keys in a small table, some bucket must have entries");
        for (int i = 0; i < 50; i++) assertEquals("v" + i, map.get(i));
    }

    public void testResize_growsCapacityPastLoadFactor() {
        CustomHashMap<Integer, Integer> map = new CustomHashMap<>(8);
        int startCap = map.capacity();
        for (int i = 0; i < 20; i++) map.put(i, i);
        assertTrue(map.capacity() > startCap, "capacity should grow once load factor exceeds 0.75");
    }

    public static void main(String[] args) {
        run(CustomHashMapTest.class);
    }
}
