package org.mskcc.cbio.oncokb.cache;

import org.redisson.api.RedissonClient;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class CustomBucketRedisCache extends CustomRedisCache {
    public CustomBucketRedisCache(String name, RedissonClient client, long ttlMinutes, CacheErrorHandler cacheErrorHandler) {
        super(name, client, ttlMinutes, cacheErrorHandler);
    }
}
