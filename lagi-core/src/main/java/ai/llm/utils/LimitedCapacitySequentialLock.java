package ai.llm.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LimitedCapacitySequentialLock {

    private static final Logger log = LoggerFactory.getLogger(LimitedCapacitySequentialLock.class);
    private ReentrantLock lock = new ReentrantLock(true);

    private final Condition condition = lock.newCondition();

    private int limit = Integer.MAX_VALUE;

    private int remaining = limit;


    public void getStats() {
        System.out.println("limit: " + limit + " remaining: " + remaining);
    }

    public LimitedCapacitySequentialLock(Integer limit) {
        this.limit = limit;
        this.remaining = limit;
    }

    public void acquire() {
        lock.lock();
        try {
            while (remaining <= 0) {
                condition.await(); // 等待信号量释放
            }
            remaining--;
        } catch (InterruptedException e){
            log.error("acquire error", e);
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    public void release() {
        lock.lock();
        try {
            remaining++;
            condition.signalAll(); // 唤醒一个等待线程
        } finally {
            lock.unlock();
        }
    }

//    public static void main(String[] args) {
//        LimitedCapacitySequentialLock limitedCapacitySequentialLock = new LimitedCapacitySequentialLock(1);
//        for (int i = 0; i < 100; i++) {
//            new Thread(() -> {
//                limitedCapacitySequentialLock.acquire();
//                try {
//                    Thread.sleep(1000);
//                }catch (InterruptedException e) {
//                    log.error("release error", e);
//                }
//                System.out.println(Thread.currentThread().getName() + " acquired");
//                limitedCapacitySequentialLock.release();
//                System.out.println(Thread.currentThread().getName() + " released");
//            }, "Thread-" + i).start();
//        }
//    }

}