package ai.llm.utils;


import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityLock {

    private static final Logger log = LoggerFactory.getLogger(PriorityLock.class);
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger currentLockCount = new AtomicInteger(0);
    private final AtomicInteger lowLockCount = new AtomicInteger(0);
    private final int limit;
    private final int LOW_LIMIT = 1;
    public static final int LOW_PRIORITY = 50;
    public static final int HIGH_PRIORITY = 100;
    private static final int PRIORITY_QUEUE_INITIAL_CAPACITY = 10000;


    private final PriorityBlockingQueue<LockRequest> lockQueue = new PriorityBlockingQueue<>(PRIORITY_QUEUE_INITIAL_CAPACITY);

    public PriorityLock(int limit) {
        if (limit < 2) {
            limit = 2;
        }
        this.limit = limit;
    }

    @ToString
    private static class LockRequest implements Comparable<LockRequest> {
        int priority;

        LockRequest(int priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(LockRequest o) {
            return Integer.compare(o.priority, this.priority);
        }
    }

    public void lock(int priority) {
        LockRequest request = new LockRequest(priority);
        lockQueue.put(request);
        while (true) {
            lock.lock();
            try {
                LockRequest head = lockQueue.peek();
                if(head == null) {
                    break;
                }
                if(request != head  && head.priority == request.priority && canAcquireLock(request.priority)) {
                    delay(100);
                    continue;
                }
                if (request == head &&  canAcquireLock(request.priority)) {
                    lockQueue.take();
                    currentLockCount.incrementAndGet();
                    if(priority <= LOW_PRIORITY) {
                        lowLockCount.incrementAndGet();
                    }
                    break;
                }
            } catch (InterruptedException e) {
                log.error("lock error", e);
            }finally {
                lock.unlock();
            }
            delay(300);
        }
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public void unlock(int priority) {
        lock.lock();
        try {
            currentLockCount.decrementAndGet();
            if(priority <= LOW_PRIORITY) {
                lowLockCount.decrementAndGet();
            }
        } finally {
            lock.unlock();
        }
    }


    private boolean canAcquireLock(int priority) {
        if(priority > LOW_PRIORITY) {
            return currentLockCount.get() < limit;
        }
        return currentLockCount.get() < limit && lowLockCount.get() < limit - LOW_LIMIT;
    }

    public static void main(String[] args) {

    }
}
