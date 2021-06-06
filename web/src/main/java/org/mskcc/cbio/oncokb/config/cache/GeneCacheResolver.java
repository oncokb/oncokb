package org.mskcc.cbio.oncokb.config.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.ArrayList;
import java.util.Collection;

public class GeneCacheResolver implements CacheResolver {
    private final CacheManager cacheManager;
    private final CacheNameResolver cacheNameResolver;

    public GeneCacheResolver(CacheManager cacheManager, CacheNameResolver cacheNameResolver) {
        this.cacheManager = cacheManager;
        this.cacheNameResolver = cacheNameResolver;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = new ArrayList<>();

        if (context.getMethod().getName() == "findByEntrezGeneId") {
            caches.add(cacheManager.getCache(this.cacheNameResolver.getCacheName(CacheKeys.GENES_BY_ENTREZ_GENE_ID)));
        } else if (context.getMethod().getName() == "findByHugoSymbol") {
            caches.add(cacheManager.getCache(this.cacheNameResolver.getCacheName(CacheKeys.GENES_BY_HUGO_SYMBOL)));
        } else if (context.getMethod().getName() == "findByName") {
            caches.add(cacheManager.getCache(this.cacheNameResolver.getCacheName(CacheKeys.GENE_ALIASES_BY_NAME)));
        } else if (context.getMethod().getName() == "getGenes") {
            caches.add(cacheManager.getCache(this.cacheNameResolver.getCacheName(CacheKeys.CANCER_GENE_LIST)));
        } else{
            caches.add(cacheManager.getCache(context.getMethod().getName()));
        }

        return caches;
    }
}
