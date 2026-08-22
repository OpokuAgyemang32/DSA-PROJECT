package hospital.experiments;

import hospital.search.SearchAlgorithms;
import hospital.sort.SortAlgorithms;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Section 9 "Required performance experiments" - search and sort comparison.
 *
 * Runs linear search vs binary search, and selection sort vs insertion sort,
 * at input sizes 100, 500, 1000, 5000, 10000, three trials each (averaged),
 * and writes the results as CSV so they can be plotted (Excel/Python/etc).
 *
 * This uses synthetic integer arrays rather than the real 300-row dataset
 * because the brief requires sizes up to 10,000 - well beyond what the team's
 * current dataset contains. Swap generateRandomArray() for a loader over the
 * real data if/when the dataset grows to cover these sizes.
 *
 * Run with:
 *   java -cp out hospital.experiments.PerformanceExperiment
 * (after compiling src/main/java into out/, see README.md)
 */
public final class PerformanceExperiment {

    private static final int[] SIZES = {100, 500, 1000, 5000, 10000};
    private static final int TRIALS = 3;
    private static final Comparator<Integer> ASCENDING = Integer::compareTo;

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of("results");
        Files.createDirectories(outDir);

        runSearchComparison(outDir.resolve("search_performance.csv"));
        runSortComparison(outDir.resolve("sort_performance.csv"));

        System.out.println("Done. CSV files written to " + outDir.toAbsolutePath());
    }

    private static void runSearchComparison(Path outFile) throws IOException {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outFile))) {
            out.println("size,algorithm,avg_time_ms");

            for (int size : SIZES) {
                Integer[] sortedData = generateSortedArray(size);
                // Search for a mix of present and absent targets, worst case first.
                Integer presentTarget = sortedData[size - 1];   // last element: linear-search worst case
                Integer absentTarget = -1;                       // guaranteed not present

                double linearMs = timeMillis(() -> {
                    SearchAlgorithms.linearSearch(sortedData, presentTarget);
                    SearchAlgorithms.linearSearch(sortedData, absentTarget);
                });

                double binaryMs = timeMillis(() -> {
                    SearchAlgorithms.binarySearchUnchecked(sortedData, presentTarget, ASCENDING);
                    SearchAlgorithms.binarySearchUnchecked(sortedData, absentTarget, ASCENDING);
                });

                out.printf("%d,linear_search,%.6f%n", size, linearMs);
                out.printf("%d,binary_search,%.6f%n", size, binaryMs);
            }
        }
    }

    private static void runSortComparison(Path outFile) throws IOException {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outFile))) {
            out.println("size,algorithm,avg_time_ms");

            for (int size : SIZES) {
                Integer[] baseData = generateRandomArray(size);

                double selectionMs = timeMillis(() -> {
                    Integer[] copy = Arrays.copyOf(baseData, baseData.length);
                    SortAlgorithms.selectionSort(copy, ASCENDING);
                });

                double insertionMs = timeMillis(() -> {
                    Integer[] copy = Arrays.copyOf(baseData, baseData.length);
                    SortAlgorithms.insertionSort(copy, ASCENDING);
                });

                out.printf("%d,selection_sort,%.6f%n", size, selectionMs);
                out.printf("%d,insertion_sort,%.6f%n", size, insertionMs);
            }
        }
    }

    /** Times {@code task} TRIALS times and returns the average duration in milliseconds. */
    private static double timeMillis(Runnable task) {
        long totalNanos = 0;
        for (int t = 0; t < TRIALS; t++) {
            long start = System.nanoTime();
            task.run();
            totalNanos += System.nanoTime() - start;
        }
        return (totalNanos / (double) TRIALS) / 1_000_000.0;
    }

    private static Integer[] generateRandomArray(int size) {
        Random random = new Random(42); // fixed seed: reproducible across runs/machines
        Integer[] arr = new Integer[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(size * 10);
        }
        return arr;
    }

    private static Integer[] generateSortedArray(int size) {
        Integer[] arr = generateRandomArray(size);
        Arrays.sort(arr); // preparing the fixture, not part of the timed algorithm
        return arr;
    }
}
