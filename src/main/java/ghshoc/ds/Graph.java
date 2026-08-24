package ghshoc.ds;

/**
 * Custom weighted, directed-capable graph — GH-SHOC Section 6 (Graph:
 * adjacency list/matrix). Adjacency-list representation (better for the
 * sparse hospital corridor network than a matrix). Vertices are int IDs
 * 0..n-1, mapping 1:1 to locations.locationId - 1 (or an offset table you
 * keep alongside it).
 *
 * This is the shared backbone the Phase 3 algorithms (BFS, DFS, Dijkstra,
 * Prim, Kruskal) will all traverse.
 */
public class Graph {

    public static class Edge {
        public final int to;
        public final double weight;
        public Edge(int to, double weight) { this.to = to; this.weight = weight; }
    }

    private final DoublyLinkedList<Edge>[] adjacency;
    private final int numVertices;
    private final boolean directed;
    private int numEdges;

    @SuppressWarnings("unchecked")
    public Graph(int numVertices, boolean directed) {
        if (numVertices < 1) throw new IllegalArgumentException("numVertices must be >= 1");
        this.numVertices = numVertices;
        this.directed = directed;
        this.adjacency = new DoublyLinkedList[numVertices];
        for (int i = 0; i < numVertices; i++) adjacency[i] = new DoublyLinkedList<>();
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= numVertices) {
            throw new IndexOutOfBoundsException("vertex " + v + " out of range [0," + numVertices + ")");
        }
    }

    public void addEdge(int from, int to, double weight) {
        checkVertex(from); checkVertex(to);
        if (weight < 0) throw new IllegalArgumentException("negative weights unsupported (Dijkstra precondition)");
        adjacency[from].addLast(new Edge(to, weight));
        if (!directed) adjacency[to].addLast(new Edge(from, weight));
        numEdges++;
    }

    public Iterable<Edge> neighbors(int v) {
        checkVertex(v);
        return adjacency[v];
    }

    public int numVertices() { return numVertices; }
    public int numEdges() { return numEdges; }
    public boolean isDirected() { return directed; }
}
