package org.mskcc.cbio.oncokb.cache;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.*;
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

    /**
     * Create a new ConcurrentMapCache with the specified name.
     * @param name the name of the cache
     */
    public CustomRedisCache(String name, RedissonClient client, long ttlMinutes) {
        super(true);
        this.name = name;
        this.store = client;
        this.ttlMinutes = ttlMinutes;
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
        Object value = this.store.getBucket(name + REDIS_KEY_SEPARATOR + key).get();
        return value;
    }

    private void asyncRefresh(Object key) {
        if (ttlMinutes != INFINITE_TTL) {
            this.store.getBucket(name + REDIS_KEY_SEPARATOR + key).expireAsync(ttlMinutes, TimeUnit.MINUTES);
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (ttlMinutes == INFINITE_TTL) {
            this.store.getBucket(name + REDIS_KEY_SEPARATOR + key).setAsync(value);
        } else {
            this.store.getBucket(name + REDIS_KEY_SEPARATOR + key).setAsync(value, ttlMinutes, TimeUnit.MINUTES);
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
        this.store.getKeys().deleteByPattern(name + REDIS_KEY_SEPARATOR + "*");
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
}
