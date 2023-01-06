package org.mskcc.cbio.oncokb.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Implementation of org.springframework.cache.interceptor.CacheErrorHandler 
 * that logs the error messages when performing Redis operations.
 * Redis will throw a RuntimeException causing our APIs to return HTTP 500 responses, so we defined
 * this class to just log the errors and allow our app fallback to the non-cached version.
 */

public class LoggingCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        System.out.println(String.format("Cache '%s' failed to get entry with key '%s'", cache.getName(), key));
        exception.printStackTrace();
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        System.out.println(String.format("Cache '%s' failed to put entry with key '%s'", cache.getName(), key));
        exception.printStackTrace();
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        System.out.println(String.format("Cache '%s' failed to evict entry with key '%s'", cache.getName(), key));
        exception.printStackTrace();
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        System.out.println(String.format("Cache '%s' failed to clear entries", cache.getName()));
        exception.printStackTrace();
    }
    
}
