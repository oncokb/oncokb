package org.mskcc.cbio.oncokb.cache;

import org.redisson.api.RedissonClient;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class CustomMapRedisCache extends CustomRedisCache {
    public CustomMapRedisCache(String name, RedissonClient client, long ttlMinutes, CacheErrorHandler cacheErrorHandler) {
        super(name, client, ttlMinutes, cacheErrorHandler);
    }

    @Override
    protected Object lookup(Object key) {
        try {
            Object value = this.store.getMap(name).get(key);
            if (value != null) {
                value = fromStoreValue(value);
            }
            return value;        
        } catch (RuntimeException e) {
            cacheErrorHandler.handleCacheGetError(e, this, key);
            return null;
        }
    }

    @Override
    public void put(Object key, Object value) {
        try {
            this.store.getMap(name).putAsync(key, toStoreValue(value));
        } catch (RuntimeException e) {
            cacheErrorHandler.handleCachePutError(e, this, key, value);
        }
    }

    @Override
    public void clear() {
        try {
            this.store.getMap(name).clear();
        } catch (RuntimeException e) {
            cacheErrorHandler.handleCacheClearError(e, this);
        }
    }
}
