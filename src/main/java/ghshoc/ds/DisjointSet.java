package ghshoc.ds;

/**
 * Custom Disjoint Set (Union-Find) — GH-SHOC Section 6.
 * Path compression + union by rank gives near-O(1) amortised operations
 * (inverse-Ackermann). Used by Kruskal's MST algorithm (M7 route/network
 * optimisation) to detect cycles when adding roads.
 */
public class DisjointSet {

    private final int[] parent;
    private final int[] rank;
    private int numSets;

    public DisjointSet(int n) {
        if (n < 1) throw new IllegalArgumentException("n must be >= 1");
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        numSets = n;
    }

    private void checkBounds(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("element " + x + " out of range [0," + parent.length + ")");
        }
    }

    /** Find the representative (root) of x's set, with path compression. */
    public int find(int x) {
        checkBounds(x);
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // path compression
        }
        return parent[x];
    }

    /** Union the sets containing x and y. Returns true if they were different sets (a merge happened). */
    public boolean union(int x, int y) {
        int rootX = find(x), rootY = find(y);
        if (rootX == rootY) return false; // already in the same set -> would form a cycle

        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
        numSets--;
        return true;
    }

    public boolean connected(int x, int y) { return find(x) == find(y); }

    public int numSets() { return numSets; }
}
