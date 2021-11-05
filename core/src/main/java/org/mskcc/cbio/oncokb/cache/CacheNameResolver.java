package org.mskcc.cbio.oncokb.cache;

import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.springframework.stereotype.Component;

import static org.mskcc.cbio.oncokb.cache.Constants.REDIS_KEY_SEPARATOR;

@Component
public class CacheNameResolver {
    String appName;
    public CacheNameResolver() {
        this.appName = PropertiesUtils.getProperties("app.name");
    }

    public String getCacheName(String cacheKey) {
        return this.appName + REDIS_KEY_SEPARATOR + cacheKey;
    }
}
