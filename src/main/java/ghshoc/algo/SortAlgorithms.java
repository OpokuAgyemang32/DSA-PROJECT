package ghshoc.algo;

import java.util.Random;

/**
 * Sort algorithms — GH-SHOC Section 7 (Search & sort).
 * All operate on int[] in place (except mergeSort, which needs an
 * auxiliary array), and return a Stats object (comparisons + swaps/writes)
 * for the Phase 6 empirical harness (Section 9: time & space complexity
 * evidenced with real measurements, not just Big-O claims).
 */
public class SortAlgorithms {

    public static class Stats {
        public long comparisons;
        public long swaps; // or writes, for merge sort
        @Override public String toString() { return "comparisons=" + comparisons + ", swaps/writes=" + swaps; }
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
    }

    // ---------------- Bubble Sort: O(n^2) worst/avg, O(n) best (with early-exit) ----------------
    public static Stats bubbleSort(int[] a) {
        Stats s = new Stats();
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                s.comparisons++;
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1);
                    s.swaps++;
                    swapped = true;
                }
            }
            if (!swapped) break; // already sorted -> early exit gives the O(n) best case
        }
        return s;
    }

    // ---------------- Insertion Sort: O(n^2) worst/avg, O(n) best ----------------
    public static Stats insertionSort(int[] a) {
        Stats s = new Stats();
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0) {
                s.comparisons++;
                if (a[j] <= key) break;
                a[j + 1] = a[j];
                s.swaps++;
                j--;
            }
            a[j + 1] = key;
        }
        return s;
    }

    // ---------------- Selection Sort: O(n^2) in all cases ----------------
    public static Stats selectionSort(int[] a) {
        Stats s = new Stats();
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                s.comparisons++;
                if (a[j] < a[minIdx]) minIdx = j;
            }
            if (minIdx != i) { swap(a, i, minIdx); s.swaps++; }
        }
        return s;
    }

    // ---------------- Merge Sort: O(n log n) all cases, O(n) extra space ----------------
    public static Stats mergeSort(int[] a) {
        Stats s = new Stats();
        int[] aux = new int[a.length];
        mergeSort(a, aux, 0, a.length - 1, s);
        return s;
    }

    private static void mergeSort(int[] a, int[] aux, int lo, int hi, Stats s) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergeSort(a, aux, lo, mid, s);
        mergeSort(a, aux, mid + 1, hi, s);
        merge(a, aux, lo, mid, hi, s);
    }

    private static void merge(int[] a, int[] aux, int lo, int mid, int hi, Stats s) {
        System.arraycopy(a, lo, aux, lo, hi - lo + 1);
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) { a[k] = aux[j++]; s.swaps++; }
            else if (j > hi) { a[k] = aux[i++]; s.swaps++; }
            else {
                s.comparisons++;
                if (aux[i] <= aux[j]) { a[k] = aux[i++]; }
                else { a[k] = aux[j++]; }
                s.swaps++;
            }
        }
    }

    // ---------------- Quick Sort: O(n log n) avg, O(n^2) worst (bad pivot) ----------------
    private static final Random RNG = new Random(42); // fixed seed -> reproducible traces

    public static Stats quickSort(int[] a) {
        Stats s = new Stats();
        quickSort(a, 0, a.length - 1, s);
        return s;
    }

    private static void quickSort(int[] a, int lo, int hi, Stats s) {
        if (lo >= hi) return;
        int pivotIdx = lo + RNG.nextInt(hi - lo + 1); // random pivot avoids the classic sorted-input worst case
        swap(a, pivotIdx, hi);
        int pivot = a[hi];
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            s.comparisons++;
            if (a[j] < pivot) {
                i++;
                swap(a, i, j);
                s.swaps++;
            }
        }
        swap(a, i + 1, hi);
        s.swaps++;
        int p = i + 1;
        quickSort(a, lo, p - 1, s);
        quickSort(a, p + 1, hi, s);
    }
}
