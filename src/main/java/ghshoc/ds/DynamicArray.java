package ghshoc.ds;

import java.util.NoSuchElementException;

/**
 * Custom dynamic (growable) array — a from-scratch replacement for
 * java.util.ArrayList, built for GH-SHOC (Section 6: Dynamic array).
 *
 * Backing store doubles on overflow, halves (down to a floor) when
 * usage drops below 1/4 capacity, to keep amortised O(1) insert/remove
 * at the end while bounding wasted space.
 */
public class DynamicArray<T> {

    private static final int DEFAULT_CAPACITY = 8;

    private Object[] data;
    private int size;

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 1) throw new IllegalArgumentException("initialCapacity must be >= 1");
        data = new Object[initialCapacity];
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int capacity() {
        return data.length;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds for size " + size);
        }
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) data[index];
    }

    public void set(int index, T value) {
        checkIndex(index);
        data[index] = value;
    }

    /** Insert at the end. Amortised O(1). */
    public void insert(T value) {
        insert(size, value);
    }

    /** Insert at an arbitrary index, shifting later elements right. O(n) worst case. */
    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds for size " + size);
        }
        ensureCapacity(size + 1);
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        data[index] = value;
        size++;
    }

    /** Remove and return the element at index, shifting later elements left. O(n). */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkIndex(index);
        T removed = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[size - 1] = null;
        size--;
        shrinkIfNeeded();
        return removed;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) return;
        int newCapacity = data.length * 2;
        while (newCapacity < minCapacity) newCapacity *= 2;
        resize(newCapacity);
    }

    private void shrinkIfNeeded() {
        if (data.length > DEFAULT_CAPACITY && size <= data.length / 4) {
            resize(Math.max(DEFAULT_CAPACITY, data.length / 2));
        }
    }

    /** Exposed (package-visible via public method) so the resize trace in the report can call it directly. */
    public void resize(int newCapacity) {
        Object[] newData = new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, size);
        data = newData;
    }

    public T removeLast() {
        if (isEmpty()) throw new NoSuchElementException("DynamicArray is empty");
        return remove(size - 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(data[i]);
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
