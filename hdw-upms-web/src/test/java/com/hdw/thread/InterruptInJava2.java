package com.hdw.thread;

/**
 * @Description 使用thread.interrupt()中断非阻塞状态线程
 * @Author TuMinglong
 * @Date 2019/3/5 11:34
 **/
public class InterruptInJava2 implements Runnable {

    @Override
    public void run() {
        // 每隔一秒检测是否设置了中断标示
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("Thread is running...");
            long time = System.currentTimeMillis();
            /*
             * 使用while循环模拟 sleep 方法，这里不要使用sleep，否则在阻塞时会 抛
             * InterruptedException异常而退出循环，这样while检测stop条件就不会执行，
             * 失去了意义。
             */
            while ((System.currentTimeMillis() - time < 1000)) {
            }
        }
        System.out.println("Thread exiting under request...");
    }
}
