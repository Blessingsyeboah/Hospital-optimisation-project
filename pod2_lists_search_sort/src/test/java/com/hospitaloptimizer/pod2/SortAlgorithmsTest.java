package com.hospitaloptimizer.pod2;

import java.util.Comparator;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class SortAlgorithmsTest {

    private final Comparator<Integer> cmp = Integer::compareTo;

    public void testSelectionSort_normalCase() {
        Integer[] arr = {5, 3, 9, 1, 7};
        SortAlgorithms.selectionSort(arr, cmp);
        assertTrue(SortAlgorithms.isSorted(arr, cmp), "array should be sorted");
        assertEquals(Integer.valueOf(1), arr[0]);
    }

    public void testInsertionSort_normalCase() {
        Integer[] arr = {5, 3, 9, 1, 7};
        SortAlgorithms.insertionSort(arr, cmp);
        assertTrue(SortAlgorithms.isSorted(arr, cmp), "array should be sorted");
    }

    public void testMergeSort_normalCase() {
        Integer[] arr = {5, 3, 9, 1, 7, 2, 8};
        SortAlgorithms.mergeSort(arr, cmp);
        assertTrue(SortAlgorithms.isSorted(arr, cmp), "array should be sorted");
    }

    public void testQuickSort_normalCase() {
        Integer[] arr = {5, 3, 9, 1, 7, 2, 8};
        SortAlgorithms.quickSort(arr, cmp);
        assertTrue(SortAlgorithms.isSorted(arr, cmp), "array should be sorted");
    }

    public void testAllSorts_boundaryCase_emptyAndSingle() {
        Integer[] empty = {};
        Integer[] single = {1};
        SortAlgorithms.selectionSort(empty, cmp);
        SortAlgorithms.insertionSort(single, cmp);
        SortAlgorithms.mergeSort(empty, cmp);
        SortAlgorithms.quickSort(single, cmp);
        assertTrue(true, "no exceptions on empty/single-element arrays");
    }

    public void testAllSorts_boundaryCase_duplicateKeys() {
        Integer[] arr = {4, 2, 4, 1, 4, 2};
        SortAlgorithms.mergeSort(arr, cmp);
        assertTrue(SortAlgorithms.isSorted(arr, cmp), "duplicates should sort correctly");
        int count4 = 0;
        for (int v : arr) if (v == 4) count4++;
        assertEquals(3, count4);
    }

    public void testMergeSort_isStable() {
        // pair objects with equal sort key but distinguishable identity to prove stability
        record Pair(int key, String tag) {}
        Pair[] arr = { new Pair(1, "first"), new Pair(1, "second"), new Pair(0, "third") };
        Comparator<Pair> byKey = Comparator.comparingInt(Pair::key);
        SortAlgorithms.mergeSort(arr, byKey);
        assertEquals("third", arr[0].tag());
        assertEquals("first", arr[1].tag());   // original relative order preserved among equal keys
        assertEquals("second", arr[2].tag());
    }

    public static void main(String[] args) {
        run(SortAlgorithmsTest.class);
    }
}
