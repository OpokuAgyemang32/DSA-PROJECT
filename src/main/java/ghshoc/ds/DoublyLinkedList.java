package ghshoc.ds;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom doubly linked list — GH-SHOC Section 6 (Singly or doubly linked list).
 * Supports addFirst, addLast, insertAfter, remove, and a forward Iterator.
 *
 * Used in GH-SHOC to hold ordered walk-lists (e.g. a porter's planned
 * stop sequence) where frequent front/back insertion beats a dynamic array.
 */
public class DoublyLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        T value;
        Node<T> prev, next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head, tail;
    private int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void addFirst(T value) {
        Node<T> n = new Node<>(value);
        if (head == null) {
            head = tail = n;
        } else {
            n.next = head;
            head.prev = n;
            head = n;
        }
        size++;
    }

    public void addLast(T value) {
        Node<T> n = new Node<>(value);
        if (tail == null) {
            head = tail = n;
        } else {
            tail.next = n;
            n.prev = tail;
            tail = n;
        }
        size++;
    }

    private Node<T> nodeAt(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index " + index);
        Node<T> cur = head;
        for (int i = 0; i < index; i++) cur = cur.next;
        return cur;
    }

    /** Insert `value` immediately after the element currently at `index`. */
    public void insertAfter(int index, T value) {
        Node<T> target = nodeAt(index);
        Node<T> n = new Node<>(value);
        n.prev = target;
        n.next = target.next;
        if (target.next != null) target.next.prev = n;
        target.next = n;
        if (target == tail) tail = n;
        size++;
    }

    /** Removes and returns the first occurrence equal to value (by equals()). */
    public boolean remove(T value) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.value == null ? value == null : cur.value.equals(value)) {
                unlink(cur);
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    public T removeFirst() {
        if (head == null) throw new NoSuchElementException("list is empty");
        T v = head.value;
        unlink(head);
        return v;
    }

    public T removeLast() {
        if (tail == null) throw new NoSuchElementException("list is empty");
        T v = tail.value;
        unlink(tail);
        return v;
    }

    private void unlink(Node<T> n) {
        if (n.prev != null) n.prev.next = n.next; else head = n.next;
        if (n.next != null) n.next.prev = n.prev; else tail = n.prev;
        n.prev = n.next = null;
        size--;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> cursor = head;

            @Override
            public boolean hasNext() { return cursor != null; }

            @Override
            public T next() {
                if (cursor == null) throw new NoSuchElementException();
                T v = cursor.value;
                cursor = cursor.next;
                return v;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> cur = head;
        while (cur != null) {
            sb.append(cur.value);
            if (cur.next != null) sb.append(" <-> ");
            cur = cur.next;
        }
        return sb.append("]").toString();
    }
}
