/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author jgao
 */
@Controller
@RequestMapping(value = "/legacy-api/evidence.json")
public class EvidenceController {
    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    List<List<Evidence>> getEvidence(
        HttpMethod method,
        @RequestParam(value = "entrezGeneId", required = false) String entrezGeneId,
        @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,
        @RequestParam(value = "alteration", required = false) String alteration,
        @RequestParam(value = "tumorType", required = false) String tumorType,
        @RequestParam(value = "evidenceType", required = false) String evidenceType,
        @RequestParam(value = "consequence", required = false) String consequence,
        @RequestParam(value = "proteinStart", required = false) String proteinStart,
        @RequestParam(value = "proteinEnd", required = false) String proteinEnd,
        @RequestParam(value = "geneStatus", required = false) String geneStatus,
        @RequestParam(value = "source", required = false) String source,
        @RequestParam(value = "levels", required = false) String levels,
        @RequestParam(value = "highestLevelOnly", required = false) Boolean highestLevelOnly) {

        List<List<Evidence>> evidences = new ArrayList<>();

        Map<String, Object> requestQueries = MainUtils.GetRequestQueries(entrezGeneId, hugoSymbol, alteration,
            tumorType, evidenceType, consequence, proteinStart, proteinEnd, geneStatus, source, levels);

        if (requestQueries == null) {
            return new ArrayList<>();
        }

        List<EvidenceQueryRes> evidenceQueries = EvidenceUtils.processRequest(
            (List<Query>) requestQueries.get("queries"),
            new HashSet<>((List<EvidenceType>) requestQueries.get("evidenceTypes")),
            geneStatus, source, new HashSet<>((List<LevelOfEvidence>) requestQueries.get("levels")), highestLevelOnly);

        if (evidenceQueries != null) {
            for (EvidenceQueryRes query : evidenceQueries) {
                evidences.add(query.getEvidences());
            }
        }

        return evidences;
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<EvidenceQueryRes> getEvidence(
        @RequestBody EvidenceQueries body) {

        List<EvidenceQueryRes> result = new ArrayList<>();
        if (body.getQueries().size() > 0) {
            List<Query> requestQueries = body.getQueries();
            Set<EvidenceType> evidenceTypes = new HashSet<>();

            if (body.getEvidenceTypes() != null) {
                for (String type : body.getEvidenceTypes().split("\\s*,\\s*")) {
                    EvidenceType et = EvidenceType.valueOf(type);
                    evidenceTypes.add(et);
                }
            } else if (body.getEvidenceTypes().isEmpty()) {
                // If the evidenceTypes has been defined but is empty, no result should be returned.
                return result;
            } else {
                evidenceTypes.add(EvidenceType.GENE_SUMMARY);
                evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
            }

            result = EvidenceUtils.processRequest(requestQueries, evidenceTypes, null, body.getSource(),
                body.getLevels(), body.getHighestLevelOnly());
        }

        return result;
    }


}