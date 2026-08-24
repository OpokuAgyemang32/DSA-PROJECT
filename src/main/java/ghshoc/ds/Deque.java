package ghshoc.ds;

import java.util.NoSuchElementException;

/**
 * Custom double-ended queue — GH-SHOC Section 6 (Deque).
 * Built on the DoublyLinkedList so addFront/addRear/removeFront/removeRear
 * are all O(1).
 *
 * Use case: a CARDIAC (urgency 5) request must jump the queue and be
 * inserted at the front, while a routine follow-up is appended at the rear.
 */
public class Deque<T> {

    private final DoublyLinkedList<T> backing = new DoublyLinkedList<>();

    public void addFront(T value) { backing.addFirst(value); }
    public void addRear(T value)  { backing.addLast(value); }

    public T removeFront() {
        if (backing.isEmpty()) throw new NoSuchElementException("deque is empty");
        return backing.removeFirst();
    }

    public T removeRear() {
        if (backing.isEmpty()) throw new NoSuchElementException("deque is empty");
        return backing.removeLast();
    }

    public boolean isEmpty() { return backing.isEmpty(); }
    public int size() { return backing.size(); }

    @Override
    public String toString() { return backing.toString(); }
}
