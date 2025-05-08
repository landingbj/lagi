package ai.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe fixed-length list that removes the oldest element when capacity is exceeded.
 *
 * @param <T> The type of elements in the list
 */
public class ThreadSafeFixedLengthList<T> {
    private final LinkedList<T> list;
    private final int capacity;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new thread-safe fixed-length list.
     *
     * @param capacity The maximum number of elements the list can hold
     */
    public ThreadSafeFixedLengthList(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.list = new LinkedList<>();
    }

    /**
     * Adds a value to the list.
     * If the list is at capacity, the oldest element is removed.
     *
     * @param value The value to add
     */
    public void add(T value) {
        lock.writeLock().lock();
        try {
            if (list.size() >= capacity) {
                list.removeFirst(); // Remove the oldest element
            }
            list.addLast(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets a copy of the current list.
     *
     * @return A copy of the list
     */
    public List<T> getList() {
        lock.readLock().lock();
        try {
            return new LinkedList<>(list);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the current size of the list.
     *
     * @return The number of elements in the list
     */
    public int size() {
        lock.readLock().lock();
        try {
            return list.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clears all elements from the list.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            list.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
