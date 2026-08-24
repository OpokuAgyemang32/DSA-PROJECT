package ghshoc.algo;

import ghshoc.ds.*;

/**
 * Graph route engine — GH-SHOC Section 7 / M7 (route & network optimisation).
 * Built entirely on the custom Graph, DisjointSet, ArrayStack, and
 * DoublyLinkedList from Phase 1/2 — no java.util.* graph utilities.
 */
public class GraphAlgorithms {

    // =====================================================================
    // BFS — shortest path in UNWEIGHTED hop count. O(V + E).
    // Use case: "how many corridor junctions away is the nearest pharmacy?"
    // =====================================================================
    public static class BfsResult {
        public final DynamicArray<Integer> visitOrder;
        public final int[] hopDistance; // -1 = unreachable
        public BfsResult(DynamicArray<Integer> visitOrder, int[] hopDistance) {
            this.visitOrder = visitOrder; this.hopDistance = hopDistance;
        }
    }

    public static BfsResult bfs(Graph g, int source) {
        int n = g.numVertices();
        boolean[] visited = new boolean[n];
        int[] hop = new int[n];
        java.util.Arrays.fill(hop, -1);
        DynamicArray<Integer> order = new DynamicArray<>();
        CircularQueue<Integer> frontier = new CircularQueue<>(n);

        visited[source] = true;
        hop[source] = 0;
        frontier.enqueue(source);

        while (!frontier.isEmpty()) {
            int u = frontier.dequeue();
            order.insert(u);
            for (Graph.Edge e : g.neighbors(u)) {
                if (!visited[e.to]) {
                    visited[e.to] = true;
                    hop[e.to] = hop[u] + 1;
                    frontier.enqueue(e.to);
                }
            }
        }
        return new BfsResult(order, hop);
    }

    // =====================================================================
    // DFS — iterative (explicit ArrayStack, no recursion call-stack risk
    // on large graphs). O(V + E).
    // Use case: reachability check / connectivity audit of the corridor network.
    // =====================================================================
    public static DynamicArray<Integer> dfs(Graph g, int source) {
        int n = g.numVertices();
        boolean[] visited = new boolean[n];
        DynamicArray<Integer> order = new DynamicArray<>();
        ArrayStack<Integer> stack = new ArrayStack<>();
        stack.push(source);

        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (visited[u]) continue;
            visited[u] = true;
            order.insert(u);
            for (Graph.Edge e : g.neighbors(u)) {
                if (!visited[e.to]) stack.push(e.to);
            }
        }
        return order;
    }

    // =====================================================================
    // Dijkstra — shortest WEIGHTED path from a single source. O((V+E) log V)
    // with the lazy-deletion binary heap below. Precondition: no negative
    // weights (already enforced by Graph.addEdge).
    // Use case: fastest route from Emergency Ward to any other location,
    // where edge weight = travelTime * roadConditionWeight.
    // =====================================================================
    public static class DijkstraResult {
        public final double[] dist;   // Double.POSITIVE_INFINITY = unreachable
        public final int[] prev;      // -1 = no predecessor (source or unreachable)
        public DijkstraResult(double[] dist, int[] prev) { this.dist = dist; this.prev = prev; }

        /** Reconstructs the path source -> target by walking prev[] backwards. Empty if unreachable. */
        public DynamicArray<Integer> pathTo(int target) {
            DynamicArray<Integer> reversed = new DynamicArray<>();
            if (dist[target] == Double.POSITIVE_INFINITY) return reversed; // unreachable
            int cur = target;
            while (cur != -1) {
                reversed.insert(0, cur); // insert at front — O(n) but path lengths are small
                cur = prev[cur];
            }
            return reversed;
        }
    }

    public static DijkstraResult dijkstra(Graph g, int source) {
        int n = g.numVertices();
        double[] dist = new double[n];
        int[] prev = new int[n];
        boolean[] settled = new boolean[n];
        java.util.Arrays.fill(dist, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(prev, -1);
        dist[source] = 0;

        LazyDoubleHeap pq = new LazyDoubleHeap();
        pq.insert(0, source);

        while (!pq.isEmpty()) {
            LazyDoubleHeap.Entry cur = pq.extractMin();
            int u = cur.vertex;
            if (settled[u]) continue; // stale entry from an earlier decrease-key-by-reinsert
            settled[u] = true;

            for (Graph.Edge e : g.neighbors(u)) {
                double newDist = dist[u] + e.weight;
                if (newDist < dist[e.to]) {
                    dist[e.to] = newDist;
                    prev[e.to] = u;
                    pq.insert(newDist, e.to); // "decrease key" via reinsertion; stale copy skipped above
                }
            }
        }
        return new DijkstraResult(dist, prev);
    }

    // =====================================================================
    // Prim's MST — grows a minimum spanning tree from a start vertex.
    // O((V+E) log V). Assumes the graph is connected and undirected.
    // Use case: minimum-cost backbone of corridors that keeps every
    // department connected (e.g. for laying a shared cable/pipe route).
    // =====================================================================
    public static class MstResult {
        public final DynamicArray<int[]> edges; // each int[]{from,to} in the MST
        public final double totalWeight;
        public MstResult(DynamicArray<int[]> edges, double totalWeight) {
            this.edges = edges; this.totalWeight = totalWeight;
        }
    }

    public static MstResult primMST(Graph g, int start) {
        int n = g.numVertices();
        boolean[] inMst = new boolean[n];
        double[] minEdgeWeight = new double[n];
        int[] connectingVertex = new int[n];
        java.util.Arrays.fill(minEdgeWeight, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(connectingVertex, -1);
        minEdgeWeight[start] = 0;

        LazyDoubleHeap pq = new LazyDoubleHeap();
        pq.insert(0, start);
        DynamicArray<int[]> mstEdges = new DynamicArray<>();
        double total = 0;

        while (!pq.isEmpty()) {
            LazyDoubleHeap.Entry cur = pq.extractMin();
            int u = cur.vertex;
            if (inMst[u]) continue;
            inMst[u] = true;
            if (connectingVertex[u] != -1) {
                mstEdges.insert(new int[]{connectingVertex[u], u});
                total += minEdgeWeight[u];
            }
            for (Graph.Edge e : g.neighbors(u)) {
                if (!inMst[e.to] && e.weight < minEdgeWeight[e.to]) {
                    minEdgeWeight[e.to] = e.weight;
                    connectingVertex[e.to] = u;
                    pq.insert(e.weight, e.to);
                }
            }
        }
        return new MstResult(mstEdges, total);
    }

    // =====================================================================
    // Kruskal's MST — sorts all edges, adds greedily unless it forms a
    // cycle (checked via DisjointSet). O(E log E). Use when the graph is
    // sparse — competitive with, and simpler than, Prim in that regime.
    // =====================================================================
    public static MstResult kruskalMST(Graph g) {
        // Collect edges once (undirected graph stores each edge twice — dedupe with from < to)
        DynamicArray<double[]> allEdges = new DynamicArray<>(); // {from, to, weight}
        for (int u = 0; u < g.numVertices(); u++) {
            for (Graph.Edge e : g.neighbors(u)) {
                if (!g.isDirected() && u > e.to) continue; // skip the mirrored duplicate
                allEdges.insert(new double[]{u, e.to, e.weight});
            }
        }
        // Sort edges by weight using a from-scratch merge sort on a parallel index array
        // (same divide-and-conquer logic as SortAlgorithms.mergeSort, adapted to sort by
        // edge weight instead of raw int value — no java.util.Arrays.sort used anywhere).
        int m = allEdges.size();
        int[] idx = new int[m];
        for (int i = 0; i < m; i++) idx[i] = i;
        mergeSortByWeight(idx, new int[m], 0, m - 1, allEdges);

        DisjointSet ds = new DisjointSet(g.numVertices());
        DynamicArray<int[]> mstEdges = new DynamicArray<>();
        double total = 0;

        for (int i = 0; i < m; i++) {
            double[] edge = allEdges.get(idx[i]);
            int from = (int) edge[0], to = (int) edge[1];
            double weight = edge[2];
            if (ds.union(from, to)) { // true only if this edge does NOT close a cycle
                mstEdges.insert(new int[]{from, to});
                total += weight;
            }
        }
        return new MstResult(mstEdges, total);
    }

    /** From-scratch merge sort of an index array, ordered by allEdges[idx][2] (the weight). */
    private static void mergeSortByWeight(int[] idx, int[] aux, int lo, int hi, DynamicArray<double[]> allEdges) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergeSortByWeight(idx, aux, lo, mid, allEdges);
        mergeSortByWeight(idx, aux, mid + 1, hi, allEdges);
        System.arraycopy(idx, lo, aux, lo, hi - lo + 1);
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) idx[k] = aux[j++];
            else if (j > hi) idx[k] = aux[i++];
            else if (allEdges.get(aux[i])[2] <= allEdges.get(aux[j])[2]) idx[k] = aux[i++];
            else idx[k] = aux[j++];
        }
    }

    // =====================================================================
    // Minimal lazy-deletion binary min-heap keyed by double priority.
    // Separate from ghshoc.ds.MinHeap (which is keyed by int) because
    // Dijkstra/Prim need continuous-valued edge weights.
    // =====================================================================
    private static class LazyDoubleHeap {
        static class Entry { double key; int vertex; Entry(double k, int v) { key = k; vertex = v; } }
        private Entry[] heap = new Entry[8];
        private int size = 0;

        boolean isEmpty() { return size == 0; }

        void insert(double key, int vertex) {
            if (size == heap.length) heap = java.util.Arrays.copyOf(heap, heap.length * 2);
            heap[size] = new Entry(key, vertex);
            siftUp(size++);
        }

        Entry extractMin() {
            Entry min = heap[0];
            size--;
            heap[0] = heap[size];
            heap[size] = null;
            if (size > 0) siftDown(0);
            return min;
        }

        private void siftUp(int i) {
            while (i > 0) {
                int p = (i - 1) / 2;
                if (heap[p].key <= heap[i].key) break;
                Entry t = heap[p]; heap[p] = heap[i]; heap[i] = t;
                i = p;
            }
        }

        private void siftDown(int i) {
            while (true) {
                int l = 2 * i + 1, r = 2 * i + 2, smallest = i;
                if (l < size && heap[l].key < heap[smallest].key) smallest = l;
                if (r < size && heap[r].key < heap[smallest].key) smallest = r;
                if (smallest == i) break;
                Entry t = heap[smallest]; heap[smallest] = heap[i]; heap[i] = t;
                i = smallest;
            }
        }
    }
}
