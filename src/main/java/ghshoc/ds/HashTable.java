package ghshoc.ds;

/**
 * Custom hash table — GH-SHOC Section 6 (Hash table with collision handling).
 * Separate chaining: each bucket is a DoublyLinkedList of key-value pairs.
 * Resizes (doubles) when load factor exceeds 0.75 to keep O(1) amortised
 * put/get/remove.
 */
public class HashTable<K, V> {

    private static class Pair<K, V> {
        K key; V value;
        Pair(K key, V value) { this.key = key; this.value = value; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair<?, ?> p = (Pair<?, ?>) o;
            return key == null ? p.key == null : key.equals(p.key);
        }
    }

    private DoublyLinkedList<Pair<K, V>>[] buckets;
    private int size;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    @SuppressWarnings("unchecked")
    public HashTable() {
        buckets = new DoublyLinkedList[16];
        size = 0;
    }

    private int bucketIndex(K key, int numBuckets) {
        int h = (key == null) ? 0 : key.hashCode();
        h ^= (h >>> 16); // spread bits to reduce clustering, mirrors java.util.HashMap's technique
        return Math.floorMod(h, numBuckets);
    }

    public void put(K key, V value) {
        int idx = bucketIndex(key, buckets.length);
        if (buckets[idx] == null) buckets[idx] = new DoublyLinkedList<>();

        for (Pair<K, V> p : buckets[idx]) {
            if (p.key == null ? key == null : p.key.equals(key)) {
                p.value = value; // overwrite
                return;
            }
        }
        buckets[idx].addLast(new Pair<>(key, value));
        size++;
        if ((double) size / buckets.length > LOAD_FACTOR_THRESHOLD) resize(buckets.length * 2);
    }

    public V get(K key) {
        int idx = bucketIndex(key, buckets.length);
        if (buckets[idx] == null) return null;
        for (Pair<K, V> p : buckets[idx]) {
            if (p.key == null ? key == null : p.key.equals(key)) return p.value;
        }
        return null;
    }

    public boolean containsKey(K key) {
        int idx = bucketIndex(key, buckets.length);
        if (buckets[idx] == null) return false;
        for (Pair<K, V> p : buckets[idx]) {
            if (p.key == null ? key == null : p.key.equals(key)) return true;
        }
        return false;
    }

    public boolean remove(K key) {
        int idx = bucketIndex(key, buckets.length);
        if (buckets[idx] == null) return false;
        Pair<K, V> target = null;
        for (Pair<K, V> p : buckets[idx]) {
            if (p.key == null ? key == null : p.key.equals(key)) { target = p; break; }
        }
        if (target == null) return false;
        buckets[idx].remove(target);
        size--;
        return true;
    }

    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        DoublyLinkedList<Pair<K, V>>[] old = buckets;
        buckets = new DoublyLinkedList[newCapacity];
        for (DoublyLinkedList<Pair<K, V>> bucket : old) {
            if (bucket == null) continue;
            for (Pair<K, V> p : bucket) {
                int idx = bucketIndex(p.key, newCapacity);
                if (buckets[idx] == null) buckets[idx] = new DoublyLinkedList<>();
                buckets[idx].addLast(p);
            }
        }
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public int bucketCount() { return buckets.length; }
}
