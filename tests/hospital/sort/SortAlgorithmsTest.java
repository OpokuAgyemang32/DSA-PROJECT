package hospital.sort;

import hospital.TestRunner;

import java.util.Arrays;
import java.util.Comparator;

public class SortAlgorithmsTest {

    public static void main(String[] args) {
        TestRunner t = new TestRunner("SortAlgorithmsTest");
        Comparator<Integer> asc = Integer::compareTo;

        // ---- selection sort ----
        Integer[] empty = new Integer[0];
        SortAlgorithms.selectionSort(empty, asc);
        t.check("selection sort on empty array does not throw / stays empty",
                empty.length == 0);

        Integer[] single = {5};
        SortAlgorithms.selectionSort(single, asc);
        t.check("selection sort on single-element array is unchanged",
                Arrays.equals(single, new Integer[]{5}));

        Integer[] reversed = {5, 4, 3, 2, 1};
        SortAlgorithms.selectionSort(reversed, asc);
        t.check("selection sort correctly sorts a reversed array",
                Arrays.equals(reversed, new Integer[]{1, 2, 3, 4, 5}));

        Integer[] withDuplicates = {3, 1, 4, 1, 5, 9, 1};
        SortAlgorithms.selectionSort(withDuplicates, asc);
        t.check("selection sort correctly sorts an array with duplicate keys",
                Arrays.equals(withDuplicates, new Integer[]{1, 1, 1, 3, 4, 5, 9}));

        Integer[] alreadySorted = {1, 2, 3, 4, 5};
        SortAlgorithms.selectionSort(alreadySorted, asc);
        t.check("selection sort leaves an already-sorted array sorted",
                Arrays.equals(alreadySorted, new Integer[]{1, 2, 3, 4, 5}));

        // ---- insertion sort ----
        Integer[] emptyIns = new Integer[0];
        SortAlgorithms.insertionSort(emptyIns, asc);
        t.check("insertion sort on empty array does not throw / stays empty",
                emptyIns.length == 0);

        Integer[] singleIns = {5};
        SortAlgorithms.insertionSort(singleIns, asc);
        t.check("insertion sort on single-element array is unchanged",
                Arrays.equals(singleIns, new Integer[]{5}));

        Integer[] reversedIns = {5, 4, 3, 2, 1};
        SortAlgorithms.insertionSort(reversedIns, asc);
        t.check("insertion sort correctly sorts a reversed array (its worst case)",
                Arrays.equals(reversedIns, new Integer[]{1, 2, 3, 4, 5}));

        Integer[] withDuplicatesIns = {3, 1, 4, 1, 5, 9, 1};
        SortAlgorithms.insertionSort(withDuplicatesIns, asc);
        t.check("insertion sort correctly sorts an array with duplicate keys",
                Arrays.equals(withDuplicatesIns, new Integer[]{1, 1, 1, 3, 4, 5, 9}));

        Integer[] alreadySortedIns = {1, 2, 3, 4, 5};
        SortAlgorithms.insertionSort(alreadySortedIns, asc);
        t.check("insertion sort leaves an already-sorted array sorted (its best case)",
                Arrays.equals(alreadySortedIns, new Integer[]{1, 2, 3, 4, 5}));

        // ---- stability check ----
        // Pair each key with an original-order tag; a stable sort must keep equal
        // keys in their original relative order after sorting.
        String[][] taggedData = {
                {"2", "first"}, {"1", "a"}, {"2", "second"}, {"1", "b"}
        };
        Comparator<String[]> byKey = Comparator.comparing(pair -> Integer.parseInt(pair[0]));

        String[][] insertionInput = taggedData.clone();
        SortAlgorithms.insertionSort(insertionInput, byKey);
        boolean insertionStable =
                insertionInput[0][1].equals("a") && insertionInput[1][1].equals("b")
                        && insertionInput[2][1].equals("first") && insertionInput[3][1].equals("second");
        t.check("insertion sort is stable (equal keys keep original relative order)",
                insertionStable);

        t.summary();
    }
}
