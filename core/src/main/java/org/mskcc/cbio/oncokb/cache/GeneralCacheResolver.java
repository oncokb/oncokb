package org.mskcc.cbio.oncokb.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.ArrayList;
import java.util.Collection;

public class GeneralCacheResolver implements CacheResolver {
    private final CacheManager cacheManager;

    public GeneralCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = new ArrayList<>();
        CacheKey nameKey = CacheKey.getByKey(context.getMethod().getName());
        if (nameKey != null) {
            caches.add(cacheManager.getCache(nameKey.getKey()));
        }
        return caches;
    }
}
