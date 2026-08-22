package hospital.search;

import hospital.TestRunner;

import java.util.Comparator;

public class SearchAlgorithmsTest {

    public static void main(String[] args) {
        TestRunner t = new TestRunner("SearchAlgorithmsTest");

        // ---- linear search ----
        Integer[] empty = new Integer[0];
        t.check("linear search on empty array returns -1",
                SearchAlgorithms.linearSearch(empty, 5) == -1);

        Integer[] single = {7};
        t.check("linear search finds the only element",
                SearchAlgorithms.linearSearch(single, 7) == 0);
        t.check("linear search on single-element array, not found",
                SearchAlgorithms.linearSearch(single, 99) == -1);

        Integer[] withDuplicates = {3, 1, 4, 1, 5, 9, 1};
        t.check("linear search returns FIRST index when duplicates exist",
                SearchAlgorithms.linearSearch(withDuplicates, 1) == 1);
        t.check("linear search finds last element (worst case scan)",
                SearchAlgorithms.linearSearch(withDuplicates, 9) == 5);
        t.check("linear search: target not present returns -1",
                SearchAlgorithms.linearSearch(withDuplicates, 42) == -1);

        // ---- binary search: correct usage ----
        Integer[] sorted = {1, 3, 5, 7, 9, 11, 13};
        Comparator<Integer> asc = Integer::compareTo;

        t.check("binary search finds middle element",
                SearchAlgorithms.binarySearch(sorted, 7, asc) == 3);
        t.check("binary search finds first element",
                SearchAlgorithms.binarySearch(sorted, 1, asc) == 0);
        t.check("binary search finds last element",
                SearchAlgorithms.binarySearch(sorted, 13, asc) == 6);
        t.check("binary search: target not present returns -1",
                SearchAlgorithms.binarySearch(sorted, 8, asc) == -1);

        Integer[] emptySorted = new Integer[0];
        t.check("binary search on empty array returns -1",
                SearchAlgorithms.binarySearch(emptySorted, 1, asc) == -1);

        Integer[] singleSorted = {42};
        t.check("binary search on single-element array finds it",
                SearchAlgorithms.binarySearch(singleSorted, 42, asc) == 0);
        t.check("binary search on single-element array, not found",
                SearchAlgorithms.binarySearch(singleSorted, 0, asc) == -1);

        Integer[] sortedWithDupes = {2, 2, 2, 5, 8};
        int dupeIndex = SearchAlgorithms.binarySearch(sortedWithDupes, 2, asc);
        t.check("binary search with duplicate keys returns a valid matching index",
                dupeIndex >= 0 && dupeIndex <= 2);

        // ---- binary search precondition: REQUIRED counterexample ----
        // "unsorted binary search input" - the brief explicitly requires this edge case.
        Integer[] unsorted = {5, 1, 9, 3, 7};
        t.expectThrows(
                "binary search on UNSORTED input throws IllegalStateException (documented precondition)",
                IllegalStateException.class,
                () -> SearchAlgorithms.binarySearch(unsorted, 3, asc));

        // ---- isSortedAscending helper ----
        t.check("isSortedAscending true for sorted array",
                SearchAlgorithms.isSortedAscending(sorted, asc));
        t.check("isSortedAscending false for unsorted array",
                !SearchAlgorithms.isSortedAscending(unsorted, asc));
        t.check("isSortedAscending true for empty array (vacuously sorted)",
                SearchAlgorithms.isSortedAscending(emptySorted, asc));

        t.summary();
    }
}
