package com.hdw.test;

import com.hdw.common.config.redis.RedissonLocker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Description redisson分布式锁测试
 * @Author TuMinglong
 * @Date 2019/1/28 16:17
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedissonTest {

    protected static final Logger logger = LoggerFactory.getLogger(RedissonTest.class);

    @Autowired
    private RedissonLocker redissonLocker;

    // 控制线程数，最优选择是处理器线程数*3，本机处理器是4线程
    private final static int THREAD_COUNT = 12;


    @Test
    public void test() {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        String key = "redisson_key";
        for (int i = 0; i < 100; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    logger.info("---- 线程开启 " + Thread.currentThread().getName() + "----");
                    //直接加速，获取不到锁则一直等待获取锁
                    redissonLocker.lock(key, 10l);
                    try {
                        //获取锁之后可以进行相应处理
                        Thread.sleep(100);
                        logger.info("---- 获取锁之后进行相应处理 " + Thread.currentThread().getName() + "----");
                        //解锁
                        redissonLocker.unlock(key);
                        logger.info("----" + Thread.currentThread().getName() + "----");
                        //尝试获取锁，等待5秒，自己获得锁后一直不解锁则10秒后自动解锁
                        boolean isGetLock = redissonLocker.tryLock(key, TimeUnit.SECONDS, 5l, 10l);
                        if (isGetLock) {
                            //获取锁之后可以进行相应处理
                            Thread.sleep(100);
                            logger.info("---- 获取锁之后进行相应处理 " + Thread.currentThread().getName() + "----");
                            //解锁
                            //redissonLocker.unlock(key);
                            logger.info("----" + Thread.currentThread().getName() + "----");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        pool.shutdown();
    }

}
