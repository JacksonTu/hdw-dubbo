package com.hdw.thread;

/**
 * @Description 中断应用
 * @Author TuMinglong
 * @Date 2019/3/5 11:39
 **/
public class InterruptInJavaTest {

    public static void main(String[] args) {
//        InterruptInJava interruptInJava=new InterruptInJava();
//        System.out.println("Starting thread...");
//        Thread thread=new Thread(interruptInJava);
//        thread.start();
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Asking thread to stop...");
//        // 设置中断信号量
//        interruptInJava.setRunning(true);
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Stopping application...");


        System.out.println("Starting thread...");
        InterruptInJava interruptInJava2 = new InterruptInJava();

        Thread thread = new Thread(interruptInJava2);
        thread.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Asking thread to stop...");
        // 发出中断请求
        thread.interrupt();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping application...");
    }
}
