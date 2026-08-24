package ghshoc.ds;

/**
 * Custom Binary Search Tree — GH-SHOC Section 6 (BST).
 * Keyed by Comparable key (e.g. requestId or timeSubmitted) mapped to a value.
 * Supports insert, search, delete (all 3 cases: leaf, one child, two children
 * via in-order successor), and in-order traversal.
 */
public class BST<K extends Comparable<K>, V> {

    private static class Node<K, V> {
        K key; V value;
        Node<K, V> left, right;
        Node(K key, V value) { this.key = key; this.value = value; }
    }

    private Node<K, V> root;
    private int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void insert(K key, V value) {
        root = insert(root, key, value);
    }

    private Node<K, V> insert(Node<K, V> node, K key, V value) {
        if (node == null) { size++; return new Node<>(key, value); }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) node.left = insert(node.left, key, value);
        else if (cmp > 0) node.right = insert(node.right, key, value);
        else node.value = value; // overwrite on duplicate key
        return node;
    }

    public V search(K key) {
        Node<K, V> cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp == 0) return cur.value;
            cur = cmp < 0 ? cur.left : cur.right;
        }
        return null; // not found
    }

    public boolean contains(K key) { return search(key) != null; }

    public void delete(K key) {
        root = delete(root, key);
    }

    private Node<K, V> delete(Node<K, V> node, K key) {
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if (cmp < 0) { node.left = delete(node.left, key); }
        else if (cmp > 0) { node.right = delete(node.right, key); }
        else {
            // Node found — three cases
            if (node.left == null && node.right == null) { // leaf
                size--;
                return null;
            } else if (node.left == null) { // one child (right)
                size--;
                return node.right;
            } else if (node.right == null) { // one child (left)
                size--;
                return node.left;
            } else { // two children: replace with in-order successor (min of right subtree)
                Node<K, V> successor = min(node.right);
                node.key = successor.key;
                node.value = successor.value;
                node.right = deleteMin(node.right); // successor removed here (decrements size)
            }
        }
        return node;
    }

    private Node<K, V> min(Node<K, V> node) {
        while (node.left != null) node = node.left;
        return node;
    }

    private Node<K, V> deleteMin(Node<K, V> node) {
        if (node.left == null) { size--; return node.right; }
        node.left = deleteMin(node.left);
        return node;
    }

    /** In-order traversal — yields keys in sorted order. */
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

    public int height() { return height(root); }
    private int height(Node<K, V> node) {
        if (node == null) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }
}
