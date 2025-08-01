package org.mskcc.cbio.oncokb.cache;

import org.springframework.stereotype.Component;

import com.vdurmont.semver4j.Semver;

import static org.mskcc.cbio.oncokb.cache.Constants.REDIS_KEY_SEPARATOR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.SemVer;
import org.mskcc.cbio.oncokb.model.Version;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;

@Component
public class CacheNameResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheNameResolver.class);
    String keyPrefix;
    public CacheNameResolver() {
        String appNameProperty = PropertiesUtils.getProperties("app.name");

        StringBuilder appName = new StringBuilder();
        appName.append(StringUtils.isNotEmpty(appNameProperty) ? appNameProperty : "oncokb-core");

        appName.append("-app:");
        String appVersion = this.getClass().getPackage().getImplementationVersion();
        if (StringUtils.isNotEmpty(appVersion)) {
            appName.append((new SemVer(appVersion, Semver.SemverType.STRICT).getVersion()));
        } else {
            LOGGER.error("Redis cache prefix not setup properly.");
        }

        appName.append("-data:");
        Version dataVersion = new Version();
        dataVersion.setVersion(MainUtils.getDataVersion());
        dataVersion.setDate(MainUtils.getDataVersionDate());
        appName.append(dataVersion.getVersion());

        // Use a versioned cache key prefix to prevent conflicts during deployments.
        // In Kubernetes, old pods may still process requests and write stale data to Redis
        // while new pods are starting up. By including the software and data version in
        // the cache key, we ensure that each deployment is fetching the correct values from Redis.
        this.keyPrefix = appName.toString();
    }

    public String getCacheName(String cacheKey) {
        return this.keyPrefix + REDIS_KEY_SEPARATOR + cacheKey;
    }
}
