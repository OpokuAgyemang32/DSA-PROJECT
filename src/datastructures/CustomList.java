package datastructures;

/**
 * A generic interface defining the standard operations for lists.
 * This ensures consistency across Dynamic Array and Linked List implementations.
 */
public interface CustomList<T> extends Iterable<T> {
    
    /**
     * Adds an item to the end of the list.
     * @param item the item to add
     */
    void add(T item);
    
    /**
     * Inserts an item at the specified index.
     * @param index the index to insert at
     * @param item the item to insert
     */
    void insert(int index, T item);
    
    /**
     * Retrieves the item at the specified index.
     * @param index the index of the item
     * @return the item
     */
    T get(int index);
    
    /**
     * Removes and returns the item at the specified index.
     * @param index the index of the item to remove
     * @return the removed item
     */
    T remove(int index);
    
    /**
     * Replaces the item at the specified index.
     * @param index the index of the item to replace
     * @param item the new item
     */
    void set(int index, T item);
    
    /**
     * Returns the number of elements in the list.
     * @return the size of the list
     */
    int size();
    
    /**
     * Checks if the list is empty.
     * @return true if empty, false otherwise
     */
    boolean isEmpty();
}
