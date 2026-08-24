package ghshoc;

import ghshoc.ds.*;

import java.util.NoSuchElementException;

/**
 * Phase 1 tests: DynamicArray, DoublyLinkedList, ArrayStack, CircularQueue, Deque.
 * Each structure is tested for: normal case, boundary case, invalid-input case
 * (Section 8.iii requirement). Run with: java -ea ghshoc.Phase1StructureTest
 *
 * This is a lightweight in-house harness (no JUnit) because the sandbox has
 * no Maven Central access; swap in JUnit5 on your own machine if you prefer —
 * the assertions below translate 1:1 to @Test methods.
 */
public class Phase1StructureTest {

    static int passed = 0, failed = 0;

    static void check(boolean cond, String label) {
        if (cond) { passed++; }
        else { failed++; System.out.println("FAILED: " + label); }
    }

    public static void main(String[] args) {
        testDynamicArray();
        testDoublyLinkedList();
        testArrayStack();
        testCircularQueue();
        testDeque();

        System.out.println("\n==== Phase 1 results: " + passed + " passed, " + failed + " failed ====");
        if (failed > 0) System.exit(1);
    }

    // ---------------- DynamicArray ----------------
    static void testDynamicArray() {
        // Normal case
        DynamicArray<Integer> a = new DynamicArray<>(2);
        a.insert(10); a.insert(20); a.insert(30); // forces a resize from cap 2 -> 4
        check(a.size() == 3, "DynamicArray normal size");
        check(a.get(0) == 10 && a.get(2) == 30, "DynamicArray normal get");
        check(a.capacity() == 4, "DynamicArray resize doubled capacity 2->4");

        a.insert(1, 15); // [10,15,20,30]
        check(a.get(1) == 15 && a.get(3) == 30, "DynamicArray insert-at-index shifts right");

        int removed = a.remove(0); // [15,20,30]
        check(removed == 10 && a.get(0) == 15, "DynamicArray remove-at-index shifts left");

        // Boundary case: single element -> empty
        DynamicArray<Integer> b = new DynamicArray<>();
        b.insert(99);
        check(b.remove(0) == 99 && b.isEmpty(), "DynamicArray boundary: drains to empty");

        // Invalid input case
        boolean threw = false;
        try { b.get(0); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "DynamicArray invalid: get on empty throws");
    }

    // ---------------- DoublyLinkedList ----------------
    static void testDoublyLinkedList() {
        // Normal case
        DoublyLinkedList<String> l = new DoublyLinkedList<>();
        l.addLast("B"); l.addFirst("A"); l.addLast("C"); // A,B,C
        check(l.size() == 3, "DLL normal size");
        StringBuilder sb = new StringBuilder();
        for (String s : l) sb.append(s);
        check(sb.toString().equals("ABC"), "DLL iterator order");

        l.insertAfter(1, "B2"); // A,B,B2,C
        sb = new StringBuilder();
        for (String s : l) sb.append(s);
        check(sb.toString().equals("ABB2C"), "DLL insertAfter");

        // Boundary case: single-element list removeFirst == removeLast target
        DoublyLinkedList<Integer> single = new DoublyLinkedList<>();
        single.addFirst(42);
        check(single.removeLast() == 42 && single.isEmpty(), "DLL boundary: single element removal empties list");

        // Invalid input case
        boolean threw = false;
        try { single.removeFirst(); } catch (NoSuchElementException e) { threw = true; }
        check(threw, "DLL invalid: removeFirst on empty throws");
    }

    // ---------------- ArrayStack ----------------
    static void testArrayStack() {
        // Normal case
        ArrayStack<String> s = new ArrayStack<>();
        s.push("assign R1->porter7");
        s.push("assign R2->ambulance2");
        check(s.peek().equals("assign R2->ambulance2"), "Stack normal peek is LIFO top");
        check(s.pop().equals("assign R2->ambulance2"), "Stack normal pop LIFO");
        check(s.size() == 1, "Stack normal size after pop");

        // Boundary case: pop down to empty
        s.pop();
        check(s.isEmpty(), "Stack boundary: empties after popping all");

        // Invalid input case
        boolean threw = false;
        try { s.pop(); } catch (java.util.EmptyStackException e) { threw = true; }
        check(threw, "Stack invalid: pop on empty throws");
    }

    // ---------------- CircularQueue ----------------
    static void testCircularQueue() {
        // Normal case
        CircularQueue<Integer> q = new CircularQueue<>(3);
        q.enqueue(1); q.enqueue(2); q.enqueue(3);
        check(q.isFull(), "CircularQueue normal: fills to capacity");
        check(q.dequeue() == 1, "CircularQueue normal FIFO order");

        // wrap-around: rear should wrap to index 0 after dequeue freed a slot
        q.enqueue(4); // front was 1(idx1), now data = [_,2,3] logically, 4 wraps into idx0
        check(q.rearIndex() == 0, "CircularQueue wrap-around: rear index wraps to 0");
        check(q.size() == 3, "CircularQueue size stays consistent after wrap");

        // Boundary case: drain to empty then refill
        q.dequeue(); q.dequeue(); q.dequeue();
        check(q.isEmpty(), "CircularQueue boundary: drains to empty");

        // Invalid input case
        boolean threw = false;
        try { q.dequeue(); } catch (NoSuchElementException e) { threw = true; }
        check(threw, "CircularQueue invalid: dequeue on empty throws");

        boolean threw2 = false;
        CircularQueue<Integer> full = new CircularQueue<>(1);
        full.enqueue(1);
        try { full.enqueue(2); } catch (IllegalStateException e) { threw2 = true; }
        check(threw2, "CircularQueue invalid: enqueue on full throws");
    }

    // ---------------- Deque ----------------
    static void testDeque() {
        // Normal case: routine request goes to rear, urgent jumps to front
        Deque<String> dq = new Deque<>();
        dq.addRear("Routine-Followup-R101");
        dq.addRear("Routine-Followup-R102");
        dq.addFront("CARDIAC-URGENT-R205");
        check(dq.removeFront().equals("CARDIAC-URGENT-R205"), "Deque normal: urgent request served first");

        // Boundary case: single element removed from either end empties it
        Deque<Integer> d2 = new Deque<>();
        d2.addFront(7);
        check(d2.removeRear() == 7 && d2.isEmpty(), "Deque boundary: single element, removeRear empties");

        // Invalid input case
        boolean threw = false;
        try { d2.removeFront(); } catch (NoSuchElementException e) { threw = true; }
        check(threw, "Deque invalid: removeFront on empty throws");
    }
}
