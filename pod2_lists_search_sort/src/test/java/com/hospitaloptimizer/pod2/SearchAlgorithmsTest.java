package com.hospitaloptimizer.pod2;

import java.util.Comparator;
import static com.hospitaloptimizer.shared.util.SimpleTest.*;

public class SearchAlgorithmsTest {

    private final Comparator<Integer> cmp = Integer::compareTo;

    public void testLinearSearch_normalCase() {
        Integer[] arr = {5, 3, 9, 1, 7};
        assertEquals(2, SearchAlgorithms.linearSearch(arr, 9, cmp));
    }

    public void testLinearSearch_notFound() {
        Integer[] arr = {5, 3, 9};
        assertEquals(-1, SearchAlgorithms.linearSearch(arr, 100, cmp));
    }

    public void testBinarySearch_normalCase_sortedInput() {
        Integer[] arr = {1, 3, 5, 7, 9, 11};
        assertEquals(3, SearchAlgorithms.binarySearch(arr, 7, cmp));
    }

    public void testBinarySearch_boundaryCase_singleElement() {
        Integer[] arr = {42};
        assertEquals(0, SearchAlgorithms.binarySearch(arr, 42, cmp));
        assertEquals(-1, SearchAlgorithms.binarySearch(arr, 1, cmp));
    }

    public void testBinarySearch_boundaryCase_emptyArray() {
        Integer[] arr = {};
        assertEquals(-1, SearchAlgorithms.binarySearch(arr, 1, cmp));
    }

    /** Counterexample: precondition violation. Binary search on UNSORTED input
     *  is not guaranteed to find an existing element -- this documents that limit
     *  rather than hiding it (Section 10 requirement: "invalid precondition"). */
    public void testBinarySearch_invalidPrecondition_unsortedInputMayFail() {
        Integer[] unsorted = {10, 2, 8, 1, 30, 3};
        int result = SearchAlgorithms.binarySearch(unsorted, 3, cmp);
        // 3 IS in the array (index 5) but binary search on unsorted data
        // does not reliably find it -- it returns -1 here, proving the
        // "sorted input" precondition is not optional.
        assertEquals(-1, result);
    }

    public void testBinarySearchWithTrace_comparisonCount() {
        Integer[] arr = {1, 2, 3, 4, 5, 6, 7, 8};
        int[] trace = SearchAlgorithms.binarySearchWithTrace(arr, 8, cmp);
        assertEquals(7, trace[0]); // index of 8
        assertTrue(trace[1] <= 4, "log2(8)+1 = 4 comparisons max, got " + trace[1]);
    }

    public static void main(String[] args) {
        run(SearchAlgorithmsTest.class);
    }
}
