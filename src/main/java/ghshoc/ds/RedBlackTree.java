package ghshoc.ds;

/**
 * Custom Red-Black Tree (left-leaning red-black tree, Sedgewick variant)
 * — GH-SHOC Section 6 (self-balancing tree). Guarantees O(log n) height
 * even under adversarial (sorted-order) insertion, unlike the plain BST,
 * which is exactly the empirical comparison the report's performance
 * section (M4/Section 9) is expected to demonstrate.
 */
public class RedBlackTree<K extends Comparable<K>, V> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private static class Node<K, V> {
        K key; V value;
        Node<K, V> left, right;
        boolean color;
        Node(K key, V value, boolean color) { this.key = key; this.value = value; this.color = color; }
    }

    private Node<K, V> root;
    private int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    private boolean isRed(Node<K, V> n) { return n != null && n.color == RED; }

    public void insert(K key, V value) {
        root = insert(root, key, value);
        root.color = BLACK;
    }

    private Node<K, V> insert(Node<K, V> h, K key, V value) {
        if (h == null) { size++; return new Node<>(key, value, RED); }
        int cmp = key.compareTo(h.key);
        if (cmp < 0) h.left = insert(h.left, key, value);
        else if (cmp > 0) h.right = insert(h.right, key, value);
        else h.value = value;

        if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);
        return h;
    }

    private Node<K, V> rotateLeft(Node<K, V> h) {
        Node<K, V> x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    private Node<K, V> rotateRight(Node<K, V> h) {
        Node<K, V> x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    private void flipColors(Node<K, V> h) {
        h.color = !h.color;
        h.left.color = !h.left.color;
        h.right.color = !h.right.color;
    }

    public V search(K key) {
        Node<K, V> cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp == 0) return cur.value;
            cur = cmp < 0 ? cur.left : cur.right;
        }
        return null;
    }

    public boolean contains(K key) { return search(key) != null; }

    public int height() { return height(root); }
    private int height(Node<K, V> node) {
        if (node == null) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public DynamicArray<K> inOrderKeys() {
        DynamicArray<K> out = new DynamicArray<>();
        inOrder(root, out);
        return out;
    }

    private void inOrder(Node<K, V> node, DynamicArray<K> out) {
        if (node == null) return;
        inOrder(node.left, out);
        out.insert(node.key);
        inOrder(node.right, out);
    }
}
