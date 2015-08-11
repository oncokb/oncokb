package org.mskcc.cbio.oncokb.controller;

import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.SummaryUtils;
import org.mskcc.cbio.oncokb.util.VariantPairUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 */
@Controller
public class SummaryController {
    @RequestMapping(value="/summary.json")
    public @ResponseBody
    List<String> getEvidence(
            HttpMethod method,
            @RequestParam(value="type", required=false) String type,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol,
            @RequestParam(value="alteration", required=false) String alteration,
            @RequestParam(value="tumorType", required=false) String tumorType,
            @RequestParam(value="consequence", required=false) String consequence,
            @RequestParam(value="source", required=false) String source,
            @RequestBody String body) {

        if(body != null && !body.isEmpty()){
            JSONObject params = new JSONObject(body);
            if(params.has("type")) {
                type = params.getString("type");
            }
            if(params.has("hugoSymbol")) {
                hugoSymbol = params.getString("hugoSymbol");
            }
            if(params.has("alteration")) {
                alteration = params.getString("alteration");
            }
            if(params.has("tumorType")) {
                tumorType = params.getString("tumorType");
            }
            if(params.has("consequence")) {
                consequence = params.getString("consequence");
            }
            if(params.has("source")) {
                source = params.getString("source");
            }
        }

        List<String> summaryList = new ArrayList<>();

        if(type != null) {
            switch (type) {
                case "variant":
                    summaryList = variantSummary(hugoSymbol, alteration, tumorType, consequence, source);
                    break;
            }
        }
        return summaryList;
    }

    private List<String> variantSummary (String gene, String alteration, String tumorType, String consequence, String tumorTypeSource) {
        List<String> summaryList = new ArrayList<>();
        List<Map<String, Object>> variantPairs = VariantPairUtils.getGeneAlterationTumorTypeConsequence(gene, alteration, tumorType, consequence, tumorTypeSource);

        if(variantPairs != null) {
            for(Map<String, Object> variantPair : variantPairs) {
                summaryList.add(SummaryUtils.variantSummary((Gene) variantPair.get("gene"), (List<Alteration>) variantPair.get("alterations"), AlterationUtils.getVariantName((Gene) variantPair.get("gene") == null?(String)variantPair.get("queryGene"):((Gene)variantPair.get("gene")).getHugoSymbol(), (String) variantPair.get("queryAlt")), (Set<TumorType>) variantPair.get("tumorTypes"), (String) variantPair.get("queryTumorType")));
            }
        }
        return summaryList;
    }
}
