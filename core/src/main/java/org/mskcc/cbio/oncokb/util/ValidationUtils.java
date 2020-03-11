package org.mskcc.cbio.oncokb.util;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.mysql.jdbc.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.*;

import java.net.URL;
import java.util.*;

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

    public static JSONArray getEmptyBiologicalVariants() {
        final String NO_ONCOGENECITY = "No oncogenicity is specified";
        final String NO_MUTATION_EFFECT = "No mutation effect is specified";
        final String NO_MUTATION_EFFECT_REFERENCE = "Mutation effect does not have any reference(pmids, abstracts)";
        JSONArray data = new JSONArray();
        for (Gene gene : GeneUtils.getAllGenes()) {
            Set<BiologicalVariant> variants = MainUtils.getBiologicalVariants(gene);
            for (BiologicalVariant variant : variants) {
                if (StringUtils.isNullOrEmpty(variant.getOncogenic())) {
                    data.put(getErrorMessage(getTargetByAlteration(variant.getVariant()), NO_ONCOGENECITY));
                }
                if (StringUtils.isNullOrEmpty(variant.getMutationEffect())) {
                    data.put(getErrorMessage(getTargetByAlteration(variant.getVariant()), NO_MUTATION_EFFECT));
                }
                if (variant.getMutationEffectPmids().isEmpty() && variant.getMutationEffectAbstracts().isEmpty()) {
                    data.put(getErrorMessage(getTargetByAlteration(variant.getVariant()), NO_MUTATION_EFFECT_REFERENCE));
                }
            }
        }
        return data;
    }

    public static JSONArray checkGeneSummaryBackground() {
        final String NO_SUMMARY = "No gene summary is specified";
        final String MULTIPLE_SUMMARY = "Multiple gene summary exist";
        final String NO_BACKGROUND = "No gene background is specified";
        final String MULTIPLE_BACKGROUND = "Multiple gene background exist";
        JSONArray data = new JSONArray();

        for (Gene gene : GeneUtils.getAllGenes()) {
            // Summary
            Set<Evidence> evidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_SUMMARY));
            if (evidences.size() > 1) {
                data.put(getErrorMessage(getTarget(gene.getHugoSymbol(), null, null), MULTIPLE_SUMMARY));
            } else if (evidences.size() == 0) {
                data.put(getErrorMessage(getTarget(gene.getHugoSymbol(), null, null), NO_SUMMARY));
            }

            // Background
            evidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_BACKGROUND));
            if (evidences.size() > 1) {
                data.put(getErrorMessage(getTarget(gene.getHugoSymbol(), null, null), MULTIPLE_BACKGROUND));
            } else if (evidences.size() == 0) {
                data.put(getErrorMessage(getTarget(gene.getHugoSymbol(), null, null), NO_BACKGROUND));
            }
        }
        return data;
    }

    private static String getTargetByClinicalVariant(ClinicalVariant variant) {
        return getTarget(variant.getVariant().getGene().getHugoSymbol(), variant.getVariant().getAlteration(), TumorTypeUtils.getTumorTypeName(variant.getOncoTreeType()));
    }

    private static String getTargetByAlteration(Alteration alteration) {
        return getTarget(alteration.getGene().getHugoSymbol(), alteration.getAlteration(), null);
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
