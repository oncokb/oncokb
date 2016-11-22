package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.SummaryUtils;
import org.mskcc.cbio.oncokb.util.VariantPairUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Summary APIs
 * Currently only variant summary and full summary supported
 * Variant summary: Three sentences summary including alteration, tumor type and drugs info.
 * Full summary: Gene summary + Variant summary
 * <p/>
 * <p/>
 * Created by Hongxin on 8/10/15.
 */
@Controller
@RequestMapping(value = "/legacy-api/summary.json")
public class SummaryController {
    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    List<String> getSummary(
            HttpMethod method,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "entrezGeneId", required = false) String entrezGeneId,
            @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,
            @RequestParam(value = "alteration", required = false) String alteration,
            @RequestParam(value = "tumorType", required = false) String tumorType,
            @RequestParam(value = "consequence", required = false) String consequence,
            @RequestParam(value = "proteinStart", required = false) String proteinStart,
            @RequestParam(value = "proteinEnd", required = false) String proteinEnd,
            @RequestParam(value = "source", required = false) String source) {

        List<String> summaryList = new ArrayList<>();
        List<VariantQuery> variantQueries = VariantPairUtils.getGeneAlterationTumorTypeConsequence(entrezGeneId, hugoSymbol, alteration, tumorType, consequence, proteinStart, proteinEnd, source);
        if (type == null) {
            type = "full";
        }

        summaryList = getSummary(variantQueries, type);
        return summaryList;
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<SummaryQueryRes> getSummary(
            @RequestBody SummaryQueries body) {

        List<SummaryQueryRes> res = new ArrayList<>();
        if (body.getSource() == null) {
            body.setSource("quest");
        }

        if (body.getType() == null) {
            body.setType("full");
        }

        for (Query query : body.getQueries()) {
            SummaryQueryRes queryRes = new SummaryQueryRes();
            queryRes.setId(query.getId());
            VariantQuery variantQuery = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null,
                    query.getHugoSymbol(), query.getAlteration(), query.getTumorType(), query.getConsequence(),
                    query.getProteinStart() != null ? Integer.toString(query.getProteinStart()) : null,
                    query.getProteinEnd() != null ? Integer.toString(query.getProteinEnd()) : null,
                    body.getSource()).get(0);
            queryRes.setSummary(getSummary(variantQuery, body.getType()));
            res.add(queryRes);
        }
        return res;
    }

    private String getSummary(VariantQuery query, String summaryType) {
        String summary = null;

        if (query != null && query.getGene() != null) {
            switch (summaryType) {
                case "variant":
                    summary = SummaryUtils.variantTumorTypeSummary(query.getGene(), query.getAlterations(), query.getQueryAlteration(), new HashSet<OncoTreeType>(query.getTumorTypes()), query.getQueryTumorType());
                    break;
                case "full":
                    summary = SummaryUtils.fullSummary(query.getGene(), query.getAlterations(), query.getQueryAlteration(), new HashSet<OncoTreeType>(query.getTumorTypes()), query.getQueryTumorType());
                    break;
                case "variantCustomized":
                    summary = SummaryUtils.variantCustomizedSummary(Collections.singleton(query.getGene()), query.getAlterations(), query.getQueryAlteration(), new HashSet<OncoTreeType>(query.getTumorTypes()), query.getQueryTumorType());
                    break;
                case "tumorType":
                    summary = SummaryUtils.tumorTypeSummary(query.getGene(), query.getQueryAlteration(), query.getAlterations(), query.getQueryTumorType(), new HashSet<OncoTreeType>(query.getTumorTypes()));
                    break;
                default:
                    summary = SummaryUtils.variantTumorTypeSummary(query.getGene(), query.getAlterations(), query.getQueryAlteration(), new HashSet<OncoTreeType>(query.getTumorTypes()), query.getQueryTumorType());
                    break;
            }
        }
        return summary;
    }

    private List<String> getSummary(List<VariantQuery> queries, String summaryType) {
        List<String> summaries = new ArrayList<>();
        for(VariantQuery query : queries) {
            String test = getSummary(query, summaryType);
            summaries.add(test);
        }
        return summaries;
    }
}


