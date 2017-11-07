/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.model.EvidenceQueries;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.service.JsonResultFactory;
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
        @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId,
        @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,
        @RequestParam(value = "alteration", required = false) String alteration,
        @RequestParam(value = "alterationType", required = false) String alterationType,
        @RequestParam(value = "tumorType", required = false) String tumorType,
        @RequestParam(value = "consequence", required = false) String consequence,
        @RequestParam(value = "proteinStart", required = false) Integer proteinStart,
        @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd,
        @RequestParam(value = "geneStatus", required = false) String geneStatus,
        @RequestParam(value = "source", required = false) String source,
        @RequestParam(value = "levels", required = false) String levels,
        @RequestParam(value = "queryType", required = false) String queryType,
        @RequestParam(value = "highestLevelOnly", required = false) Boolean highestLevelOnly,
        @RequestParam(value = "fields", required = false) String fields,
        @RequestParam(value = "hgvs", required = false) String hgvs
    ) {
        Query query = new Query(id, queryType, entrezGeneId, hugoSymbol, alteration, alterationType, tumorType, consequence, proteinStart, proteinEnd, hgvs);
        Set<LevelOfEvidence> levelOfEvidences = levels == null ? LevelUtils.getPublicAndOtherIndicationLevels() : LevelUtils.parseStringLevelOfEvidences(levels);
        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, geneStatus, levelOfEvidences, source, highestLevelOnly);

        return JsonResultFactory.getIndicatorQueryResp(resp, fields);
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<IndicatorQueryResp> getResult(
        @RequestBody EvidenceQueries body,
        @RequestParam(value = "fields", required = false) String fields
    ) {

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

        return JsonResultFactory.getIndicatorQueryResp(result, fields);
    }


}
