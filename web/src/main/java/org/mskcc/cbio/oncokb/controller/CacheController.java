/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author zhangh2
 */
@Controller
public class CacheController {
    @RequestMapping(value = "/legacy-api/cache", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Map<String, String> getAlteration(
        HttpMethod method,
        @RequestParam(value = "cmd", required = false) String cmd
    ) {
        Map<String, String> result = new HashMap<>();
        if (cmd != null) {
            switch (cmd) {
                case "getStatus":
                    result.put("status", getStatus());
                    break;
                default:
                    break;
            }
        }

        return result;
    }

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
            result.put("allVars", AlterationUtils.getAllAlterations(gene));
            result.put("excludedVars", AlterationUtils.excludeVUS(gene, new ArrayList<>(AlterationUtils.getAllAlterations(gene))));

            Map<Alteration, Map<TumorType, Map<LevelOfEvidence, Set<Evidence>>>> evidences = new HashMap<>();
            Set<EvidenceType> evidenceTypes = EvidenceTypeUtils.getTreatmentEvidenceTypes();

            for (Alteration alteration : AlterationUtils.excludeVUS(gene, new ArrayList<>(AlterationUtils.getAllAlterations(gene)))) {
                evidences.put(alteration, new HashMap<TumorType, Map<LevelOfEvidence, Set<Evidence>>>());
            }

            Map<Gene, Set<Evidence>> geneEvidences =
                EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypes);

            result.put("geneEvidences", geneEvidences);
            result.put("gene", CacheUtils.getGeneByEntrezId(gene.getEntrezGeneId()));
            result.put("vus", CacheUtils.getVUS(gene.getEntrezGeneId()));
            result.put("cachedGeneAlts", CacheUtils.getAlterations(gene.getEntrezGeneId()));
        }

        return result;
    }

    @RequestMapping(value = "/legacy-api/cache", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Map<String, String> postAlteration(
        HttpMethod method,
        @RequestParam(value = "cmd", required = false) String cmd,
        @RequestParam(value = "entrezGeneIds", required = false) Set<Integer> entrezGeneIds,
        @RequestParam(value = "propagation", required = false, defaultValue = "false") Boolean propagation
    ) {
        Map<String, String> result = new HashMap<>();
        if (cmd != null) {
            switch (cmd) {
                case "reset":
                    resetCache(propagation);
                    break;
                case "enable":
                    disableCache(false);
                    break;
                case "disable":
                    disableCache(true);
                    break;
                case "updateGene":
                    CacheUtils.updateGene(entrezGeneIds, propagation);
                    break;
                case "updateAbbreviationOntology":
                    NamingUtils.cacheAllAbbreviations();
                    break;
                default:
                    break;
            }
        }
        result.put("status", "success");
        return result;
    }

    private String getStatus() {
        return CacheUtils.getCacheUtilsStatus();
    }

    private Boolean resetCache(Boolean propagation) {
        Boolean operation = true;
        try {
            CacheUtils.resetAll(propagation);
        } catch (Exception e) {
            operation = false;
        }
        return operation;
    }

    private Boolean disableCache(Boolean cmd) {
        Boolean operation = true;
        try {
            if (cmd) {
                CacheUtils.disableCacheUtils();
            } else {
                CacheUtils.enableCacheUtils();
            }
        } catch (Exception e) {
            operation = false;
        }
        return operation;
    }
}
