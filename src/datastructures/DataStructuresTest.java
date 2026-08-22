package datastructures;

public class DataStructuresTest {
    
    public static void main(String[] args) {
        System.out.println("Running Data Structures Verification...");
        
        testDynamicArray();
        testSinglyLinkedList();
        testDoublyLinkedList();
        
        System.out.println("All basic tests passed successfully!");
    }

    private static void testDynamicArray() {
        System.out.print("Testing DynamicArray... ");
        CustomList<String> list = new DynamicArray<>();
        
        // Test add and size
        list.add("A");
        list.add("B");
        list.add("C");
        assert list.size() == 3 : "Size should be 3";
        
        // Test get
        assert list.get(1).equals("B") : "Index 1 should be B";
        
        // Test insert
        list.insert(1, "X");
        assert list.get(1).equals("X") : "Index 1 should be X after insert";
        assert list.get(2).equals("B") : "Index 2 should be B after insert";
        assert list.size() == 4 : "Size should be 4";
        
        // Test remove
        String removed = list.remove(1);
        assert removed.equals("X") : "Removed item should be X";
        assert list.size() == 3 : "Size should be 3 after removal";
        
        // Test iterator
        int count = 0;
        for (String item : list) {
            count++;
        }
        assert count == 3 : "Iterator should iterate 3 times";
        
        System.out.println("OK");
    }

    private static void testSinglyLinkedList() {
        System.out.print("Testing SinglyLinkedList... ");
        SinglyLinkedList<Integer> list = new SinglyLinkedList<>();
        
        list.add(10);
        list.add(20);
        list.addFirst(5);
        
        assert list.size() == 3 : "Size should be 3";
        assert list.get(0) == 5 : "First element should be 5";
        assert list.get(2) == 20 : "Last element should be 20";
        
        list.insert(1, 15);
        assert list.get(1) == 15 : "Inserted element should be 15";
        
        int removed = list.removeFirst();
        assert removed == 5 : "Removed element should be 5";
        assert list.size() == 3 : "Size should be 3 after removal";
        
        System.out.println("OK");
    }

    private static void testDoublyLinkedList() {
        System.out.print("Testing DoublyLinkedList... ");
        DoublyLinkedList<Double> list = new DoublyLinkedList<>();
        
        list.add(1.1);
        list.addLast(3.3);
        list.addFirst(0.0);
        
        assert list.size() == 3 : "Size should be 3";
        assert list.get(0) == 0.0 : "First element should be 0.0";
        assert list.get(2) == 3.3 : "Last element should be 3.3";
        
        list.insert(1, 2.2);
        assert list.get(1) == 2.2 : "Inserted element should be 2.2";
        
        double removed = list.removeLast();
        assert removed == 3.3 : "Removed element should be 3.3";
        assert list.size() == 3 : "Size should be 3 after removal";
        
        System.out.println("OK");
    }
}
