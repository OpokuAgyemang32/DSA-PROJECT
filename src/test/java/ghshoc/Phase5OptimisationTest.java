package ghshoc;

import ghshoc.algo.GreedyAlgorithms;
import ghshoc.algo.GreedyAlgorithms.Activity;
import ghshoc.algo.GreedyAlgorithms.KnapsackItem;
import ghshoc.algo.DPAlgorithms;

public class Phase5OptimisationTest {

    static int passed = 0, failed = 0;

    static void check(boolean cond, String label) {
        if (cond) { passed++; }
        else { failed++; System.out.println("FAILED: " + label); }
    }

    public static void main(String[] args) {
        testActivitySelection();
        testFractionalKnapsack();
        testZeroOneKnapsackDP();
        testLCS();
        testGreedyVsDpCounterexample(); // the required Section 7/M8 evidence

        System.out.println("\n==== Phase 5 results: " + passed + " passed, " + failed + " failed ====");
        if (failed > 0) System.exit(1);
    }

    static void testActivitySelection() {
        // Normal case: 6-activity instance (start,finish) pairs; verified by hand:
        // sorted by finish -> (1,4)(3,5)(0,6)(5,7)(5,9)(8,9); greedy picks (1,4)->(5,7)->(8,9) = 3 activities
        Activity[] activities = {
            new Activity(1, 1, 4), new Activity(2, 3, 5), new Activity(3, 0, 6),
            new Activity(4, 5, 7), new Activity(5, 5, 9), new Activity(6, 8, 9)
        };
        var selected = GreedyAlgorithms.activitySelection(activities);
        check(selected.size() == 3, "ActivitySelection normal: selects 3 non-overlapping requests (verified optimal count)");
        check(selected.get(0).id == 1 && selected.get(1).id == 4 && selected.get(2).id == 6,
                "ActivitySelection normal: picks earliest-finishing compatible activities in order (1,4,6)");

        // Boundary case: empty input
        var empty = GreedyAlgorithms.activitySelection(new Activity[0]);
        check(empty.isEmpty(), "ActivitySelection boundary: empty input returns empty selection, no crash");

        // Invalid/edge case: single activity always selected
        var single = GreedyAlgorithms.activitySelection(new Activity[]{new Activity(1, 2, 3)});
        check(single.size() == 1, "ActivitySelection edge case: single activity is trivially selected");
    }

    static void testFractionalKnapsack() {
        // Normal case: capacity 50, items with known optimal fractional value = 240
        // (all of items 1,2 [30kg total] + 20/30 = two-thirds of item 3, exactly filling capacity 50)
        KnapsackItem[] items = {
            new KnapsackItem(1, 10, 60), new KnapsackItem(2, 20, 100), new KnapsackItem(3, 30, 120)
        };
        double value = GreedyAlgorithms.fractionalKnapsack(items, 50);
        check(Math.abs(value - 240.0) < 1e-9, "FractionalKnapsack normal: optimal value is 240 (all of items 1,2 + all of item 3, exactly fills capacity 50)");

        // Boundary case: capacity 0 -> value 0
        check(GreedyAlgorithms.fractionalKnapsack(items, 0) == 0.0, "FractionalKnapsack boundary: zero capacity yields zero value");

        // Invalid input case: capacity larger than total weight -> takes everything
        double overCapacity = GreedyAlgorithms.fractionalKnapsack(items, 1000);
        check(overCapacity == 60 + 100 + 120, "FractionalKnapsack invalid/edge: capacity exceeding total weight takes all items fully");
    }

    static void testZeroOneKnapsackDP() {
        // Normal case
        int[] weights = {2, 3, 4, 5};
        int[] values = {3, 4, 5, 6};
        DPAlgorithms.KnapsackResult r = DPAlgorithms.zeroOneKnapsackDP(weights, values, 5);
        check(r.maxValue == 7, "ZeroOneKnapsackDP normal: optimal value for capacity 5 is 7 (items 0+1, weight 5, value 7)");

        // Boundary case: capacity 0
        DPAlgorithms.KnapsackResult zero = DPAlgorithms.zeroOneKnapsackDP(weights, values, 0);
        check(zero.maxValue == 0 && zero.chosenItemIndices.isEmpty(), "ZeroOneKnapsackDP boundary: zero capacity yields zero value, no items chosen");

        // Invalid/edge case: single item heavier than capacity is excluded
        DPAlgorithms.KnapsackResult tooHeavy = DPAlgorithms.zeroOneKnapsackDP(new int[]{10}, new int[]{100}, 5);
        check(tooHeavy.maxValue == 0, "ZeroOneKnapsackDP invalid: item exceeding capacity is correctly excluded");
    }

    static void testLCS() {
        // Normal case
        check(DPAlgorithms.longestCommonSubsequence("PARACETAMOL", "PARACETMOL") == 10,
                "LCS normal: near-identical drug names match on 10 of 11 characters");
        // Boundary case: one empty string
        check(DPAlgorithms.longestCommonSubsequence("", "ASPIRIN") == 0, "LCS boundary: empty string yields LCS length 0");
        // Invalid/edge case: no common characters at all
        check(DPAlgorithms.longestCommonSubsequence("ABC", "XYZ") == 0, "LCS edge case: completely disjoint strings yield 0");
    }

    /**
     * THE REQUIRED COUNTEREXAMPLE (Section 7 / M8): a concrete instance where
     * the ratio-greedy heuristic, applied to the 0/1 (indivisible) knapsack,
     * returns a value strictly less than the true optimum — proving greedy
     * is not always correct, and DP is needed for a provably optimal answer.
     *
     * Instance: pharmacy dispatch bin, capacity 50 (kg), three indivisible
     * equipment crates:
     *   Item 1: weight 10, value 60  -> ratio 6.0  (best ratio)
     *   Item 2: weight 20, value 100 -> ratio 5.0
     *   Item 3: weight 30, value 120 -> ratio 4.0  (worst ratio)
     *
     * Greedy (highest ratio first): take item1 (cap 50->40), take item2
     * (cap 40->20), item3 needs 30 > 20 remaining -> SKIP.
     *   Greedy total = 60 + 100 = 160, using 30 of 50 kg.
     *
     * True optimum (found by DP, and verifiable by exhaustive check since
     * there are only 2^3 = 8 subsets): items 2+3 = weight 50 (exactly fits),
     * value 100+120 = 220.
     *
     * 220 > 160, so greedy is demonstrably suboptimal on this instance.
     */
    static void testGreedyVsDpCounterexample() {
        KnapsackItem[] greedyItems = {
            new KnapsackItem(1, 10, 60), new KnapsackItem(2, 20, 100), new KnapsackItem(3, 30, 120)
        };
        int[] weights = {10, 20, 30};
        int[] values = {60, 100, 120};
        int capacity = 50;

        double greedyValue = GreedyAlgorithms.zeroOneKnapsackGreedy(greedyItems, capacity);
        DPAlgorithms.KnapsackResult dpResult = DPAlgorithms.zeroOneKnapsackDP(weights, values, capacity);

        check(greedyValue == 160.0, "Counterexample: naive greedy achieves 160 (items 1+2) on this instance");
        check(dpResult.maxValue == 220, "Counterexample: DP finds the true optimum 220 (items 2+3)");
        check(dpResult.maxValue > greedyValue,
                "Counterexample PROVEN: DP optimum (" + dpResult.maxValue + ") strictly exceeds greedy's answer (" + (int) greedyValue
                        + ") — greedy is not optimal for 0/1 knapsack, DP is required");

        // Exhaustive brute-force cross-check (2^3 = 8 subsets) confirms 220 really is the global optimum,
        // independent of trusting the DP implementation alone.
        int bruteForceBest = 0;
        for (int mask = 0; mask < 8; mask++) {
            int w = 0, v = 0;
            for (int i = 0; i < 3; i++) {
                if ((mask & (1 << i)) != 0) { w += weights[i]; v += values[i]; }
            }
            if (w <= capacity && v > bruteForceBest) bruteForceBest = v;
        }
        check(bruteForceBest == 220, "Counterexample: brute-force exhaustive search over all 8 subsets confirms 220 is the true global optimum");
    }
}
