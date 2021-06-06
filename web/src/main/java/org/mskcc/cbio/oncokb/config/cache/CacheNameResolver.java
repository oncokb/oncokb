package org.mskcc.cbio.oncokb.config.cache;

import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.springframework.stereotype.Component;

@Component
public class CacheNameResolver {
    String appName;
    public CacheNameResolver() {
        this.appName = PropertiesUtils.getProperties("app.name");
    }

    public String getCacheName(String cacheKey) {
        return this.appName + "-" + cacheKey;
    }
}
