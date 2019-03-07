package com.hdw.thread;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.*;

/**
 * @Description com.hdw.thread
 * @Date 2018/9/5 10:37
 * @Author TuMingling
 */
@RunWith(SpringRunner.class)
public class FutureTaskTest {

    @Test
    public void test() {
        ExecutorService pool = Executors.newFixedThreadPool(12);
        CallableInJava joinThread = new CallableInJava("测试");
        FutureTask<Integer> futureTask = new FutureTask<>(joinThread);

//        Thread thread = new Thread(futureTask);
//        thread.start();
        pool.submit(futureTask);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean flag = futureTask.cancel(true);
        System.out.println("取消任务状态：" + flag);
        pool.shutdown();
        /**
         * cancel方法用来取消任务，如果取消任务成功则返回true，如果取消任务失败则返回false。参数mayInterruptIfRunning表示是否允许取消正在执行却没有执行完毕的任务，如果设置true，则表示可以取消正在执行过程中的任务。如果任务已经完成，则无论mayInterruptIfRunning为true还是false，此方法肯定返回false，即如果取消已经完成的任务会返回false；如果任务正在执行，若mayInterruptIfRunning设置为true，则返回true，若mayInterruptIfRunning设置为false，则返回false；如果任务还没有执行，则无论mayInterruptIfRunning为true还是false，肯定返回true。
         * isCancelled方法表示任务是否被取消成功，如果在任务正常完成前被取消成功，则返回 true。
         * isDone方法表示任务是否已经完成，若任务完成，则返回true；
         * get()方法用来获取执行结果，这个方法会产生阻塞，会一直等到任务执行完毕才返回；
         * get(long timeout, TimeUnit unit)用来获取执行结果，如果在指定时间内，还没获取到结果，就直接返回null。
         */

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.println("主线程在执行任务");

        try {
            System.out.println("thread运行结果:" + futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("所有任务执行完毕");
    }
}

class CallableInJava implements Callable<Integer> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CallableInJava(String name) {
        this.name = name;
    }

    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " " + getName() + " " + i);
            sum = i;
        }
        return sum;
    }
}
