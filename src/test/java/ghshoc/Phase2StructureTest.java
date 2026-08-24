package ghshoc;

import ghshoc.ds.*;

public class Phase2StructureTest {

    static int passed = 0, failed = 0;

    static void check(boolean cond, String label) {
        if (cond) { passed++; }
        else { failed++; System.out.println("FAILED: " + label); }
    }

    public static void main(String[] args) {
        testMinHeap();
        testBST();
        testRedBlackTree();
        testHashTable();
        testDisjointSet();
        testGraph();

        System.out.println("\n==== Phase 2 results: " + passed + " passed, " + failed + " failed ====");
        if (failed > 0) System.exit(1);
    }

    static void testMinHeap() {
        // Normal case
        MinHeap<String> h = new MinHeap<>();
        h.insert(5, "flu-followup");        // urgency 5 (low priority key)
        h.insert(1, "cardiac-arrest");      // priority key 1 = most urgent
        h.insert(3, "fracture");
        check(h.extractMin().equals("cardiac-arrest"), "MinHeap normal: most urgent served first");
        check(h.extractMin().equals("fracture"), "MinHeap normal: second most urgent next");

        // Boundary case: single element
        MinHeap<Integer> single = new MinHeap<>();
        single.insert(10, 100);
        check(single.extractMin() == 100 && single.isEmpty(), "MinHeap boundary: single element drains to empty");

        // Invalid input case
        boolean threw = false;
        try { single.extractMin(); } catch (java.util.NoSuchElementException e) { threw = true; }
        check(threw, "MinHeap invalid: extractMin on empty throws");

        // heapify O(n) build
        @SuppressWarnings("unchecked")
        MinHeap.Entry<String>[] entries = new MinHeap.Entry[]{
            new MinHeap.Entry<>(4, "d"), new MinHeap.Entry<>(2, "b"), new MinHeap.Entry<>(1, "a")
        };
        MinHeap<String> built = MinHeap.heapify(entries);
        check(built.extractMin().equals("a"), "MinHeap heapify: min bubbles to top");
    }

    static void testBST() {
        // Normal case
        BST<Integer, String> t = new BST<>();
        t.insert(50, "R101"); t.insert(30, "R102"); t.insert(70, "R103"); t.insert(20, "R104"); t.insert(40, "R105");
        check(t.search(30).equals("R102"), "BST normal search hit");
        check(t.search(999) == null, "BST normal search miss returns null");
        check(t.inOrderKeys().toString().equals("[20, 30, 40, 50, 70]"), "BST in-order traversal sorted");

        // delete leaf
        t.delete(20);
        check(!t.contains(20), "BST delete leaf case");
        // delete node with one child (40 is now... actually 30 has one child 40 after 20 removed)
        t.delete(30);
        check(!t.contains(30) && t.contains(40), "BST delete one-child case preserves child");
        // delete node with two children (50 has children on both sides: 40-ish and 70)
        t.delete(50);
        check(!t.contains(50) && t.contains(40) && t.contains(70), "BST delete two-children case (in-order successor)");

        // Boundary case: single-node tree deleted -> empty
        BST<Integer, String> single = new BST<>();
        single.insert(1, "only");
        single.delete(1);
        check(single.isEmpty(), "BST boundary: deleting only node empties tree");

        // Invalid input case
        check(single.search(1) == null, "BST invalid: search on empty tree returns null, no crash");
    }

    static void testRedBlackTree() {
        // Normal case
        RedBlackTree<Integer, String> rb = new RedBlackTree<>();
        for (int i = 1; i <= 15; i++) rb.insert(i, "v" + i); // ascending insert: worst case for plain BST
        check(rb.contains(7) && rb.contains(15), "RedBlackTree normal: search finds inserted keys");

        // Boundary: height must stay O(log n) even for sorted insertion order (this is the whole point of the tree)
        BST<Integer, String> plainBst = new BST<>();
        for (int i = 1; i <= 15; i++) plainBst.insert(i, "v" + i);
        check(rb.height() <= 2 * (Math.log(16) / Math.log(2)),
                "RedBlackTree boundary: height bounded (~" + rb.height() + ") vs degenerate plain BST height (" + plainBst.height() + ")");
        check(plainBst.height() == 14, "Plain BST sanity: degenerates to a linked list under sorted insert (height 14)");

        // Invalid input case
        check(rb.search(999) == null, "RedBlackTree invalid: search miss returns null, no crash");
    }

    static void testHashTable() {
        // Normal case
        HashTable<String, Integer> ht = new HashTable<>();
        ht.put("Emergency Ward", 1);
        ht.put("Pharmacy Block A", 2);
        ht.put("Emergency Ward", 99); // overwrite
        check(ht.get("Emergency Ward") == 99, "HashTable normal: overwrite existing key");
        check(ht.get("Pharmacy Block A") == 2, "HashTable normal: distinct key retained");
        check(ht.size() == 2, "HashTable normal: size counts unique keys only");

        // Boundary: trigger resize (load factor > 0.75 on default 16 buckets -> resize at 13th insert)
        HashTable<Integer, Integer> big = new HashTable<>();
        for (int i = 0; i < 100; i++) big.put(i, i * i);
        boolean allPresent = true;
        for (int i = 0; i < 100; i++) if (big.get(i) != i * i) allPresent = false;
        check(allPresent && big.bucketCount() > 16, "HashTable boundary: survives multiple resizes with all entries intact");

        // Invalid input case
        check(ht.get("does-not-exist") == null, "HashTable invalid: get on missing key returns null");
        check(!ht.remove("does-not-exist"), "HashTable invalid: remove on missing key returns false, no crash");
    }

    static void testDisjointSet() {
        // Normal case
        DisjointSet ds = new DisjointSet(6);
        check(ds.numSets() == 6, "DisjointSet normal: starts with n singleton sets");
        ds.union(0, 1);
        ds.union(1, 2);
        check(ds.connected(0, 2), "DisjointSet normal: transitive union connects 0 and 2");
        check(ds.numSets() == 4, "DisjointSet normal: two unions reduce set count by 2");

        // Boundary case: unioning already-connected elements is a no-op (cycle detection for Kruskal)
        boolean merged = ds.union(0, 2);
        check(!merged && ds.numSets() == 4, "DisjointSet boundary: union within same set detects cycle, no merge");

        // Invalid input case
        boolean threw = false;
        try { ds.find(99); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "DisjointSet invalid: find out-of-range index throws");
    }

    static void testGraph() {
        // Normal case: undirected weighted graph
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 5.0);
        g.addEdge(1, 2, 3.0);
        g.addEdge(2, 3, 2.0);
        int neighborCount = 0;
        for (Graph.Edge e : g.neighbors(1)) neighborCount++;
        check(neighborCount == 2, "Graph normal: undirected edge visible from both endpoints");
        check(g.numEdges() == 3, "Graph normal: edge count tracked");

        // Boundary case: vertex with no edges
        Graph isolated = new Graph(3, true);
        int isolatedNeighbors = 0;
        for (Graph.Edge e : isolated.neighbors(0)) isolatedNeighbors++;
        check(isolatedNeighbors == 0, "Graph boundary: isolated vertex has no neighbors");

        // Invalid input case
        boolean threw = false;
        try { g.addEdge(0, 99, 1.0); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "Graph invalid: edge to out-of-range vertex throws");

        boolean threwNeg = false;
        try { g.addEdge(0, 1, -5.0); } catch (IllegalArgumentException e) { threwNeg = true; }
        check(threwNeg, "Graph invalid: negative weight rejected (Dijkstra precondition)");
    }
}
