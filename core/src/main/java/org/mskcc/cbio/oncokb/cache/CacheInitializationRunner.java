package org.mskcc.cbio.oncokb.cache;

import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CacheInitializationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheInitializationRunner.class);

    @PostConstruct
    public void initialize() {
        // When CacheUtils runs initializeCaches() inside its static initializer, the JVM marks the class as initializing.
        // Inside initializeCaches() we start parallel tasks. Those tasks call static methods on CacheUtils (e.g., setAllAlterations, cacheAllEvidencesByGenes).
        // But the JVM enforces a class initialization lock, meaning any other thread that touches CacheUtils while it is still
        // initializing must wait until initialization completes.
        LOGGER.info("Cache initialization bean starting");
        CacheUtils.initializeCaches();
        LOGGER.info("Cache initialization bean completed");
    }
}
