package ai.llm.utils;

import ai.medusa.utils.PromptCacheConfig;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityLock {

    private static final Logger log = LoggerFactory.getLogger(PriorityLock.class);
    private final ReentrantLock lock = new ReentrantLock(true); // Using fair lock to prevent starvation
    private final Condition lockAvailable = lock.newCondition();
    private final AtomicInteger currentLockCount = new AtomicInteger(0);
    private final AtomicInteger lowLockCount = new AtomicInteger(0);
    private final int limit;
    private final int LOW_LIMIT;
    public static final int LOW_PRIORITY = PromptCacheConfig.MEDUSA_PRIORITY;
    public static final int HIGH_PRIORITY = 100;
    private static final int PRIORITY_QUEUE_INITIAL_CAPACITY = 10000;

    private final PriorityBlockingQueue<LockRequest> lockQueue = new PriorityBlockingQueue<>(PRIORITY_QUEUE_INITIAL_CAPACITY);

    public PriorityLock(int limit) {
        if (limit < 2) {
            limit = 2;
        }
        this.limit = limit;
        this.LOW_LIMIT = 1; // Made this consistent as a final field with initialization in constructor
    }

    @ToString
    private static class LockRequest implements Comparable<LockRequest> {
        final int priority;
        final Thread owner;

        LockRequest(int priority) {
            this.priority = priority;
            this.owner = Thread.currentThread(); // Track owner thread for better debugging
        }

        @Override
        public int compareTo(LockRequest o) {
            return Integer.compare(o.priority, this.priority);
        }
    }

    public void lock(int priority) {
        LockRequest request = new LockRequest(priority);

        lock.lock();
        try {
            lockQueue.add(request); // Use add instead of put since we already have the lock

            // Wait until this request is at the head of the queue and we can acquire the lock
            while (true) {
                LockRequest head = lockQueue.peek();

                // If our request is at the head and we can acquire a lock, proceed
                if (head != null && request.equals(head) && canAcquireLock(priority)) {
                    lockQueue.poll(); // Atomically remove the head within the lock section

                    // Update lock counts atomically within the same critical section
                    currentLockCount.incrementAndGet();
                    if (priority <= LOW_PRIORITY) {
                        lowLockCount.incrementAndGet();
                    }
                    return; // Successfully acquired lock, return from method
                }

                try {
                    // Wait efficiently instead of busy polling
                    lockAvailable.await();
                } catch (InterruptedException e) {
                    // Remove our request from the queue since we're interrupted
                    lockQueue.remove(request);
                    // Restore interrupted status
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Lock acquisition interrupted", e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void unlock(int priority) {
        lock.lock();
        try {
            currentLockCount.decrementAndGet();
            if (priority <= LOW_PRIORITY) {
                lowLockCount.decrementAndGet();
            }

            // Signal waiting threads that a lock might be available
            lockAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean canAcquireLock(int priority) {
        if (priority > LOW_PRIORITY) {
            return currentLockCount.get() < limit;
        }
        return currentLockCount.get() < limit && lowLockCount.get() < (limit - LOW_LIMIT);
    }

    // Method to get current lock statistics - helpful for monitoring
    public String getStats() {
        return String.format("Total locks: %d, Low priority locks: %d, Limit: %d, Queue size: %d",
                currentLockCount.get(), lowLockCount.get(), limit, lockQueue.size());
    }

    // Method to handle timeouts
    public boolean tryLock(int priority, long timeoutMillis) {
        LockRequest request = new LockRequest(priority);
        long deadline = System.currentTimeMillis() + timeoutMillis;

        lock.lock();
        try {
            lockQueue.add(request);

            while (System.currentTimeMillis() < deadline) {
                LockRequest head = lockQueue.peek();

                if (head != null && request.equals(head) && canAcquireLock(priority)) {
                    lockQueue.poll();
                    currentLockCount.incrementAndGet();
                    if (priority <= LOW_PRIORITY) {
                        lowLockCount.incrementAndGet();
                    }
                    return true; // Successfully acquired lock
                }

                try {
                    long remainingTime = deadline - System.currentTimeMillis();
                    if (remainingTime <= 0) {
                        // Timeout expired, remove our request
                        lockQueue.remove(request);
                        return false;
                    }
                    // Wait with timeout
                    lockAvailable.await(remainingTime, java.util.concurrent.TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // Remove our request from the queue
                    lockQueue.remove(request);
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            // If we get here, timeout expired while waiting
            lockQueue.remove(request);
            return false;
        } finally {
            lock.unlock();
        }
    }
}