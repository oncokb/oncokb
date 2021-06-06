package org.mskcc.cbio.oncokb.api.pub.v1;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class CacheFetcher {
    @Cacheable(cacheResolver = "geneCacheResolver")
    public String getVersion(String version) {
        return version;
    }
}
