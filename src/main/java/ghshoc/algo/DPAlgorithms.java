package ghshoc.algo;

import ghshoc.ds.DynamicArray;

/**
 * Dynamic programming algorithms — GH-SHOC Section 7 / M8 (optimisation engine).
 *
 * zeroOneKnapsackDP is the correct, always-optimal counterpart to
 * GreedyAlgorithms.zeroOneKnapsackGreedy — see Phase5CounterexampleTest
 * for a concrete instance where the greedy approach returns a strictly
 * worse answer than this DP solution.
 */
public class DPAlgorithms {

    public static class KnapsackResult {
        public final int maxValue;
        public final DynamicArray<Integer> chosenItemIndices; // indices into the original items array
        public KnapsackResult(int maxValue, DynamicArray<Integer> chosenItemIndices) {
            this.maxValue = maxValue; this.chosenItemIndices = chosenItemIndices;
        }
    }

    /**
     * Bottom-up 0/1 knapsack. weights and values use int here (DP table
     * indices must be integral); for continuous weights, scale to the
     * desired precision before calling. O(n * capacity) time and space.
     *
     * Correctness: dp[i][c] = max value achievable using items 0..i-1
     * with capacity c. Each item is either excluded (dp[i-1][c]) or
     * included (value[i-1] + dp[i-1][c - weight[i-1]], if it fits).
     * Because every (i, c) subproblem is solved exactly once and reused,
     * this explores the full decision space — unlike greedy, which
     * commits to each item irrevocably based on a local heuristic.
     */
    public static KnapsackResult zeroOneKnapsackDP(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int c = 0; c <= capacity; c++) {
                dp[i][c] = dp[i - 1][c]; // exclude item i-1
                if (weights[i - 1] <= c) {
                    int included = values[i - 1] + dp[i - 1][c - weights[i - 1]];
                    if (included > dp[i][c]) dp[i][c] = included;
                }
            }
        }

        // Backtrack to find which items were chosen
        DynamicArray<Integer> chosen = new DynamicArray<>();
        int c = capacity;
        for (int i = n; i > 0; i--) {
            if (dp[i][c] != dp[i - 1][c]) { // item i-1 was included
                chosen.insert(0, i - 1);
                c -= weights[i - 1];
            }
        }

        return new KnapsackResult(dp[n][capacity], chosen);
    }

    /**
     * Longest Common Subsequence — bonus DP example, useful for e.g.
     * matching a patient's drug-name entry against the pharmacy catalogue
     * despite typos/partial input. O(m*n) time and space.
     */
    public static int longestCommonSubsequence(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1] + 1;
                else dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        return dp[m][n];
    }
}
