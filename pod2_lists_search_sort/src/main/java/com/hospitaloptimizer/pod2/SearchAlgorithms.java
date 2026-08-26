package com.hospitaloptimizer.pod2;

import java.util.Comparator;

/**
 * Pod 2 -- linear and binary search, implemented from scratch.
 *
 * Preconditions:
 *  - linearSearch: none (works on unsorted arrays). O(n) worst case.
 *  - binarySearch: REQUIRES arr to already be sorted ascending by comparator.
 *    Calling it on unsorted input gives an undefined (usually wrong) result --
 *    this precondition is deliberately tested in SearchAlgorithmsTest as a
 *    counterexample ("invalid precondition").
 */
public final class SearchAlgorithms {

    private SearchAlgorithms() {}

    /** Returns index of target, or -1 if not found. O(n). */
    public static <T> int linearSearch(T[] arr, T target, Comparator<T> cmp) {
        for (int i = 0; i < arr.length; i++) {
            if (cmp.compare(arr[i], target) == 0) return i;
        }
        return -1;
    }

    /** Returns index of target in a SORTED array, or -1 if not found. O(log n). */
    public static <T> int binarySearch(T[] arr, T target, Comparator<T> cmp) {
        int lo = 0, hi = arr.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int c = cmp.compare(arr[mid], target);
            if (c == 0) return mid;
            else if (c < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    /** Same as binarySearch but returns the number of comparisons made (for trace tables / benchmarks). */
    public static <T> int[] binarySearchWithTrace(T[] arr, T target, Comparator<T> cmp) {
        int lo = 0, hi = arr.length - 1;
        int comparisons = 0;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            comparisons++;
            int c = cmp.compare(arr[mid], target);
            if (c == 0) return new int[]{mid, comparisons};
            else if (c < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return new int[]{-1, comparisons};
    }
}
