package com.hdw.thread.lock;

/**
 * @Description com.hdw.lock
 * @Author TuMinglong
 * @Date 2019/2/26 17:35
 **/
public class LockTest {

    public static void main(String[] args) {
        ReentrantLockService reentrantLockService = new ReentrantLockService();
        new Thread(() -> reentrantLockService.test2()).start();
        new Thread(() -> reentrantLockService.test2()).start();
    }
}
