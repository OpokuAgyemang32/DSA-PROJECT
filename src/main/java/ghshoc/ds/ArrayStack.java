package ghshoc.ds;

import java.util.EmptyStackException;

/**
 * Custom array-backed stack — GH-SHOC Section 6 (Stack).
 * Backs the audit_events undo log: every resource assignment pushes an
 * AuditEvent; an "undo last assignment" operation pops it off.
 */
public class ArrayStack<T> {

    private final DynamicArray<T> backing = new DynamicArray<>();

    public void push(T value) {
        backing.insert(value); // insert at end == push
    }

    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        return backing.removeLast();
    }

    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return backing.get(backing.size() - 1);
    }

    public boolean isEmpty() {
        return backing.isEmpty();
    }

    public int size() {
        return backing.size();
    }

    @Override
    public String toString() {
        return backing.toString();
    }
}
