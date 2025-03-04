package org.mskcc.cbio.oncokb.controller;


import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.health.InMemoryCacheSizes;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/health")
public class HealthController {

    /**
     * Checks if the in-memory caches initilized during application startup is consistent.
     * @return true if the cache size is consistent, otherwise false
     */
    @GetMapping("/cache-size")
    public ResponseEntity<Void> checkInMemoryCacheSizes() {
        // Re-fetch MySQL data
        Set<Gene> reloadedGenes = new HashSet<>(ApplicationContextSingleton.getGeneBo().findAll());
        Integer reloadedGeneCacheSize = (new HashSet<>(ApplicationContextSingleton.getGeneBo().findAll())).size();
        Integer reloadedAlterationCacheSize = ApplicationContextSingleton.getAlterationBo().findAll().size();
        Integer reloadedDrugCacheSize = new HashSet<>(ApplicationContextSingleton.getDrugBo().findAll()).size();
        Integer reloadedCancerTypeCacheSize = ApplicationContextSingleton.getTumorTypeBo().findAll().size();

        Map<Gene, List<Evidence>> mappedEvidence = EvidenceUtils.separateEvidencesByGene(reloadedGenes, new HashSet<>(ApplicationContextSingleton.getEvidenceBo().findAll()));
        Integer reloadedGeneEvidenceCacheSize = mappedEvidence.size();

        InMemoryCacheSizes reloadedCacheSize = new InMemoryCacheSizes(reloadedGeneCacheSize, reloadedAlterationCacheSize, reloadedDrugCacheSize, reloadedCancerTypeCacheSize, reloadedGeneEvidenceCacheSize);

        // Compare with sizes of current in-memory caches in CacheUtils.java
        InMemoryCacheSizes currentCacheSizes = CacheUtils.getCurrentCacheSizes();

        List<String> invalidCacheNames = reloadedCacheSize.getDifferentCacheSizes(currentCacheSizes);
        if (!invalidCacheNames.isEmpty()) {
            System.out.println(String.join("\n", invalidCacheNames));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}
