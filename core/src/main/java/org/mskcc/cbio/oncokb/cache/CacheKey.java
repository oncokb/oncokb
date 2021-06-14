package org.mskcc.cbio.oncokb.cache;


public enum CacheKey {
    CANCER_GENE_LIST("getCancerGenes"),
    CANCER_GENE_LIST_TXT("getCancerGenesTxt"),
    CURATED_GENE_LIST("getCuratedGenes"),
    CURATED_GENE_LIST_TXT("getCuratedGenesTxt"),
    PROCESS_QUERY("processQuery"),
    GENOMIC_LOCATION_QUERY("getIndicatorQueryFromGenomicLocation"),
    ONCOKB_INFO("getOncoKBInfo");

    String key;

    CacheKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static CacheKey getByKey(String key) {
        for (CacheKey cacheKey : CacheKey.values()) {
            if (cacheKey.key.equalsIgnoreCase(key)) {
                return cacheKey;
            }
        }
        return null;
    }
}
