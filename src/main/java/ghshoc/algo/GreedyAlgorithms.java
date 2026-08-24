package ghshoc.algo;

import ghshoc.ds.DynamicArray;

/**
 * Greedy algorithms — GH-SHOC Section 7 / M8 (optimisation engine).
 *
 * Two of these (activitySelection, fractionalKnapsack) are provably
 * optimal greedy algorithms (exchange-argument proofs are standard and
 * summarised in the javadoc below). The third, zeroOneKnapsackGreedy,
 * is included specifically to demonstrate that greedy is NOT always
 * optimal — see DPAlgorithms.zeroOneKnapsackDP and Phase5CounterexampleTest
 * for the required counterexample (Section 7/M8 asks for this explicitly).
 */
public class GreedyAlgorithms {

    // =====================================================================
    // Activity Selection — pick the max number of non-overlapping requests
    // a single resource (porter/ambulance) can serve in one shift.
    // Greedy rule: always take the activity that finishes earliest among
    // those still compatible. O(n log n) for the sort + O(n) selection.
    //
    // Optimality proof sketch (exchange argument): let A be the activity
    // selection algorithm's first pick (earliest finish time f1) and let
    // O be any optimal solution's first pick, with finish time f_O >= f1.
    // Swapping O's first pick for A's first pick cannot make O worse,
    // since A's activity frees at least as much room for what follows.
    // Inducting on the remainder proves greedy's choice is always safe.
    // =====================================================================
    public static class Activity {
        public final int id, start, finish;
        public Activity(int id, int start, int finish) { this.id = id; this.start = start; this.finish = finish; }
    }

    public static DynamicArray<Activity> activitySelection(Activity[] activities) {
        // Sort by finish time (from-scratch insertion sort — n is small: one shift's worth of requests)
        Activity[] sorted = activities.clone();
        for (int i = 1; i < sorted.length; i++) {
            Activity key = sorted[i];
            int j = i - 1;
            while (j >= 0 && sorted[j].finish > key.finish) { sorted[j + 1] = sorted[j]; j--; }
            sorted[j + 1] = key;
        }

        DynamicArray<Activity> selected = new DynamicArray<>();
        if (sorted.length == 0) return selected;

        selected.insert(sorted[0]);
        int lastFinish = sorted[0].finish;
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i].start >= lastFinish) {
                selected.insert(sorted[i]);
                lastFinish = sorted[i].finish;
            }
        }
        return selected;
    }

    // =====================================================================
    // Fractional Knapsack — divisible resource (e.g. bulk IV fluid litres,
    // vaccine doses measured continuously) allocated to maximise value
    // under a capacity constraint. Greedy rule: fill by descending
    // value/weight ratio, taking a fraction of the last item if needed.
    // O(n log n). Optimal for the FRACTIONAL case (proof: any non-greedy
    // allocation can be improved by shifting capacity toward the highest
    // remaining ratio item, a strict local improvement — this is the
    // property that breaks once items become indivisible, see below).
    // =====================================================================
    public static class KnapsackItem {
        public final int id; public final double weight, value;
        public KnapsackItem(int id, double weight, double value) { this.id = id; this.weight = weight; this.value = value; }
        public double ratio() { return value / weight; }
    }

    public static double fractionalKnapsack(KnapsackItem[] items, double capacity) {
        KnapsackItem[] sorted = items.clone();
        // Insertion sort descending by ratio
        for (int i = 1; i < sorted.length; i++) {
            KnapsackItem key = sorted[i];
            int j = i - 1;
            while (j >= 0 && sorted[j].ratio() < key.ratio()) { sorted[j + 1] = sorted[j]; j--; }
            sorted[j + 1] = key;
        }

        double remaining = capacity, totalValue = 0;
        for (KnapsackItem item : sorted) {
            if (remaining <= 0) break;
            double take = Math.min(item.weight, remaining);
            totalValue += take * item.ratio();
            remaining -= take;
        }
        return totalValue;
    }

    // =====================================================================
    // Naive greedy applied to the 0/1 (INDIVISIBLE) knapsack — same
    // ratio-based rule as above, but items can no longer be split.
    // This is deliberately NOT optimal. See DPAlgorithms.zeroOneKnapsackDP
    // for the correct approach and Phase5CounterexampleTest for the proof
    // by concrete example that this can return a suboptimal answer.
    // =====================================================================
    public static double zeroOneKnapsackGreedy(KnapsackItem[] items, double capacity) {
        KnapsackItem[] sorted = items.clone();
        for (int i = 1; i < sorted.length; i++) {
            KnapsackItem key = sorted[i];
            int j = i - 1;
            while (j >= 0 && sorted[j].ratio() < key.ratio()) { sorted[j + 1] = sorted[j]; j--; }
            sorted[j + 1] = key;
        }

        double remaining = capacity, totalValue = 0;
        for (KnapsackItem item : sorted) {
            if (item.weight <= remaining) { // take the WHOLE item only, or skip it entirely
                totalValue += item.value;
                remaining -= item.weight;
            }
        }
        return totalValue;
    }
}
