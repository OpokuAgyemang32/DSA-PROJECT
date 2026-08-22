package hospital.sort;

import hospital.model.ServiceRequest;

import java.util.Comparator;
import java.util.List;

/**
 * Role 11: Selection sort and insertion sort, implemented from scratch
 * (no Arrays.sort / Collections.sort).
 *
 * Both are:
 *   - In-place: O(1) extra space, sort the array/list directly.
 *   - As implemented below, NOT stable in the selection sort case (a swap can
 *     jump an equal-urgency element past another equal-urgency element),
 *     while the insertion sort here IS stable (equal elements never swap
 *     past each other, since shifting only happens while strictly greater).
 *     This distinction is exactly what the brief's "stability and in-place
 *     discussion" evidence item asks for - see docs/trace_tables.md.
 */
public final class SortAlgorithms {

    private SortAlgorithms() {
        // utility class
    }

    // ------------------------------------------------------------------
    // Generic selection sort
    // ------------------------------------------------------------------

    /**
     * Selection sort, ascending, in-place.
     * Not stable: swapping the minimum into place can reorder equal keys.
     * Time: O(n^2) in all cases (best, average, worst) - it always scans the
     * remaining unsorted region to find the minimum, even if already sorted.
     * Space: O(1) extra.
     */
    public static <T> void selectionSort(T[] arr, Comparator<T> comparator) {
        if (arr == null || comparator == null) {
            throw new IllegalArgumentException("arr and comparator must not be null");
        }
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (comparator.compare(arr[j], arr[minIndex]) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                swap(arr, i, minIndex);
            }
        }
    }

    // ------------------------------------------------------------------
    // Generic insertion sort
    // ------------------------------------------------------------------

    /**
     * Insertion sort, ascending, in-place, stable.
     * Time: O(n) best case (already sorted - inner loop never shifts),
     *       O(n^2) average and worst case (reverse sorted).
     * Space: O(1) extra.
     */
    public static <T> void insertionSort(T[] arr, Comparator<T> comparator) {
        if (arr == null || comparator == null) {
            throw new IllegalArgumentException("arr and comparator must not be null");
        }
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            T key = arr[i];
            int j = i - 1;
            while (j >= 0 && comparator.compare(arr[j], key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    private static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    // ------------------------------------------------------------------
    // Domain-specific convenience methods over ServiceRequest
    // ------------------------------------------------------------------

    /** Sorts requests ascending by urgency (1 = lowest ... 5 = highest) using selection sort. */
    public static void selectionSortByUrgency(List<ServiceRequest> requests) {
        sortListInPlace(requests, Comparator.comparingInt(ServiceRequest::getUrgency), true);
    }

    /** Sorts requests ascending by requestId using insertion sort - needed as a binary-search precondition. */
    public static void insertionSortByRequestId(List<ServiceRequest> requests) {
        sortListInPlace(requests, Comparator.comparingInt(ServiceRequest::getRequestId), false);
    }

    /**
     * Sorts requests ascending by deadline using insertion sort. Requests with no
     * deadline (null) are treated as "latest" and pushed to the end - relevant since
     * ~ half the dataset's rows have a blank deadline field.
     */
    public static void insertionSortByDeadline(List<ServiceRequest> requests) {
        Comparator<ServiceRequest> byDeadline = Comparator.comparing(
                ServiceRequest::getDeadline,
                Comparator.nullsLast(Comparator.naturalOrder()));
        sortListInPlace(requests, byDeadline, false);
    }

    private static void sortListInPlace(List<ServiceRequest> requests,
                                         Comparator<ServiceRequest> comparator,
                                         boolean useSelectionSort) {
        ServiceRequest[] arr = requests.toArray(new ServiceRequest[0]);
        if (useSelectionSort) {
            selectionSort(arr, comparator);
        } else {
            insertionSort(arr, comparator);
        }
        for (int i = 0; i < arr.length; i++) {
            requests.set(i, arr[i]);
        }
    }
}
