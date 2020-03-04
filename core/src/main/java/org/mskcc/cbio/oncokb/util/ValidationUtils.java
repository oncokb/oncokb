package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.ClinicalVariant;
import org.mskcc.cbio.oncokb.model.Gene;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ValidationUtils {

    public static JSONArray getEmptyClinicalVariants() {
        final String NO_LEVEL = "No level is specified";
        final String NO_REFERENCE = "No reference is specified";
        final String NO_TREATMENT = "No treatment is specified";
        JSONArray data = new JSONArray();
        for (Gene gene : GeneUtils.getAllGenes()) {
            Set<ClinicalVariant> variants = MainUtils.getClinicalVariants(gene);
            for (ClinicalVariant variant : variants) {
                if (StringUtils.isNullOrEmpty(variant.getLevel())) {
                    data.put(getErrorMessage(getTargetByClinicalVariant(variant), NO_LEVEL));
                }
                if (variant.getDrugAbstracts().isEmpty() && variant.getDrugPmids().isEmpty()) {
                    data.put(getErrorMessage(getTargetByClinicalVariant(variant), NO_REFERENCE));
                }
                if (variant.getDrug().isEmpty()) {
                    data.put(getErrorMessage(getTargetByClinicalVariant(variant), NO_TREATMENT));
                }
            }
        }
        return data;
    }

    private static String getTargetByClinicalVariant(ClinicalVariant variant) {
        return getTarget(variant.getVariant().getGene().getHugoSymbol(), variant.getVariant().getAlteration(), TumorTypeUtils.getTumorTypeName(variant.getOncoTreeType()));
    }

    private static JSONObject getErrorMessage(String target, String reason) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("target", target);
        jsonObject.append("reason", reason);
        return jsonObject;
    }

    private static String getTarget(String hugoSymbol, String alteration, String tumorType) {
        List<String> items = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(hugoSymbol)) {
            items.add(hugoSymbol);
        }
        if (!StringUtils.isNullOrEmpty(alteration)) {
            items.add(alteration);
        }
        if (!StringUtils.isNullOrEmpty(tumorType)) {
            items.add(tumorType);
        }
        return String.join("-", items);
    }
}
