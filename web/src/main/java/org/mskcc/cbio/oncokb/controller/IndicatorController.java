/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.model.EvidenceQueries;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.util.IndicatorUtils;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author jgao
 */
@Controller
@RequestMapping(value = "/legacy-api/indicator.json")
public class IndicatorController {
    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    IndicatorQueryResp getEvidence(
        HttpMethod method,
        @RequestParam(value = "id", required = false) String id,
        @RequestParam(value = "entrezGeneId", required = false) String entrezGeneId,
        @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,
        @RequestParam(value = "alteration", required = false) String alteration,
        @RequestParam(value = "alterationType", required = false) String alterationType,
        @RequestParam(value = "tumorType", required = false) String tumorType,
        @RequestParam(value = "consequence", required = false) String consequence,
        @RequestParam(value = "proteinStart", required = false) String proteinStart,
        @RequestParam(value = "proteinEnd", required = false) String proteinEnd,
        @RequestParam(value = "geneStatus", required = false) String geneStatus,
        @RequestParam(value = "source", required = false) String source,
        @RequestParam(value = "levels", required = false) String levels,
        @RequestParam(value = "highestLevelOnly", required = false) Boolean highestLevelOnly
    ) {

        Query query = new Query();
        query.setId(id);

        if (entrezGeneId != null) {
            query.setEntrezGeneId(Integer.parseInt(entrezGeneId));
        }
        query.setHugoSymbol(hugoSymbol);
        query.setAlteration(alteration);
        query.setAlterationType(alterationType);
        query.setTumorType(tumorType);
        query.setConsequence(consequence);
        if (proteinStart != null) {
            query.setProteinStart(Integer.parseInt(proteinStart));
        }
        if (proteinEnd != null) {
            query.setProteinEnd(Integer.parseInt(proteinEnd));
        }

        Set<LevelOfEvidence> levelOfEvidences = levels == null ? LevelUtils.getPublicAndOtherIndicationLevels() : LevelUtils.parseStringLevelOfEvidences(levels);
        return IndicatorUtils.processQuery(query, geneStatus, levelOfEvidences, source, highestLevelOnly);
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<IndicatorQueryResp> getResult(
        @RequestBody EvidenceQueries body) {

        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null || body.getQueries() == null || body.getQueries().size() == 0) {
            return result;
        }

        String source = body.getSource() == null ? "oncokb" : body.getSource();

        for (Query query : body.getQueries()) {
            result.add(IndicatorUtils.processQuery(query, null,
                body.getLevels() == null ? LevelUtils.getPublicAndOtherIndicationLevels() : body.getLevels(),
                source, body.getHighestLevelOnly()));
        }
        return result;
    }


}
