/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.*;

/**
 * @author zhangh2
 */
@Controller
public class CacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

    @RequestMapping(value = "/legacy-api/cache/getGeneCache", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Map<String, Object> getGeneCache(
        HttpMethod method,
        @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    ) {
        Map<String, Object> result = new HashedMap();
        Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);

        if (gene != null) {
            result.put("allVars", AlterationUtils.getAllAlterations(null, gene));
            result.put("excludedVars", AlterationUtils.excludeVUS(gene, new ArrayList<>(AlterationUtils.getAllAlterations(null, gene))));

            Map<Alteration, Map<TumorType, Map<LevelOfEvidence, Set<Evidence>>>> evidences = new HashMap<>();
            Set<EvidenceType> evidenceTypes = EvidenceTypeUtils.getTreatmentEvidenceTypes();

            for (Alteration alteration : AlterationUtils.excludeVUS(gene, new ArrayList<>(AlterationUtils.getAllAlterations(null, gene)))) {
                evidences.put(alteration, new HashMap<TumorType, Map<LevelOfEvidence, Set<Evidence>>>());
            }

            Map<Gene, Set<Evidence>> geneEvidences =
                EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypes);

            result.put("geneEvidences", geneEvidences);
            result.put("gene", CacheUtils.getGeneByEntrezId(gene.getEntrezGeneId()));
            result.put("vus", CacheUtils.getVUS(gene.getEntrezGeneId()));
            result.put("cachedGeneAlts", CacheUtils.getAlterations(gene.getEntrezGeneId(), null));
        }

        return result;
    }

    @PostMapping(value = "/legacy-api/cache", produces = "application/json")
    @ResponseBody
    public Map<String, String> handleCacheCommand(
        @RequestParam(value = "cmd", required = true) String cmd,
        @RequestParam(value = "entrezGeneIds", required = false) Set<Integer> entrezGeneIds,
        @RequestParam(value = "propagation", required = false, defaultValue = "false") Boolean propagation
    ) {
        Map<String, String> result = new LinkedHashMap<>();
    
        if (cmd == null || cmd.isEmpty()) {
            result.put("status", "error");
            result.put("message", "Missing 'cmd' parameter");
            return result;
        }
    
        try {
            switch (cmd) {
                case "reset":
                    CacheUtils.resetAll(propagation);
                    result.put("status", "success");
                    result.put("message", "All in memory caches reset");
                    break;
    
                case "updateGene":
                    if (entrezGeneIds == null || entrezGeneIds.isEmpty()) {
                        result.put("status", "error");
                        result.put("message", "Missing or empty 'entrezGeneIds' for updateGene");
                        return result;
                    }
                    CacheUtils.updateGene(entrezGeneIds, propagation);
                    result.put("status", "success");
                    result.put("message", "Updated in memory cache");
                    break;
    
                default:
                    result.put("status", "error");
                    result.put("message", "Invalid command: " + cmd);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("Cache operation failed for cmd={} (propagation={})", cmd, propagation, e);
            result.put("status", "error");
            result.put("message", "Exception during cache operation: " + e.getMessage());
        }
    
        return result;
    }
}
