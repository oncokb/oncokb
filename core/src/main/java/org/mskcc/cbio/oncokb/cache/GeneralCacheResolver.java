package org.mskcc.cbio.oncokb.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.ArrayList;
import java.util.Collection;

import static org.mskcc.cbio.oncokb.cache.Constants.REDIS_KEY_SEPARATOR;

public class GeneralCacheResolver implements CacheResolver {
    private final CacheManager cacheManager;

    public GeneralCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = new ArrayList<>();
        caches.add(cacheManager.getCache(CacheCategory.GENERAL.getKey() + REDIS_KEY_SEPARATOR + context.getMethod().getName()));
        return caches;
    }
}
