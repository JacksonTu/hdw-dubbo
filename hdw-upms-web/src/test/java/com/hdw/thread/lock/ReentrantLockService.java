package com.hdw.thread.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description com.hdw.lock
 * @Author TuMinglong
 * @Date 2019/2/26 17:33
 **/
public class ReentrantLockService {
    private Lock lock = new ReentrantLock();

    private volatile int num = 5;

    public synchronized void test() {
        // lock.lock();
        for (int i = 0; i < 5; i++) {
            System.out.println("ThreadName=" + Thread.currentThread().getName()
                    + (" " + (i + 1)));
        }
        //lock.unlock();
    }

    public void test2() {
        lock.lock();
        for (int i = 0; i < 5; i++) {
            num++;
            System.out.println("ThreadName=" + Thread.currentThread().getName()
                    + (" " + (i + 1)) + " " + num);
        }
        lock.unlock();
    }


}
