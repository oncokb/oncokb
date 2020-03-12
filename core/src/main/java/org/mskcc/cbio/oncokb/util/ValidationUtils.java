package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class ValidationUtils {

    public static JSONArray getMissingTreatmentInfoData() {
        final String NO_LEVEL = "No level is specified";
        final String NO_REFERENCE = "No reference is specified";
        final String NO_TREATMENT = "No treatment is specified";
        JSONArray data = new JSONArray();
        for (Gene gene : GeneUtils.getAllGenes()) {
            Set<Evidence> evidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, EvidenceTypeUtils.getTreatmentEvidenceTypes());
            for (Evidence evidence : evidences) {
                String hugoSymbol = gene.getHugoSymbol();
                String alterationsName = evidence.getAlterations().stream().map(alteration -> alteration.getName()).collect(Collectors.joining(", "));
                String tumorTypeName = TumorTypeUtils.getTumorTypeName(evidence.getOncoTreeType());
                if (evidence.getTreatments().isEmpty()) {
                    data.put(getErrorMessage(getTarget(hugoSymbol, alterationsName, tumorTypeName), NO_TREATMENT));
                } else {
                    String treatmentName = TreatmentUtils.getTreatmentName(evidence.getTreatments());
                    if (evidence.getLevelOfEvidence() == null) {
                        data.put(getErrorMessage(getTarget(hugoSymbol, alterationsName, tumorTypeName, treatmentName), NO_LEVEL));
                    }
                    if (evidence.getArticles().isEmpty()) {
                        data.put(getErrorMessage(getTarget(hugoSymbol, alterationsName, tumorTypeName, treatmentName), NO_REFERENCE));
                    }
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
                data.put(getErrorMessage(getTarget(gene.getHugoSymbol()), MULTIPLE_SUMMARY));
            } else if (evidences.size() == 0) {
                data.put(getErrorMessage(getTarget(gene.getHugoSymbol()), NO_SUMMARY));
            }

            // Background
            evidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, Collections.singleton(EvidenceType.GENE_BACKGROUND));
            if (evidences.size() > 1) {
                data.put(getErrorMessage(getTarget(gene.getHugoSymbol()), MULTIPLE_BACKGROUND));
            } else if (evidences.size() == 0) {
                data.put(getErrorMessage(getTarget(gene.getHugoSymbol()), NO_BACKGROUND));
            }
        }
        return data;
    }

    private static String getTargetByAlteration(Alteration alteration) {
        return getTarget(alteration.getGene().getHugoSymbol(), alteration.getAlteration());
    }

    private static JSONObject getErrorMessage(String target, String reason) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("target", target);
        jsonObject.put("reason", reason);
        return jsonObject;
    }

    private static String getTarget(String hugoSymbol) {
        return getTarget(hugoSymbol, null);
    }

    private static String getTarget(String hugoSymbol, String alteration) {
        return getTarget(hugoSymbol, alteration, null);
    }

    private static String getTarget(String hugoSymbol, String alteration, String tumorType) {
        return getTarget(hugoSymbol, alteration, tumorType, null);
    }

    private static String getTarget(String hugoSymbol, String alteration, String tumorType, String treatment) {
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
        if (!StringUtils.isNullOrEmpty(treatment)) {
            items.add(treatment);
        }
        return String.join(" / ", items);
    }
}
