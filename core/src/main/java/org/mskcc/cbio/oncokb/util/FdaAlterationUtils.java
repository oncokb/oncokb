package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class FdaAlterationUtils {
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
            case LEVEL_3B:
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
