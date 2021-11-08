package org.mskcc.cbio.oncokb.cache;


public enum CacheCategory {
    GENERAL("general");

    String key;

    CacheCategory(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static CacheCategory getByKey(String key) {
        for (CacheCategory cacheKey : CacheCategory.values()) {
            if (cacheKey.key.equalsIgnoreCase(key)) {
                return cacheKey;
            }
        }
        return null;
    }
}
