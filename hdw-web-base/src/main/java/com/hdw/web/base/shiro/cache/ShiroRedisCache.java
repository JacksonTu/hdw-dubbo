package com.hdw.web.base.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description 自定义Shiro的Cache
 * @Author JacksonTu
 * @Date 2020/4/5 12:38
 */
public class ShiroRedisCache<K, V> implements Cache<K, V> {

    //TODO:缓存存活时间
    private long cacheLive;
    //TODO:缓存名称前缀
    private String cacheKeyPrefix;

    private RedisTemplate redisTemplate;

    public ShiroRedisCache(long cacheLive, String cacheKeyPrefix, RedisTemplate redisTemplate) {
        this.cacheLive = cacheLive;
        this.cacheKeyPrefix = cacheKeyPrefix;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public V get(K k) throws CacheException {
        return (V) this.redisTemplate.opsForValue().get(this.getRedisCacheKey(k));
    }

    @Override
    public V put(K k, V v) throws CacheException {
        this.redisTemplate.opsForValue().set(this.getRedisCacheKey(k), v, cacheLive, TimeUnit.SECONDS);
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        if (k == null) {
            return null;
        } else {
            V obj = (V) this.redisTemplate.opsForValue().get(this.getRedisCacheKey(k));
            this.redisTemplate.delete(this.getRedisCacheKey(k));
            return obj;
        }
    }

    @Override
    public void clear() throws CacheException {
        Set keys = this.redisTemplate.keys(this.cacheKeyPrefix + "*");
        if (null != keys && keys.size() > 0) {
            Iterator iterator = keys.iterator();
            this.redisTemplate.delete(iterator.next());
        }

    }

    @Override
    public int size() {
        Set<K> keys = this.redisTemplate.keys(this.cacheKeyPrefix + ":");
        return keys.size();
    }

    @Override
    public Set<K> keys() {
        return this.redisTemplate.keys(this.cacheKeyPrefix + ":");
    }

    @Override
    public Collection<V> values() {
        Set<K> keys = this.redisTemplate.keys(this.cacheKeyPrefix + ":");
        Set<V> values = new HashSet<V>(keys.size());
        for (K key : keys) {
            values.add((V) this.redisTemplate.opsForValue().get(this.getRedisCacheKey(key)));
        }
        return values;
    }

    private String getRedisCacheKey(K key) {
        Object redisKey = this.getStringRedisKey(key);
        if (redisKey instanceof String) {
            return this.cacheKeyPrefix + redisKey;
        } else {
            return String.valueOf(redisKey);
        }
    }

    private Object getStringRedisKey(K key) {
        Object redisKey;
        if (key instanceof PrincipalCollection) {
            redisKey = this.getRedisKeyFromPrincipalCollection((PrincipalCollection) key);
        } else {
            redisKey = key.toString();
        }
        return redisKey;
    }

    private Object getRedisKeyFromPrincipalCollection(PrincipalCollection key) {
        List realmNames = this.getRealmNames(key);
        Collections.sort(realmNames);
        Object redisKey = this.joinRealmNames(realmNames);
        return redisKey;
    }

    private List<String> getRealmNames(PrincipalCollection key) {
        ArrayList realmArr = new ArrayList();
        Set realmNames = key.getRealmNames();
        Iterator i$ = realmNames.iterator();
        while (i$.hasNext()) {
            String realmName = (String) i$.next();
            realmArr.add(realmName);
        }
        return realmArr;
    }

    private Object joinRealmNames(List<String> realmArr) {
        StringBuilder redisKeyBuilder = new StringBuilder();
        for (int i = 0; i < realmArr.size(); ++i) {
            String s = realmArr.get(i);
            redisKeyBuilder.append(s);
        }
        String redisKey = redisKeyBuilder.toString();
        return redisKey;
    }

}
