"""
plot_performance.py — turns data/performance/*.csv (produced by
ghshoc.perf.PerformanceHarness) into PNG charts for the report's
Section 9 (empirical time & space complexity analysis).

Usage: python3 plot_performance.py [data/performance] [docs/charts]
"""
import sys
import os
import pandas as pd
import matplotlib.pyplot as plt

in_dir = sys.argv[1] if len(sys.argv) > 1 else "data/performance"
out_dir = sys.argv[2] if len(sys.argv) > 2 else "docs/charts"
os.makedirs(out_dir, exist_ok=True)


def save(fig, name):
    path = os.path.join(out_dir, name)
    fig.savefig(path, dpi=130, bbox_inches="tight")
    print(f"wrote {path}")
    plt.close(fig)


# --- 1. Sort algorithm comparisons vs n ---
df = pd.read_csv(os.path.join(in_dir, "sort_timings.csv"))
random_df = df[df.inputType == "random"]

fig, ax = plt.subplots(figsize=(7, 5))
for algo in random_df.algorithm.unique():
    sub = random_df[random_df.algorithm == algo].sort_values("inputSize")
    ax.plot(sub.inputSize, sub.comparisons, marker="o", label=algo)
ax.set_xlabel("Input size (n)")
ax.set_ylabel("Comparisons")
ax.set_title("Sort algorithms: comparisons vs input size (random input)")
ax.legend()
ax.set_yscale("log")
save(fig, "sort_comparisons_vs_n.png")

fig, ax = plt.subplots(figsize=(7, 5))
for algo in random_df.algorithm.unique():
    sub = random_df[random_df.algorithm == algo].sort_values("inputSize")
    ax.plot(sub.inputSize, sub.timeNanos / 1e6, marker="o", label=algo)
ax.set_xlabel("Input size (n)")
ax.set_ylabel("Time (ms)")
ax.set_title("Sort algorithms: wall-clock time vs input size (random input)")
ax.legend()
ax.set_yscale("log")
save(fig, "sort_time_vs_n.png")

# --- 2. Insertion sort best case (sorted) vs worst/avg case (random) ---
ins = df[df.algorithm == "InsertionSort"]
fig, ax = plt.subplots(figsize=(7, 5))
for itype in ["random", "sorted"]:
    sub = ins[ins.inputType == itype].sort_values("inputSize")
    ax.plot(sub.inputSize, sub.comparisons, marker="o", label=f"InsertionSort ({itype})")
ax.set_xlabel("Input size (n)")
ax.set_ylabel("Comparisons")
ax.set_title("Insertion Sort: best case (O(n)) vs average/worst case (O(n^2))")
ax.legend()
save(fig, "insertion_sort_best_vs_worst.png")

# --- 3. Linear vs binary search ---
s = pd.read_csv(os.path.join(in_dir, "search_comparisons.csv"))
fig, ax = plt.subplots(figsize=(7, 5))
for algo in s.algorithm.unique():
    sub = s[s.algorithm == algo].sort_values("inputSize")
    ax.plot(sub.inputSize, sub.comparisons, marker="o", label=algo)
ax.set_xlabel("Input size (n)")
ax.set_ylabel("Comparisons")
ax.set_title("Linear O(n) vs Binary O(log n) search")
ax.legend()
save(fig, "search_comparisons_vs_n.png")

# --- 4. BST vs Red-Black Tree height under adversarial (sorted) insertion ---
t = pd.read_csv(os.path.join(in_dir, "tree_height.csv"))
fig, ax = plt.subplots(figsize=(7, 5))
for structure in t.structure.unique():
    sub = t[t.structure == structure].sort_values("inputSize")
    ax.plot(sub.inputSize, sub.height, marker="o", label=structure)
ax.set_xlabel("Input size (n), inserted in ascending sorted order")
ax.set_ylabel("Tree height")
ax.set_title("BST vs Red-Black Tree height (adversarial sorted-order insertion)")
ax.legend()
save(fig, "bst_vs_redblack_height.png")

# --- 5. HashTable put/get timing (should stay near-flat: O(1) amortised) ---
h = pd.read_csv(os.path.join(in_dir, "hashtable_timings.csv"))
fig, ax = plt.subplots(figsize=(7, 5))
for op in h.operation.unique():
    sub = h[h.operation == op].sort_values("inputSize")
    ax.plot(sub.inputSize, sub.timeNanos / sub.inputSize, marker="o", label=op)
ax.set_xlabel("Input size (n)")
ax.set_ylabel("Time per operation (ns)")
ax.set_title("HashTable: per-operation cost stays near-constant (O(1) amortised)")
ax.legend()
save(fig, "hashtable_per_op_cost.png")

# --- 6. Graph algorithms vs (V, E) ---
g = pd.read_csv(os.path.join(in_dir, "graph_timings.csv"))
fig, ax = plt.subplots(figsize=(7, 5))
for algo in g.algorithm.unique():
    sub = g[g.algorithm == algo].sort_values("vertices")
    ax.plot(sub.vertices, sub.timeNanos / 1e6, marker="o", label=algo)
ax.set_xlabel("Vertices (V), edges scale ~3x V")
ax.set_ylabel("Time (ms)")
ax.set_title("Graph algorithms: time vs graph size")
ax.legend()
save(fig, "graph_algorithms_vs_size.png")

print("\nAll charts written to", out_dir)
