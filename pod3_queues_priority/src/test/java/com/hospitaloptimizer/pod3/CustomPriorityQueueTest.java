package com.hospitaloptimizer.pod3;

import java.util.NoSuchElementException;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class CustomPriorityQueueTest {

    public void testMinHeap_dispatchOrderTrace() {
        // dispatch order trace: insert out of order, extractMin should come out ascending
        CustomPriorityQueue<Integer> pq = new CustomPriorityQueue<>(Integer::compareTo);
        int[] insertOrder = {5, 1, 4, 2, 3};
        for (int v : insertOrder) pq.insert(v);
        StringBuilder trace = new StringBuilder();
        while (!pq.isEmpty()) trace.append(pq.extractMin());
        assertEquals("12345", trace.toString());
    }

    public void testMaxHeap_viaReversedComparator() {
        CustomPriorityQueue<Integer> pq = new CustomPriorityQueue<>((a, b) -> b - a);
        for (int v : new int[]{5, 1, 4, 2, 3}) pq.insert(v);
        assertEquals(5, pq.extractMin()); // "min" under reversed comparator == max value
    }

    public void testHeapify_bulkLoad() {
        Integer[] values = {9, 3, 7, 1, 8, 2};
        CustomPriorityQueue<Integer> pq = CustomPriorityQueue.heapify(values, Integer::compareTo);
        assertEquals(1, pq.extractMin());
        assertEquals(2, pq.extractMin());
    }

    public void testBoundaryCase_emptyThrows() {
        CustomPriorityQueue<Integer> pq = new CustomPriorityQueue<>(Integer::compareTo);
        assertThrows(NoSuchElementException.class, pq::extractMin);
    }

    public void testGrow_manyInserts() {
        CustomPriorityQueue<Integer> pq = new CustomPriorityQueue<>(Integer::compareTo);
        for (int i = 100; i > 0; i--) pq.insert(i);
        assertEquals(100, pq.size());
        assertEquals(1, pq.extractMin());
    }

    public static void main(String[] args) {
        run(CustomPriorityQueueTest.class);
    }
}
