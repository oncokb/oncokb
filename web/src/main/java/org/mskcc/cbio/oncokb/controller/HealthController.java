package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.model.health.InMemoryCacheSizes;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/v1/health")
public class HealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);

    /**
     * Checks if system is healthy
     */
    @GetMapping
    public ResponseEntity<Void> systemHealthCheck() {
        Boolean memoryCacheSizeCheckPassed = checkInMemoryCacheSizeConsistency();
        Boolean successfullyFetchedActionableVariants = checkActionableGenesResponse();
        if (!memoryCacheSizeCheckPassed || !successfullyFetchedActionableVariants) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

    private Boolean checkInMemoryCacheSizeConsistency() {
        // Re-fetch MySQL data
        Integer reloadedGeneCacheSize = ApplicationContextSingleton.getGeneBo().countAll();
        Integer reloadedAlterationCacheSize = ApplicationContextSingleton.getAlterationBo().countAll();
        Integer reloadedDrugCacheSize = ApplicationContextSingleton.getDrugBo().countAll();
        Integer reloadedCancerTypeCacheSize = ApplicationContextSingleton.getTumorTypeBo().countAll();
        Integer reloadedEvidenceCacheSize = ApplicationContextSingleton.getEvidenceBo().countAll();

        InMemoryCacheSizes reloadedCacheSize = new InMemoryCacheSizes(reloadedGeneCacheSize, reloadedAlterationCacheSize, reloadedDrugCacheSize, reloadedCancerTypeCacheSize, reloadedEvidenceCacheSize);

        // Compare with sizes of current in-memory caches in CacheUtils.java
        InMemoryCacheSizes currentCacheSizes = CacheUtils.getCurrentCacheSizes();

        List<String> invalidCacheNames = reloadedCacheSize.getDifferentCacheSizes(currentCacheSizes);
        if(!invalidCacheNames.isEmpty()) {
            LOGGER.debug(String.join("\n", invalidCacheNames));
        }
        return invalidCacheNames.isEmpty();
    }

    private Boolean checkActionableGenesResponse() {
        List<ActionableGene> actionableVariants = AlterationUtils.getAllActionableVariants(false);
        Boolean result = !actionableVariants.isEmpty();
        if (result == false) {
            LOGGER.debug("Failed get actionable genes check");
        }
        return result;
    }
}
