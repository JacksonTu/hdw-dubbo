package com.hdw.common.core.task;

import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description Spring定时任务线程池配置
 * @Author JacksonTu
 * @Date 2020/4/1 23:45
 */
@EnableScheduling
public class ScheduledThreadPoolConfig implements SchedulingConfigurer {
    @Resource
    private Environment env;

    /**
     * 对于CPU密集型任务，最大线程数是CPU线程数+1。对于IO密集型任务，尽量多配点，可以是CPU线程数*2，或者CPU线程数/(1-阻塞系数)。
     * maxPoolSize=(int) (Runtime.getRuntime().availableProcessors() / (1 - 0.9))
     *
     * @param taskRegistrar
     */

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.initialize();
        // 设置默认线程名称
        executor.setThreadNamePrefix(env.getProperty("spring.application.name") + "-task");
        // 设置最大线程数
        executor.setPoolSize((int) (Runtime.getRuntime().availableProcessors() / (1 - 0.9)));
        // 设置线程活跃时间（秒）
        executor.setAwaitTerminationSeconds(60);
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
