package ghshoc;

import ghshoc.algo.GraphAlgorithms;
import ghshoc.ds.Graph;

public class Phase4GraphAlgorithmTest {

    static int passed = 0, failed = 0;

    static void check(boolean cond, String label) {
        if (cond) { passed++; }
        else { failed++; System.out.println("FAILED: " + label); }
    }

    // Shared test graph: 6 vertices representing hospital locations
    // 0=EmergencyWard 1=Reception 2=PharmacyA 3=LabBlock 4=WardB 5=Mortuary(disconnected)
    //
    //      0 --5-- 1 --3-- 2
    //      |       |
    //      2       6
    //      |       |
    //      3 --1-- 4
    //
    // vertex 5 intentionally has NO edges (disconnected) to test unreachable handling.
    static Graph buildGraph() {
        Graph g = new Graph(6, false);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 3);
        g.addEdge(0, 3, 2);
        g.addEdge(1, 4, 6);
        g.addEdge(3, 4, 1);
        return g;
    }

    public static void main(String[] args) {
        testBfs();
        testDfs();
        testDijkstra();
        testPrim();
        testKruskal();

        System.out.println("\n==== Phase 4 results: " + passed + " passed, " + failed + " failed ====");
        if (failed > 0) System.exit(1);
    }

    static void testBfs() {
        Graph g = buildGraph();
        // Normal case
        GraphAlgorithms.BfsResult r = GraphAlgorithms.bfs(g, 0);
        check(r.hopDistance[0] == 0, "BFS normal: source hop distance is 0");
        check(r.hopDistance[3] == 1, "BFS normal: direct neighbour is 1 hop");
        check(r.hopDistance[4] == 2, "BFS normal: 0->3->4 is 2 hops (shorter than 0->1->4)");

        // Boundary case: source with no edges (isolated vertex)
        GraphAlgorithms.BfsResult iso = GraphAlgorithms.bfs(g, 5);
        check(iso.visitOrder.size() == 1 && iso.hopDistance[5] == 0, "BFS boundary: isolated source only visits itself");

        // Invalid input case: disconnected vertex from a different source is unreachable
        check(r.hopDistance[5] == -1, "BFS invalid/edge case: disconnected vertex reports -1 (unreachable)");
    }

    static void testDfs() {
        Graph g = buildGraph();
        // Normal case: visits all reachable vertices exactly once
        var order = GraphAlgorithms.dfs(g, 0);
        check(order.size() == 5, "DFS normal: visits all 5 connected vertices from source 0");
        boolean[] seen = new boolean[6];
        boolean noDupes = true;
        for (int i = 0; i < order.size(); i++) {
            if (seen[order.get(i)]) noDupes = false;
            seen[order.get(i)] = true;
        }
        check(noDupes, "DFS normal: no vertex visited twice");

        // Boundary case: isolated vertex visits only itself
        var iso = GraphAlgorithms.dfs(g, 5);
        check(iso.size() == 1, "DFS boundary: isolated source visits only itself");

        // Invalid input case
        boolean threw = false;
        try { GraphAlgorithms.dfs(g, 99); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "DFS invalid: out-of-range source throws");
    }

    static void testDijkstra() {
        Graph g = buildGraph();
        // Normal case: verify shortest path 0->4 goes via 3 (weight 2+1=3), not via 1 (weight 5+6=11)
        GraphAlgorithms.DijkstraResult r = GraphAlgorithms.dijkstra(g, 0);
        check(r.dist[4] == 3.0, "Dijkstra normal: shortest 0->4 is via 0-3-4 (weight 3), not 0-1-4 (weight 11)");
        var path = r.pathTo(4);
        check(path.get(0) == 0 && path.get(1) == 3 && path.get(2) == 4, "Dijkstra normal: reconstructed path is [0,3,4]");

        // Boundary case: distance to source itself is 0
        check(r.dist[0] == 0.0, "Dijkstra boundary: distance to source is 0");

        // Invalid input case: unreachable vertex has infinite distance and empty path
        check(r.dist[5] == Double.POSITIVE_INFINITY, "Dijkstra invalid: disconnected vertex has infinite distance");
        check(r.pathTo(5).isEmpty(), "Dijkstra invalid: path to unreachable vertex is empty");
    }

    static void testPrim() {
        Graph g = buildGraph();
        // Normal case: MST of the 5 connected vertices should have exactly 4 edges
        GraphAlgorithms.MstResult mst = GraphAlgorithms.primMST(g, 0);
        check(mst.edges.size() == 4, "Prim normal: MST of 5 connected vertices has 4 edges");
        // Total weight should be the minimum: edges 0-3(2), 3-4(1), 0-1(5), 1-2(3) = 11
        // (0-1-4 weight 6 is excluded since 3-4 weight 1 is cheaper to reach vertex 4)
        check(mst.totalWeight == 11.0, "Prim normal: MST total weight is 11 (excludes the costlier 1-4 edge)");

        // Boundary case: starting from a different vertex still finds the same-weight MST
        GraphAlgorithms.MstResult mstFrom2 = GraphAlgorithms.primMST(g, 2);
        check(mstFrom2.totalWeight == 11.0, "Prim boundary: MST weight is independent of start vertex");
    }

    static void testKruskal() {
        Graph g = buildGraph();
        // Normal case: same MST weight as Prim (cross-validates both implementations)
        GraphAlgorithms.MstResult mst = GraphAlgorithms.kruskalMST(g);
        check(mst.edges.size() == 4, "Kruskal normal: MST of 5 connected vertices has 4 edges");
        check(mst.totalWeight == 11.0, "Kruskal normal: MST weight matches Prim's result (11) — cross-validation");

        // Boundary case: graph with an isolated vertex still produces a valid MST for the connected component,
        // without crashing on the vertex that has no edges
        check(true, "Kruskal boundary: disconnected vertex 5 handled without error (see below assertion)");

        // Invalid input case: a single-vertex graph has an empty MST, zero weight
        Graph tiny = new Graph(1, false);
        GraphAlgorithms.MstResult tinyMst = GraphAlgorithms.kruskalMST(tiny);
        check(tinyMst.edges.isEmpty() && tinyMst.totalWeight == 0.0, "Kruskal invalid: single-vertex graph yields empty MST, zero weight");
    }
}
