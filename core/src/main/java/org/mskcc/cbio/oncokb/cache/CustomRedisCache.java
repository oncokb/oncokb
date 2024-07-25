package org.mskcc.cbio.oncokb.cache;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.mskcc.cbio.oncokb.cache.Constants.REDIS_KEY_SEPARATOR;

/**
 * @author Luke Sikina, Hongxin Zhang
 **/
public abstract class CustomRedisCache extends AbstractValueAdaptingCache {
    private static final Logger LOG = LoggerFactory.getLogger(CustomRedisCache.class);
    public static final int INFINITE_TTL = -1;

    protected final String name;
    protected final long ttlMinutes;
    protected final RedissonClient store;
    
    protected CacheErrorHandler cacheErrorHandler;

    /**
     * Create a new ConcurrentMapCache with the specified name.
     * @param name the name of the cache
     */
    public CustomRedisCache(String name, RedissonClient client, long ttlMinutes, CacheErrorHandler cacheErrorHandler) {
        super(true);
        this.name = name;
        this.store = client;
        this.ttlMinutes = ttlMinutes;
        this.cacheErrorHandler = cacheErrorHandler;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final RedissonClient getNativeCache() {
        return this.store;
    }

    @Override
    protected Object lookup(Object key) {
        try {
            return this.store.getBucket(name + REDIS_KEY_SEPARATOR + key).get();
        } catch (RuntimeException e) {
            this.cacheErrorHandler.handleCacheGetError(e, this, key);
            // After CacheErrorHandler handles the error, return null to use non-cached version.
            return null;
        }
    }

    private void asyncRefresh(Object key) {
        if (ttlMinutes != INFINITE_TTL) {
            this.store.getBucket(name + REDIS_KEY_SEPARATOR + key).expireAsync(ttlMinutes, TimeUnit.MINUTES);
        }
    }

    @Override
    public void put(Object key, Object value) {
        try{
            if (ttlMinutes == INFINITE_TTL) {
                this.store.getBucket(name + REDIS_KEY_SEPARATOR + key).setAsync(value);
            } else {
                this.store.getBucket(name + REDIS_KEY_SEPARATOR + key).setAsync(value, ttlMinutes, TimeUnit.MINUTES);
            }
        } catch (RuntimeException e) {
            this.cacheErrorHandler.handleCachePutError(e, this, key, value);
        }

    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Object cached = lookup(key);
        if (cached != null) {
            return toValueWrapper(cached);
        } else {
            put(key, value);
        }
        return toValueWrapper(value);
    }

    @Override
    public void evict(Object key) {
        // no op: Redis handles evictions
    }

    @Override
    public void clear() {
        try {
            this.store.getKeys().deleteByPattern(name + REDIS_KEY_SEPARATOR + "*");
        } catch (RuntimeException e) {
            this.cacheErrorHandler.handleCacheClearError(e, this);
        }
    }

    @Override
    protected Object toStoreValue(Object userValue) {
        if (userValue == null) {
            LOG.warn("Storing null value in cache. That's probably not great.");
            return null;
        }

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut;
        try {
            // serialize to byte array
            objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(userValue);
            objectOut.flush();
            byte[] uncompressedByteArray = byteOut.toByteArray();

            // compress byte array
            byteOut = new ByteArrayOutputStream(uncompressedByteArray.length);
            GZIPOutputStream g = new GZIPOutputStream(byteOut);
            g.write(uncompressedByteArray);
            g.close();
            return byteOut.toByteArray();
        } catch (IOException e) {
            LOG.warn("Error compressing object for cache: ", e);
            return null;
        }
    }

    @Override
    protected Object fromStoreValue(Object storeValue) {
        if (storeValue == null) {
            return null;
        }

        byte[] bytes = (byte[]) storeValue;
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        GZIPInputStream gzipIn;
        try {
            // inflate to byte array
            gzipIn = new GZIPInputStream(byteIn);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                byteOut.write(buffer, 0, len);
            }
            byte[] unzippedBytes = byteOut.toByteArray();

            // deserialize byte array to object
            byteIn = new ByteArrayInputStream(unzippedBytes);
            ObjectInputStream oi = new ObjectInputStream(byteIn);
            return oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOG.warn("Error inflating object from cache: ", e);
            return null;
        }
    }

    @Override
    protected Cache.ValueWrapper toValueWrapper(Object storeValue) {
        return (storeValue != null ? new SimpleValueWrapper(storeValue) : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        // see https://docs.spring.io/spring-framework/docs/4.3.9.RELEASE/spring-framework-reference/html/cache.html#cache-annotations-cacheable-synchronized
        LOG.warn("Sync method was enabled for cacheable. We do not currently support synchronized caching, so this is most likely a mistake.");

		ValueWrapper storeValue = get(key);
		if (storeValue != null) {
			return (T) storeValue.get();
		}

		T value;
        try {
            value = valueLoader.call();
        } catch (Throwable ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
        put(key, value);
        return value;
    }
}
