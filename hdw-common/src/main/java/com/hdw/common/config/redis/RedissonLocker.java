package com.hdw.common.config.redis;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * @Description redisson 分布式锁接口
 * @Author TuMinglong
 * @Date 2019/1/28 16:00
 **/
public interface RedissonLocker {

    RLock lock(String lockKey);

    /**
     * @param lockKey
     * @param leaseTime 加锁时间，单位秒
     * @return
     */
    RLock lock(String lockKey, Long leaseTime);

    /**
     * @param lockKey
     * @param unit
     * @param timeout 加锁时间
     * @return
     */
    RLock lock(String lockKey, TimeUnit unit, Long timeout);

    /**
     * 测试锁，拿到lock就返回true,否则返回false
     * 时间限制，拿不到lock，就等一段时间，超时返回false
     *
     * @param lockKey
     * @param unit
     * @param waitTime
     * @param leaseTime
     * @return
     */
    boolean tryLock(String lockKey, TimeUnit unit, Long waitTime, Long leaseTime);

    /**
     * 解锁
     *
     * @param lockKey
     */
    void unlock(String lockKey);

    /**
     * 解锁
     *
     * @param lock
     */
    void unlock(RLock lock);
}
