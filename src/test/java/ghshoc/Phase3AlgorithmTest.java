package ghshoc;

import ghshoc.algo.SearchAlgorithms;
import ghshoc.algo.SortAlgorithms;
import java.util.Arrays;

public class Phase3AlgorithmTest {

    static int passed = 0, failed = 0;

    static void check(boolean cond, String label) {
        if (cond) { passed++; }
        else { failed++; System.out.println("FAILED: " + label); }
    }

    public static void main(String[] args) {
        testLinearSearch();
        testBinarySearch();
        testBubbleSort();
        testInsertionSort();
        testSelectionSort();
        testMergeSort();
        testQuickSort();

        System.out.println("\n==== Phase 3 results: " + passed + " passed, " + failed + " failed ====");
        if (failed > 0) System.exit(1);
    }

    static void testLinearSearch() {
        int[] arr = {40, 10, 30, 20, 50}; // unsorted — linear search doesn't need order
        // Normal case
        check(SearchAlgorithms.linearSearch(arr, 30).index == 2, "LinearSearch normal: finds mid-array target");
        // Boundary case: target is the very last element -> worst case, comparisons == length
        SearchAlgorithms.Result last = SearchAlgorithms.linearSearch(arr, 50);
        check(last.index == 4 && last.comparisons == 5, "LinearSearch boundary: last element costs n comparisons");
        // Invalid input case: target absent, empty array
        check(SearchAlgorithms.linearSearch(arr, 999).index == -1, "LinearSearch invalid: absent target returns -1");
        check(SearchAlgorithms.linearSearch(new int[0], 1).index == -1, "LinearSearch invalid: empty array returns -1, no crash");
    }

    static void testBinarySearch() {
        int[] sorted = {10, 20, 30, 40, 50, 60, 70};
        // Normal case
        check(SearchAlgorithms.binarySearch(sorted, 40).index == 3, "BinarySearch normal: finds middle element");
        // Boundary case: first and last elements
        check(SearchAlgorithms.binarySearch(sorted, 10).index == 0, "BinarySearch boundary: finds first element");
        check(SearchAlgorithms.binarySearch(sorted, 70).index == 6, "BinarySearch boundary: finds last element");
        // Invalid input case: absent target, empty array
        check(SearchAlgorithms.binarySearch(sorted, 25).index == -1, "BinarySearch invalid: absent target returns -1");
        check(SearchAlgorithms.binarySearch(new int[0], 1).index == -1, "BinarySearch invalid: empty array returns -1, no crash");
    }

    static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }

    static void testBubbleSort() {
        // Normal case
        int[] a = {5, 2, 9, 1, 5, 6};
        SortAlgorithms.bubbleSort(a);
        check(isSorted(a), "BubbleSort normal: produces sorted output");
        // Boundary: already-sorted input should early-exit after 1 pass, 0 swaps
        int[] already = {1, 2, 3, 4, 5};
        SortAlgorithms.Stats s = SortAlgorithms.bubbleSort(already);
        check(s.swaps == 0, "BubbleSort boundary: already-sorted input triggers zero swaps (best case)");
        // Invalid input: empty and single-element arrays must not crash
        SortAlgorithms.bubbleSort(new int[0]);
        SortAlgorithms.bubbleSort(new int[]{7});
        check(true, "BubbleSort invalid: empty/single-element input does not throw");
    }

    static void testInsertionSort() {
        int[] a = {8, 3, 7, 4, 2};
        SortAlgorithms.insertionSort(a);
        check(isSorted(a), "InsertionSort normal: produces sorted output");

        int[] reversed = {5, 4, 3, 2, 1}; // worst case: every element shifts
        SortAlgorithms.Stats s = SortAlgorithms.insertionSort(reversed);
        check(isSorted(reversed) && s.swaps == 10, "InsertionSort boundary: reverse-sorted input is worst case (n(n-1)/2 shifts)");

        SortAlgorithms.insertionSort(new int[0]);
        check(true, "InsertionSort invalid: empty input does not throw");
    }

    static void testSelectionSort() {
        int[] a = {29, 10, 14, 37, 13};
        SortAlgorithms.selectionSort(a);
        check(isSorted(a), "SelectionSort normal: produces sorted output");

        int[] dup = {5, 5, 5, 5};
        SortAlgorithms.selectionSort(dup);
        check(Arrays.equals(dup, new int[]{5, 5, 5, 5}), "SelectionSort boundary: all-duplicate input stays stable/correct");

        SortAlgorithms.selectionSort(new int[0]);
        check(true, "SelectionSort invalid: empty input does not throw");
    }

    static void testMergeSort() {
        int[] a = {38, 27, 43, 3, 9, 82, 10};
        SortAlgorithms.mergeSort(a);
        check(isSorted(a), "MergeSort normal: produces sorted output");
        check(Arrays.equals(a, new int[]{3, 9, 10, 27, 38, 43, 82}), "MergeSort normal: exact expected order");

        int[] single = {42};
        SortAlgorithms.mergeSort(single);
        check(Arrays.equals(single, new int[]{42}), "MergeSort boundary: single-element array unchanged");

        SortAlgorithms.mergeSort(new int[0]);
        check(true, "MergeSort invalid: empty input does not throw");
    }

    static void testQuickSort() {
        int[] a = {33, 10, 55, 71, 29, 3, 18};
        SortAlgorithms.quickSort(a);
        check(isSorted(a), "QuickSort normal: produces sorted output");

        int[] sortedInput = {1, 2, 3, 4, 5, 6, 7, 8}; // classic worst case for naive last-element pivot;
        SortAlgorithms.quickSort(sortedInput);        // random pivot here keeps it out of O(n^2) territory
        check(isSorted(sortedInput), "QuickSort boundary: already-sorted input still sorts correctly (random pivot)");

        SortAlgorithms.quickSort(new int[0]);
        SortAlgorithms.quickSort(new int[]{1});
        check(true, "QuickSort invalid: empty/single-element input does not throw");
    }
}
