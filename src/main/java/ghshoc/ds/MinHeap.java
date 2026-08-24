package ghshoc.ds;

import java.util.NoSuchElementException;

/**
 * Custom binary heap / priority queue — GH-SHOC Section 6 (Priority queue / heap).
 * Array-backed min-heap (smallest key = highest priority, e.g. urgency
 * mapped so 5=CARDIAC becomes key -5, or a wait-time key for FIFO-within-priority).
 *
 * insert: O(log n) sift-up.  extractMin: O(log n) sift-down.  heapify: O(n).
 */
public class MinHeap<T> {

    public static class Entry<T> {
        public final int priority; // lower = served first
        public final T value;
        public Entry(int priority, T value) { this.priority = priority; this.value = value; }
        @Override public String toString() { return "(" + priority + "," + value + ")"; }
    }

    private Entry<?>[] heap;
    private int size;

    @SuppressWarnings("unchecked")
    public MinHeap() {
        heap = new Entry<?>[8];
        size = 0;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void insert(int priority, T value) {
        ensureCapacity(size + 1);
        heap[size] = new Entry<>(priority, value);
        siftUp(size);
        size++;
    }

    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (isEmpty()) throw new NoSuchElementException("heap is empty");
        Entry<T> min = (Entry<T>) heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) siftDown(0);
        return min.value;
    }

    @SuppressWarnings("unchecked")
    public T peekMin() {
        if (isEmpty()) throw new NoSuchElementException("heap is empty");
        return ((Entry<T>) heap[0]).value;
    }

    /** Build a heap from an unordered array of entries in O(n) — bottom-up heapify. */
    @SuppressWarnings("unchecked")
    public static <T> MinHeap<T> heapify(Entry<T>[] entries) {
        MinHeap<T> h = new MinHeap<>();
        h.ensureCapacity(entries.length);
        System.arraycopy(entries, 0, h.heap, 0, entries.length);
        h.size = entries.length;
        for (int i = entries.length / 2 - 1; i >= 0; i--) {
            h.siftDown(i);
        }
        return h;
    }

    private void ensureCapacity(int min) {
        if (min <= heap.length) return;
        int newCap = heap.length * 2;
        while (newCap < min) newCap *= 2;
        Entry<?>[] nh = new Entry<?>[newCap];
        System.arraycopy(heap, 0, nh, 0, size);
        heap = nh;
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (heap[parent].priority <= heap[i].priority) break;
            swap(i, parent);
            i = parent;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
            if (left < size && heap[left].priority < heap[smallest].priority) smallest = left;
            if (right < size && heap[right].priority < heap[smallest].priority) smallest = right;
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int i, int j) {
        Entry<?> tmp = heap[i]; heap[i] = heap[j]; heap[j] = tmp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(heap[i]);
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
