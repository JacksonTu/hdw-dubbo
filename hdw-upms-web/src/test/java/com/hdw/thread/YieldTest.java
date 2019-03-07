package com.hdw.thread;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @Description 让步线程测试
 * @Date 2018/9/5 11:18
 * @Author TuMingling
 */
@RunWith(SpringRunner.class)
public class YieldTest {
    /**
     * 记住当线程的优先级没有指定时，所有线程都携带普通优先级。
     * 优先级可以用从1到10的范围指定。10表示最高优先级，1表示最低优先级，5是普通优先级。
     * 记住优先级最高的线程在执行时被给予优先。但是不能保证线程在启动时就进入运行状态。
     * 与在线程池中等待运行机会的线程相比，当前正在运行的线程可能总是拥有更高的优先级。
     * 由调度程序决定哪一个线程被执行。
     * t.setPriority()用来设定线程的优先级。
     * 记住在线程开始方法被调用之前，线程的优先级应该被设定。
     * 你可以使用常量，如MIN_PRIORITY,MAX_PRIORITY，NORM_PRIORITY来设定优先级
     */

    @Test
    public void test() {
        YieldThread yieldThread = new YieldThread("让步线程1");
        FutureTask<Integer> futureTask = new FutureTask<>(yieldThread);
        Thread thread = new Thread(futureTask);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        YieldThread2 yieldThread2 = new YieldThread2("让步线程2");
        FutureTask<Integer> futureTask2 = new FutureTask<>(yieldThread2);
        Thread thread2 = new Thread(futureTask2);
        thread2.setPriority(Thread.MAX_PRIORITY);
        thread2.start();

        System.out.println("主线程在执行任务");

        try {
            System.out.println("thread运行结果:" + futureTask.get());
            System.out.println("thread2运行结果:" + futureTask2.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("所有任务执行完毕");


    }


}

class YieldThread implements Callable<Integer> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public YieldThread(String name) {
        this.name = name;
    }

    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " " + getName() + " " + i);
            sum = i;
            Thread.yield();
        }
        return sum;
    }
}


class YieldThread2 implements Callable<Integer> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public YieldThread2(String name) {
        this.name = name;
    }

    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 0; i < 50; i++) {
            System.out.println(Thread.currentThread().getName() + " " + getName() + " " + i);
            sum = i;
            Thread.yield();
        }
        return sum;
    }
}
