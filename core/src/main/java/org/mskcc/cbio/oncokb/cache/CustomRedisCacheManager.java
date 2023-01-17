package org.mskcc.cbio.oncokb.cache;

import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomRedisCacheManager implements CacheManager {
    private final ConcurrentMap<String, CustomRedisCache> caches = new ConcurrentHashMap<>();
    private final RedissonClient client;
    private final long ttlInMins;
    private CacheNameResolver cacheNameResolver;
    private CacheErrorHandler cacheErrorHandler;

    public CustomRedisCacheManager(RedissonClient client, long ttlInMins, CacheNameResolver cacheNameResolver, CacheErrorHandler cacheErrorHandler) {
        this.client = client;
        this.ttlInMins = ttlInMins;
        this.cacheNameResolver = cacheNameResolver;
        this.cacheErrorHandler = cacheErrorHandler;
    }

    /**
     * Get the cache associated with the given name.
     * <p>Note that the cache may be lazily created at runtime if the
     * native provider supports it.
     *
     * @param name the cache identifier (must not be {@code null})
     * @return the associated cache, or {@code null} if such a cache
     * does not exist or could be not created
     */
    @Override
    public Cache getCache(String name) {
        // !name.toLowerCase().contains("static") is a hack. Sometimes spring calls this getCache method from
        // a place I can't control, so I needed a way in this method to determine whether or not the cache
        // it's getting should have a ttl.
        // In practice, any cache we have that is static should not expire.
        // (I mean, we have two caches, so this isn't rocket science)
        return getCache(name, !name.toLowerCase().contains("static"));
    }

    public void clearAll() {
        // remove all cache within the application
        this.getCache("*").clear();
    }

    public Cache getCache(String name, boolean expires) {
        long clientTTLInMinutes = expires ? ttlInMins : CustomRedisCache.INFINITE_TTL;
        String cacheName = this.cacheNameResolver.getCacheName(name);
        return caches.computeIfAbsent(cacheName, k -> {
            return new CustomBucketRedisCache(cacheName, client, clientTTLInMinutes, cacheErrorHandler);
        });
    }

    /**
     * Get a collection of the cache names known by this manager.
     *
     * @return the names of all caches known by the cache manager
     */
    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
}
