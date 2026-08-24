package ghshoc.perf;

import ghshoc.algo.*;
import ghshoc.ds.*;

import java.io.*;
import java.util.Random;

/**
 * Empirical performance harness — GH-SHOC Section 9 (time & space complexity
 * evidenced with real measurements). Runs each algorithm across increasing
 * input sizes, timing with System.nanoTime(), and writes CSV files that
 * plot_performance.py turns into charts. This is real measurement, not
 * simulated numbers — rerun it yourself to reproduce the report's figures.
 */
public class PerformanceHarness {

    static final int[] SIZES = {100, 500, 1000, 2500, 5000, 10000, 20000};
    static final Random RNG = new Random(7);

    public static void main(String[] args) throws IOException {
        String outDir = args.length > 0 ? args[0] : "data/performance";
        new File(outDir).mkdirs();

        benchmarkSorts(outDir + "/sort_timings.csv");
        benchmarkSearch(outDir + "/search_comparisons.csv");
        benchmarkTrees(outDir + "/tree_height.csv");
        benchmarkHashTable(outDir + "/hashtable_timings.csv");
        benchmarkGraph(outDir + "/graph_timings.csv");

        System.out.println("Performance CSVs written to " + outDir + "/");
    }

    static int[] randomArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = RNG.nextInt(1_000_000);
        return a;
    }

    static int[] sortedArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        return a;
    }

    // ---------------------------------------------------------------------
    static void benchmarkSorts(String path) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("algorithm,inputSize,inputType,timeNanos,comparisons");
            for (int n : SIZES) {
                if (n > 5000) continue; // O(n^2) algorithms become impractically slow beyond this — skip for bubble/insertion/selection
                timeSort(w, "BubbleSort", n, "random", randomArray(n), SortAlgorithms::bubbleSort);
                timeSort(w, "InsertionSort", n, "random", randomArray(n), SortAlgorithms::insertionSort);
                timeSort(w, "InsertionSort", n, "sorted", sortedArray(n), SortAlgorithms::insertionSort); // best case
                timeSort(w, "SelectionSort", n, "random", randomArray(n), SortAlgorithms::selectionSort);
            }
            for (int n : SIZES) {
                timeSort(w, "MergeSort", n, "random", randomArray(n), SortAlgorithms::mergeSort);
                timeSort(w, "QuickSort", n, "random", randomArray(n), SortAlgorithms::quickSort);
                timeSort(w, "QuickSort", n, "sorted", sortedArray(n), SortAlgorithms::quickSort); // random pivot avoids O(n^2)
            }
        }
    }

    interface SortFn { SortAlgorithms.Stats apply(int[] a); }

    static void timeSort(PrintWriter w, String name, int n, String inputType, int[] arr, SortFn fn) {
        long start = System.nanoTime();
        SortAlgorithms.Stats stats = fn.apply(arr);
        long elapsed = System.nanoTime() - start;
        w.printf("%s,%d,%s,%d,%d%n", name, n, inputType, elapsed, stats.comparisons);
        System.out.printf("  %-15s n=%-7d %-8s %10d ns  (%d comparisons)%n", name, n, inputType, elapsed, stats.comparisons);
    }

    // ---------------------------------------------------------------------
    static void benchmarkSearch(String path) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("algorithm,inputSize,comparisons");
            for (int n : SIZES) {
                int[] sorted = sortedArray(n);
                int target = n - 1; // worst case for linear search: last element
                w.printf("LinearSearch,%d,%d%n", n, SearchAlgorithms.linearSearch(sorted, target).comparisons);
                w.printf("BinarySearch,%d,%d%n", n, SearchAlgorithms.binarySearch(sorted, target).comparisons);
            }
        }
    }

    // ---------------------------------------------------------------------
    static void benchmarkTrees(String path) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("structure,inputSize,inputType,height,timeNanos");
            for (int n : SIZES) {
                if (n > 5000) continue; // plain BST on sorted input degenerates; keep runs fast

                BST<Integer, Integer> bstSorted = new BST<>();
                long t1 = System.nanoTime();
                for (int i = 0; i < n; i++) bstSorted.insert(i, i); // adversarial: ascending order
                long e1 = System.nanoTime() - t1;
                w.printf("BST,%d,sorted,%d,%d%n", n, bstSorted.height(), e1);

                RedBlackTree<Integer, Integer> rbSorted = new RedBlackTree<>();
                long t2 = System.nanoTime();
                for (int i = 0; i < n; i++) rbSorted.insert(i, i);
                long e2 = System.nanoTime() - t2;
                w.printf("RedBlackTree,%d,sorted,%d,%d%n", n, rbSorted.height(), e2);

                System.out.printf("  n=%-7d BST height=%-6d RedBlackTree height=%-4d (sorted-order insert)%n",
                        n, bstSorted.height(), rbSorted.height());
            }
        }
    }

    // ---------------------------------------------------------------------
    static void benchmarkHashTable(String path) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("operation,inputSize,timeNanos");
            for (int n : SIZES) {
                HashTable<Integer, Integer> ht = new HashTable<>();
                long t1 = System.nanoTime();
                for (int i = 0; i < n; i++) ht.put(i, i * i);
                long putTime = System.nanoTime() - t1;
                w.printf("put,%d,%d%n", n, putTime);

                long t2 = System.nanoTime();
                for (int i = 0; i < n; i++) ht.get(i);
                long getTime = System.nanoTime() - t2;
                w.printf("get,%d,%d%n", n, getTime);
            }
        }
    }

    // ---------------------------------------------------------------------
    static void benchmarkGraph(String path) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("algorithm,vertices,edges,timeNanos");
            for (int n : new int[]{50, 200, 500, 1000, 2000}) {
                Graph g = new Graph(n, false);
                int edgeTarget = n * 3;
                for (int i = 1; i < n; i++) g.addEdge(i, RNG.nextInt(i), 1 + RNG.nextDouble() * 10); // connectivity backbone
                for (int i = 0; i < edgeTarget - (n - 1); i++) {
                    int a = RNG.nextInt(n), b = RNG.nextInt(n);
                    if (a != b) g.addEdge(a, b, 1 + RNG.nextDouble() * 10);
                }

                long t1 = System.nanoTime();
                GraphAlgorithms.bfs(g, 0);
                w.printf("BFS,%d,%d,%d%n", n, g.numEdges(), System.nanoTime() - t1);

                long t2 = System.nanoTime();
                GraphAlgorithms.dijkstra(g, 0);
                w.printf("Dijkstra,%d,%d,%d%n", n, g.numEdges(), System.nanoTime() - t2);

                long t3 = System.nanoTime();
                GraphAlgorithms.kruskalMST(g);
                w.printf("Kruskal,%d,%d,%d%n", n, g.numEdges(), System.nanoTime() - t3);

                System.out.printf("  V=%-6d E=%-6d graph algorithms timed%n", n, g.numEdges());
            }
        }
    }
}
