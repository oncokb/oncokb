package org.mskcc.cbio.oncokb.cache;

import org.redisson.api.RedissonClient;

public class CustomBucketRedisCache extends CustomRedisCache {
    public CustomBucketRedisCache(String name, RedissonClient client, long ttlMinutes) {
        super(name, client, ttlMinutes);
    }
}
