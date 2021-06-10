package org.mskcc.cbio.oncokb.cache;

import org.redisson.api.RedissonClient;

public class CustomMapRedisCache extends CustomRedisCache {
    public CustomMapRedisCache(String name, RedissonClient client, long ttlMinutes) {
        super(name, client, ttlMinutes);
    }

    @Override
    protected Object lookup(Object key) {
        Object value = this.store.getMap(name).get(key);
        if (value != null) {
            value = fromStoreValue(value);
        }
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        this.store.getMap(name).putAsync(key, toStoreValue(value));
    }

    @Override
    public void clear() {
        this.store.getMap(name).clear();
    }
}
