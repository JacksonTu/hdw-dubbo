package com.hdw.common.core.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author JacksonTu
 * @version 1.0
 * @description 异步和定时任务线程池配置
 * @since 2020/11/9 21:22
 */
@Slf4j
@EnableScheduling
@EnableAsync
public class ThreadPoolConfigure {

    /**
     * ThreadPoolExecutor
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                (int) (Runtime.getRuntime().availableProcessors() / (1 - 0.9)),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Runtime.getRuntime().availableProcessors()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }


    /**
     * 异步任务
     * ThreadPoolTaskExecutor
     * 对于CPU密集型任务，最大线程数是CPU线程数+1。
     * 对于IO密集型任务，尽量多配点，可以是CPU线程数*2，或者CPU线程数/(1-阻塞系数)。
     * maxPoolSize=(int) (Runtime.getRuntime().availableProcessors()/(1-0.9))
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // 设置最大线程数
        taskExecutor.setMaxPoolSize((int) (Runtime.getRuntime().availableProcessors() / (1 - 0.9)));
        // 设置队列容量
        taskExecutor.setQueueCapacity(Runtime.getRuntime().availableProcessors());
        // 设置线程活跃时间（秒）
        taskExecutor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        taskExecutor.setThreadNamePrefix("taskExecutor-");
        // 设置拒绝策略为使用当前线程执行
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return taskExecutor;
    }

    /**
     * 定时任务
     * ThreadPoolTaskScheduler
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        taskScheduler.setThreadNamePrefix("taskScheduler-");
        taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //调度器shutdown被调用时等待当前被调度的任务完成
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        //等待时长
        taskScheduler.setAwaitTerminationSeconds(60);
        return taskScheduler;
    }
}
