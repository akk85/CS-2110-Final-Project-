package datastructures;

/**
 * A priority queue containing distinct elements of type E, with
 * priorities represented as values of type {@code double}. Smaller
 * values expressing higher priorities; for example, 0.0 means higher
 * priority than 1.0 does. Below, N is used as the number of elements
 * currently in the priority queue.
 */
public interface PQueue<E> {
    /**
     * Returns a string that represents this priority queue:
     * {@code [item0:priority0, item1:priority1, ..., item(N-1):priority(N-1)]}
     * That is, the list is delimited by '[' and ']', and items are separated
     * by ", " (a comma and a space).
     */
    String toString();

    /**
     * Returns: the number of elements in the priority queue.
     */
    int size();

    /**
     * Returns: true iff the priority queue is empty.
     */
    boolean isEmpty();

    /**
     * Effect: Add e with priority p to the priority queue.
     * Throw an illegalArgumentException if e is already in the queue.
     */
    void add(E e, double priority) throws IllegalArgumentException;

    /**
     * Returns: the element of the priority queue with highest priority,
     * without changing the priority queue.
     * Requires: the priority queue is not empty.
     */
    E peek();

    /**
     * Effect: Remove (and return) the element of the priority queue
     * with highest priority.
     * Requires: the priority queue is not empty.
     */
    E extractMin();

    /**
     * Effect: Change the priority of element e to p.
     * Requires: e is in the priority queue.
     */
    void changePriority(E e, double p);
}
