package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.apiModels.FdaAlteration;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.mskcc.cbio.oncokb.model.InferredMutation.ONCOGENIC_MUTATIONS;

public class FdaAlterationUtils {
    public static Set<FdaAlteration> getAllFdaAlterations() {
        Set<Gene> genes = CacheUtils.getAllGenes();
        Set<FdaAlteration> alterations = new HashSet<>();
        Map<Alteration, Map<String, Set<FdaAlteration>>> resultMap = new HashMap<>();
        for (Gene gene : genes) {
            for (ClinicalVariant clinicalVariant : MainUtils.getClinicalVariants(gene)) {
                LevelOfEvidence level = LevelOfEvidence.getByLevel(clinicalVariant.getLevel());
                if (level == null || !LevelUtils.getTherapeuticLevels().contains(level)) {
                    continue;
                }
                Set<Alteration> mappedAlterations = new HashSet<>();
                if (clinicalVariant.getVariant().getAlteration().startsWith(ONCOGENIC_MUTATIONS.getVariant())) {
                    for (BiologicalVariant annotatedAlt : MainUtils.getBiologicalVariants(gene)) {
                        List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterations(ReferenceGenome.GRCh37, annotatedAlt.getVariant());
                        if(relevantAlterations.contains(clinicalVariant.getVariant())) {
                            mappedAlterations.add(annotatedAlt.getVariant());
                        }
                    }
                } else {
                    mappedAlterations.add(clinicalVariant.getVariant());
                }
                for (Alteration alteration : mappedAlterations) {
                    if (!resultMap.containsKey(alteration)) {
                        resultMap.put(alteration, new HashMap<>());
                    }
                    for (TumorType tumorType : clinicalVariant.getCancerTypes()) {
                        FdaAlteration fdaAlteration = new FdaAlteration();
                        String cancerTypeName = TumorTypeUtils.getTumorTypeName(tumorType);
                        fdaAlteration.setAlteration(alteration);
                        LevelOfEvidence fdaLevel = convertToFdaLevel(LevelOfEvidence.getByLevel(clinicalVariant.getLevel()));
                        if (fdaLevel != null) {
                            fdaAlteration.setLevel(fdaLevel.getLevel());
                        }
                        fdaAlteration.setCancerType(cancerTypeName);
                        if (!resultMap.get(alteration).containsKey(cancerTypeName)) {
                            resultMap.get(alteration).put(cancerTypeName, new HashSet<>());
                        }
                        resultMap.get(alteration).get(cancerTypeName).add(fdaAlteration);
                    }
                }
            }
        }

        for (Map.Entry<Alteration, Map<String, Set<FdaAlteration>>> alterationMap : resultMap.entrySet()) {
            for (Map.Entry<String, Set<FdaAlteration>> cancerTypeMap : alterationMap.getValue().entrySet()) {
                FdaAlteration pickedFdaAlt = cancerTypeMap.getValue().iterator().next();
                if (cancerTypeMap.getValue().size() > 1) {
                    Optional<FdaAlteration> level2 = cancerTypeMap.getValue().stream().filter(fdaAlteration -> fdaAlteration.getLevel().equals(LevelOfEvidence.LEVEL_Fda2.getLevel())).findAny();
                    if (level2.isPresent()) {
                        pickedFdaAlt = level2.get();
                    }
                }
                alterations.add(pickedFdaAlt);
            }
        }
        return alterations;
    }

    public static Set<FdaAlteration> getGeneFdaAlterations(Gene gene) {
        return getAllFdaAlterations().stream().filter(fdaAlt -> fdaAlt.getAlteration().getGene().equals(gene)).collect(Collectors.toSet());
    }

    public static LevelOfEvidence convertToFdaLevel(LevelOfEvidence level) {
        if (level == null) {
            return null;
        }
        switch (level) {
            case LEVEL_1:
            case LEVEL_R1:
            case LEVEL_2:
                return LevelOfEvidence.LEVEL_Fda2;
            case LEVEL_3A:
            case LEVEL_4:
            case LEVEL_R2:
                return LevelOfEvidence.LEVEL_Fda3;
            default:
                return null;
        }
    }

    private static boolean specialFdaL3(ClinicalVariant clinicalVariant) {
        List<String> specialList = Arrays.asList(new String[]{
            "ERBB2&Oncogenic Mutations&Non-Small Cell Lung Cancer&Ado-Trastuzumab Emtansine",
            "ERBB2&Oncogenic Mutations&Non-Small Cell Lung Cancer&Trastuzumab Deruxtecan",
            "EZH2&Oncogenic Mutations {excluding Y646S; Y646H; Y646C; Y646F; Y646N; A682G; A692V}&Follicular Lymphoma&Tazemetostat",
            "FGFR1&Fusions&Myeloid/Lymphoid Neoplasms with FGFR1 Rearrangement&Pemigatinib",
            "MET&Amplification&Non-Small Cell Lung Cancer&Crizotinib",
            "PALB2&Oncogenic Mutations&Pancreatic Adenocarcinoma, Acinar Cell Carcinoma of the Pancreas&Rucaparib",
            "PIK3CA&Oncogenic Mutations {excluding C420R; E542K; E545A; E545D; E545G; E545K; Q546E; Q546R; H1047L; H1047R; H1047Y}&Breast Cancer&Alpelisib + Fulvestrant",
            "EGFR&A763_Y764insFQEA&Non-Small Cell Lung Cancer&Erlotinib",
            "ALK&Fusions&Inflammatory Myofibroblastic Tumor&Crizotinib",
            "ALK&Fusions&Inflammatory Myofibroblastic Tumor&Ceritinib",
            "BRAF&V600E&Ganglioglioma, Pleomorphic Xanthoastrocytoma, Pilocytic Astrocytoma&Cobimetinib+Vemurafenib,",
            "BRAF&V600E&Ganglioglioma, Pleomorphic Xanthoastrocytoma, Pilocytic Astrocytoma&Trametinib+Dabrafenib",
            "BRAF&V600E&Encapsulated Glioma&Dabrafenib + Trametinib, Vemurafenib + Cobimetinib",
            "BRAF&V600E&Diffuse Glioma&Dabrafenib + Trametinib, Vemurafenib + Cobimetinib",
            "BRAF&V600&Langerhans Cell Histiocytosis&Vemurafenib, Dabrafenib",
            "BRAF&Oncogenic Mutations {excluding V600}&Erdheim-Chester Disease&Cobimetinib, Trametinib",
            "BRAF&Oncogenic Mutations {excluding V600}&Langerhans Cell Histiocytosis&Cobimetinib, Trametinib",
            "BRAF&Oncogenic Mutations {excluding V600}&Rosai-Dorfman Disease&Cobimetinib, Trametinib",
            "ARAF&Oncogenic Mutations&Erdheim-Chester Disease&Cobimetinib, Trametinib",
            "RAF1&Oncogenic Mutations&Erdheim-Chester Disease&Cobimetinib, Trametinib",
            "KRAS&Oncogenic Mutations&Erdheim-Chester Disease&Cobimetinib, Trametinib",
            "NRAS&Oncogenic Mutations&Erdheim-Chester Disease&Cobimetinib, Trametinib",
            "MAP2K1&Oncogenic Mutations&Erdheim-Chester Disease&Cobimetinib, Trametinib",
            "MAP2K2&Oncogenic Mutations&Erdheim-Chester Disease&Cobimetinib, Trametinib",
            "ARAF&Oncogenic Mutations&Langerhans Cell Histiocytosis&Cobimetinib, Trametinib",
            "RAF1&Oncogenic Mutations&Langerhans Cell Histiocytosis&Cobimetinib, Trametinib",
            "KRAS&Oncogenic Mutations&Langerhans Cell Histiocytosis&Cobimetinib, Trametinib",
            "NRAS&Oncogenic Mutations&Langerhans Cell Histiocytosis&Cobimetinib, Trametinib",
            "MAP2K1&Oncogenic Mutations&Langerhans Cell Histiocytosis&Cobimetinib, Trametinib",
            "MAP2K2&Oncogenic Mutations&Langerhans Cell Histiocytosis&Cobimetinib, Trametinib",
            "ARAF&Oncogenic Mutations&Rosai-Dorfman Disease&Cobimetinib, Trametinib",
            "RAF1&Oncogenic Mutations&Rosai-Dorfman Disease&Cobimetinib, Trametinib",
            "KRAS&Oncogenic Mutations&Rosai-Dorfman Disease&Cobimetinib, Trametinib",
            "NRAS&Oncogenic Mutations&Rosai-Dorfman Disease&Cobimetinib, Trametinib",
            "MAP2K1&Oncogenic Mutations&Rosai-Dorfman Disease&Cobimetinib, Trametinib",
            "BRCA2&Oncogenic Mutations&Uterine Sarcoma&Niraparib, Olaparib, Rucaparib",
            "PALB2&Oncogenic Mutations&Pancreatic Adenocarcinoma, Acinar Cell Carcinoma of the Pancreas&Rucaparib",
            "BRCA1&Oncogenic Mutations&Pancreatic Adenocarcinoma, Acinar Cell Carcinoma of the Pancreas&Rucaparib",
            "BRCA2&Oncogenic Mutations&Pancreatic Adenocarcinoma, Acinar Cell Carcinoma of the Pancreas&Rucaparib",
            "MAP2K2&Oncogenic Mutations&Rosai-Dorfman Disease&Cobimetinib, Trametinib"
        });

        String separator = "&";
        List<String> queryParts = new ArrayList<>();
        queryParts.add(clinicalVariant.getVariant().getGene().getHugoSymbol());
        queryParts.add(clinicalVariant.getVariant().getAlteration());
        queryParts.add(TumorTypeUtils.getTumorTypesName(clinicalVariant.getCancerTypes()));
        queryParts.add(String.join(", ", clinicalVariant.getDrug().stream().sorted().collect(Collectors.toList())));
        String query = String.join(separator, queryParts);
        return specialList.contains(query);
    }
}
