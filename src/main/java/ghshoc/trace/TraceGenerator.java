package ghshoc.trace;

/** One-off utility to print verified step-by-step traces for the report's trace tables. Not part of the core library. */
public class TraceGenerator {

    public static void main(String[] args) {
        System.out.println("=== Binary Search trace: array=[10,20,30,40,50,60,70], target=60 ===");
        int[] sorted = {10, 20, 30, 40, 50, 60, 70};
        int target = 60;
        int lo = 0, hi = sorted.length - 1, step = 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            System.out.printf("Step %d: lo=%d hi=%d mid=%d arr[mid]=%d -> %s%n",
                step, lo, hi, mid, sorted[mid],
                sorted[mid] == target ? "FOUND" : (sorted[mid] < target ? "target > arr[mid], lo=mid+1" : "target < arr[mid], hi=mid-1"));
            if (sorted[mid] == target) break;
            if (sorted[mid] < target) lo = mid + 1; else hi = mid - 1;
            step++;
        }

        System.out.println("\n=== Insertion Sort trace: array=[8,3,7,4,2] ===");
        int[] a = {8, 3, 7, 4, 2};
        System.out.println("Initial: " + java.util.Arrays.toString(a));
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
            System.out.printf("After i=%d (key=%d): %s%n", i, key, java.util.Arrays.toString(a));
        }

        System.out.println("\n=== Merge Sort trace: array=[38,27,43,3] (single merge level shown) ===");
        int[] m = {38, 27, 43, 3};
        System.out.println("Split: [38,27] and [43,3]");
        System.out.println("Sort left half  [38,27] -> [27,38]");
        System.out.println("Sort right half [43,3]  -> [3,43]");
        System.out.println("Merge [27,38] and [3,43]:");
        int[] left = {27, 38}, right = {3, 43};
        int i = 0, j = 0, k = 0;
        int[] result = new int[4];
        while (i < left.length && j < right.length) {
            System.out.printf("  compare left[%d]=%d vs right[%d]=%d -> take %d%n", i, left[i], j, right[j], Math.min(left[i], right[j]));
            if (left[i] <= right[j]) result[k++] = left[i++]; else result[k++] = right[j++];
        }
        while (i < left.length) result[k++] = left[i++];
        while (j < right.length) result[k++] = right[j++];
        System.out.println("Merged result: " + java.util.Arrays.toString(result));
    }
}
