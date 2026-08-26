package com.hospitaloptimizer.pod2;

import java.util.Comparator;

/**
 * Pod 2 -- selection sort, insertion sort, merge sort and quicksort,
 * implemented from scratch (java.util.Arrays.sort is NOT used for core logic).
 *
 * Stability / in-place notes (Section 7 evidence):
 *  - selectionSort: NOT stable (long-distance swaps can reorder equal keys), in-place O(1) extra space.
 *  - insertionSort: STABLE (shifts, never swaps past an equal key), in-place O(1) extra space.
 *  - mergeSort:      STABLE (merge takes left run first on ties), NOT in-place, O(n) extra space.
 *  - quickSort:       NOT stable (partition swaps can cross equal keys), in-place O(log n) stack space.
 */
public final class SortAlgorithms {

    private SortAlgorithms() {}

    public static <T> void selectionSort(T[] arr, Comparator<T> cmp) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (cmp.compare(arr[j], arr[minIdx]) < 0) minIdx = j;
            }
            swap(arr, i, minIdx);
        }
    }

    public static <T> void insertionSort(T[] arr, Comparator<T> cmp) {
        for (int i = 1; i < arr.length; i++) {
            T key = arr[i];
            int j = i - 1;
            while (j >= 0 && cmp.compare(arr[j], key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    public static <T> void mergeSort(T[] arr, Comparator<T> cmp) {
        if (arr.length < 2) return;
        Object[] buffer = new Object[arr.length];
        mergeSortHelper(arr, buffer, 0, arr.length - 1, cmp);
    }

    @SuppressWarnings("unchecked")
    private static <T> void mergeSortHelper(T[] arr, Object[] buffer, int lo, int hi, Comparator<T> cmp) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergeSortHelper(arr, buffer, lo, mid, cmp);
        mergeSortHelper(arr, buffer, mid + 1, hi, cmp);

        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            // <= keeps it stable: ties take the LEFT run first
            if (cmp.compare(arr[i], arr[j]) <= 0) buffer[k++] = arr[i++];
            else buffer[k++] = arr[j++];
        }
        while (i <= mid) buffer[k++] = arr[i++];
        while (j <= hi) buffer[k++] = arr[j++];
        for (int m = lo; m <= hi; m++) arr[m] = (T) buffer[m];
    }

    public static <T> void quickSort(T[] arr, Comparator<T> cmp) {
        quickSortHelper(arr, 0, arr.length - 1, cmp);
    }

    private static <T> void quickSortHelper(T[] arr, int lo, int hi, Comparator<T> cmp) {
        if (lo >= hi) return;
        int p = partition(arr, lo, hi, cmp);
        quickSortHelper(arr, lo, p - 1, cmp);
        quickSortHelper(arr, p + 1, hi, cmp);
    }

    // Lomuto partition scheme, pivot = last element
    private static <T> int partition(T[] arr, int lo, int hi, Comparator<T> cmp) {
        T pivot = arr[hi];
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (cmp.compare(arr[j], pivot) <= 0) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, hi);
        return i + 1;
    }

    private static <T> void swap(T[] arr, int a, int b) {
        T tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static <T> boolean isSorted(T[] arr, Comparator<T> cmp) {
        for (int i = 1; i < arr.length; i++) {
            if (cmp.compare(arr[i - 1], arr[i]) > 0) return false;
        }
        return true;
    }
}
