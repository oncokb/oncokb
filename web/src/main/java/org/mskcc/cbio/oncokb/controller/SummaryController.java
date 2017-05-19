package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.SummaryQueries;
import org.mskcc.cbio.oncokb.model.SummaryQueryRes;
import org.mskcc.cbio.oncokb.model.VariantQuery;
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

    private String getSummary(VariantQuery variantQuery, String summaryType) {
        String summary = null;

        if (variantQuery != null && variantQuery.getGene() != null) {
            Query query = new Query(variantQuery);
            switch (summaryType) {
                case "gene":
                    summary = SummaryUtils.geneSummary(variantQuery.getGene());
                    break;
                case "oncogenic":
                    summary = SummaryUtils.oncogenicSummary(variantQuery.getGene(), variantQuery.getExactMatchAlteration(), variantQuery.getAlterations(), query);
                    break;
                case "variant":
                    summary = SummaryUtils.variantTumorTypeSummary(variantQuery.getGene(), variantQuery.getExactMatchAlteration(), variantQuery.getAlterations(), query, new HashSet<>(variantQuery.getTumorTypes()));
                    break;
                case "full":
                    summary = SummaryUtils.fullSummary(variantQuery.getGene(), variantQuery.getExactMatchAlteration(), variantQuery.getAlterations(), query, new HashSet<>(variantQuery.getTumorTypes()));
                    break;
                case "variantCustomized":
                    summary = SummaryUtils.variantCustomizedSummary(Collections.singleton(variantQuery.getGene()), variantQuery.getExactMatchAlteration(), variantQuery.getAlterations(), query, new HashSet<>(variantQuery.getTumorTypes()));
                    break;
                case "tumorType":
                    summary = SummaryUtils.tumorTypeSummary(variantQuery.getGene(), query, variantQuery.getAlterations(), new HashSet<>(variantQuery.getTumorTypes()));
                    break;
                default:
                    summary = SummaryUtils.variantTumorTypeSummary(variantQuery.getGene(), variantQuery.getExactMatchAlteration(), variantQuery.getAlterations(), query, new HashSet<>(variantQuery.getTumorTypes()));
                    break;
            }
        }
        return summary;
    }

    private List<String> getSummary(List<VariantQuery> queries, String summaryType) {
        List<String> summaries = new ArrayList<>();
        for (VariantQuery variantQuery : queries) {
            String test = getSummary(variantQuery, summaryType);
            summaries.add(test);
        }
        return summaries;
    }
}


