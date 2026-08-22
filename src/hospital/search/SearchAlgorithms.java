package hospital.search;

import hospital.model.ServiceRequest;

import java.util.Comparator;
import java.util.List;

/**
 * Role 11: Linear search and binary search, implemented from scratch
 * (no Arrays.binarySearch / Collections.binarySearch / List.indexOf).
 *
 * Two layers are provided:
 *   1. Generic versions that work over any Comparable type / any Comparator -
 *      these are what the performance experiments (100..10,000 elements) run against.
 *   2. Domain-specific convenience methods over List<ServiceRequest>, searching
 *      by requestId, so the rest of the team can call this module directly.
 */
public final class SearchAlgorithms {

    private SearchAlgorithms() {
        // utility class
    }

    // ------------------------------------------------------------------
    // Generic linear search
    // ------------------------------------------------------------------

    /**
     * Scans left to right. No precondition on ordering.
     * Preconditions: arr != null, target may be null only if T allows null equality.
     * Returns the index of the first match, or -1 if not found.
     * Time: O(n) worst/average case, O(1) best case (target is arr[0]).
     */
    public static <T> int linearSearch(T[] arr, T target) {
        if (arr == null) {
            throw new IllegalArgumentException("arr must not be null");
        }
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null ? target == null : arr[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // Generic binary search
    // ------------------------------------------------------------------

    /**
     * Requires arr to be sorted in ascending order according to the given comparator.
     * This precondition is checked defensively (O(n) scan) and an
     * IllegalStateException is thrown if it does not hold - this is the
     * "unsorted binary search input" counterexample/edge case the brief asks for.
     *
     * Time: O(log n) once the precondition holds. The precondition check itself
     * is O(n); in the performance experiment we sort once and search many times
     * so the check cost is amortised and does not appear in the timed region.
     */
    public static <T> int binarySearch(T[] arr, T target, Comparator<T> comparator) {
        if (arr == null || comparator == null) {
            throw new IllegalArgumentException("arr and comparator must not be null");
        }
        if (!isSortedAscending(arr, comparator)) {
            throw new IllegalStateException(
                    "binarySearch precondition violated: input array is not sorted ascending");
        }
        return binarySearchUnchecked(arr, target, comparator);
    }

    /**
     * Same algorithm as binarySearch, but skips the O(n) precondition check.
     * Only call this when the caller has already guaranteed the array is sorted
     * (e.g. inside the timed loop of a performance experiment, after sorting once).
     */
    public static <T> int binarySearchUnchecked(T[] arr, T target, Comparator<T> comparator) {
        int low = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2; // avoids overflow vs (low + high) / 2
            int cmp = comparator.compare(arr[mid], target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    public static <T> boolean isSortedAscending(T[] arr, Comparator<T> comparator) {
        for (int i = 1; i < arr.length; i++) {
            if (comparator.compare(arr[i - 1], arr[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Domain-specific convenience methods over ServiceRequest
    // ------------------------------------------------------------------

    /** Linear search by requestId over a list in any order. Returns null if not found. */
    public static ServiceRequest findByIdLinear(List<ServiceRequest> requests, int requestId) {
        for (ServiceRequest r : requests) {
            if (r.getRequestId() == requestId) {
                return r;
            }
        }
        return null;
    }

    /**
     * Binary search by requestId. Precondition: requests must already be sorted
     * ascending by requestId (e.g. via SortAlgorithms.insertionSortByRequestId,
     * or the team's chosen sort). Throws IllegalStateException if not sorted.
     * Returns null if not found.
     */
    public static ServiceRequest findByIdBinary(List<ServiceRequest> requests, int requestId) {
        Comparator<ServiceRequest> byId = Comparator.comparingInt(ServiceRequest::getRequestId);
        ServiceRequest[] arr = requests.toArray(new ServiceRequest[0]);

        // Build a probe object is awkward for a record with many fields, so search
        // directly with a small inline variant of binary search on requestId.
        if (!isSortedAscending(arr, byId)) {
            throw new IllegalStateException(
                    "findByIdBinary precondition violated: requests are not sorted by requestId");
        }

        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int midId = arr[mid].getRequestId();
            if (midId == requestId) {
                return arr[mid];
            } else if (midId < requestId) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return null;
    }
}
