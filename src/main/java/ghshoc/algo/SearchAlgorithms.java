package ghshoc.algo;

/**
 * Search algorithms — GH-SHOC Section 7 (Search & sort).
 * Each method returns a Result carrying the index found (-1 if absent)
 * plus a comparisonCount, so Phase 6's empirical harness can plot
 * comparisons vs input size without re-instrumenting later.
 */
public class SearchAlgorithms {

    public static class Result {
        public final int index;
        public final long comparisons;
        public Result(int index, long comparisons) { this.index = index; this.comparisons = comparisons; }
        @Override public String toString() { return "index=" + index + ", comparisons=" + comparisons; }
    }

    /** Linear search — O(n) worst/average, O(1) best. No precondition on ordering. */
    public static Result linearSearch(int[] arr, int target) {
        long comparisons = 0;
        for (int i = 0; i < arr.length; i++) {
            comparisons++;
            if (arr[i] == target) return new Result(i, comparisons);
        }
        return new Result(-1, comparisons);
    }

    /**
     * Binary search — O(log n). Precondition: arr must be sorted ascending.
     * Iterative (avoids recursion call-stack overhead / stack-depth limits
     * on very large inputs, which matters for the M4 empirical benchmark).
     */
    public static Result binarySearch(int[] arr, int target) {
        long comparisons = 0;
        int lo = 0, hi = arr.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2; // avoids overflow vs (lo+hi)/2
            comparisons++;
            if (arr[mid] == target) return new Result(mid, comparisons);
            comparisons++;
            if (arr[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return new Result(-1, comparisons);
    }
}
