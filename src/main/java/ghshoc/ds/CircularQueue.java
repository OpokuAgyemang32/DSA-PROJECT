package ghshoc.ds;

import java.util.NoSuchElementException;

/**
 * Fixed-capacity circular queue — GH-SHOC Section 6 (Queue and circular queue).
 * front/rear wrap around the backing array using modulo arithmetic instead
 * of shifting elements, so enqueue/dequeue are true O(1).
 *
 * Used to model the FIFO "walk-in patient" dispatch rule.
 */
public class CircularQueue<T> {

    private final Object[] data;
    private int front;   // index of the current front element
    private int count;   // number of elements currently stored
    private final int capacity;

    public CircularQueue(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be >= 1");
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.front = 0;
        this.count = 0;
    }

    public boolean isEmpty() { return count == 0; }
    public boolean isFull()  { return count == capacity; }
    public int size()        { return count; }

    public void enqueue(T value) {
        if (isFull()) throw new IllegalStateException("queue is full (capacity " + capacity + ")");
        int rear = (front + count) % capacity;
        data[rear] = value;
        count++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        T value = (T) data[front];
        data[front] = null;
        front = (front + 1) % capacity;
        count--;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peekFront() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        return (T) data[front];
    }

    /** Returns the physical index of front and rear — useful for producing the wrap-around trace table. */
    public int frontIndex() { return front; }
    public int rearIndex()  { return isEmpty() ? -1 : (front + count - 1) % capacity; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            sb.append(data[(front + i) % capacity]);
            if (i < count - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
