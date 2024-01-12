package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.EvidenceType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Hongxin on 11/09/16.
 */
public class EvidenceTypeUtils {
    public static Set<EvidenceType> getGeneEvidenceTypes() {
        Set<EvidenceType> evidenceTypes = new HashSet<>();
        evidenceTypes.add(EvidenceType.GENE_SUMMARY);
        evidenceTypes.add(EvidenceType.GERMLINE_GENE_SUMMARY);
        evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
        return evidenceTypes;
    }

    public static Set<EvidenceType> getMutationEvidenceTypes() {
        Set<EvidenceType> evidenceTypes = new HashSet<>();
        evidenceTypes.addAll(getSomaticMutationEvidenceTypes());
        evidenceTypes.addAll(getGermlineMutationEvidenceTypes());
        evidenceTypes.add(EvidenceType.VUS);
        evidenceTypes.add(EvidenceType.MUTATION_SUMMARY);
        return evidenceTypes;
    }

    public static Set<EvidenceType> getSomaticMutationEvidenceTypes() {
        Set<EvidenceType> evidenceTypes = new HashSet<>();
        evidenceTypes.add(EvidenceType.ONCOGENIC);
        evidenceTypes.add(EvidenceType.MUTATION_EFFECT);
        return evidenceTypes;
    }

    public static Set<EvidenceType> getGermlineMutationEvidenceTypes() {
        Set<EvidenceType> evidenceTypes = new HashSet<>();
        evidenceTypes.add(EvidenceType.PATHOGENIC);
        evidenceTypes.add(EvidenceType.GERMLINE_VARIANT_PENETRANCE);
        evidenceTypes.add(EvidenceType.GERMLINE_CANCER_RISK);
        evidenceTypes.add(EvidenceType.GERMLINE_INHERITANCE_MECHANISM);
        return evidenceTypes;
    }

    public static Set<EvidenceType> getTumorTypeEvidenceTypes() {
        Set<EvidenceType> evidenceTypes = new HashSet<>();

        evidenceTypes.add(EvidenceType.TUMOR_TYPE_SUMMARY);
        evidenceTypes.add(EvidenceType.DIAGNOSTIC_SUMMARY);
        evidenceTypes.add(EvidenceType.PROGNOSTIC_SUMMARY);


        evidenceTypes.add(EvidenceType.DIAGNOSTIC_IMPLICATION);
        evidenceTypes.add(EvidenceType.PROGNOSTIC_IMPLICATION);

        evidenceTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        evidenceTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);

        evidenceTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE);
        evidenceTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE);
        return evidenceTypes;
    }

    public static Set<EvidenceType> getSensitiveTreatmentEvidenceTypes() {
        Set<EvidenceType> types = new HashSet<>();
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
        return types;
    }

    public static Set<EvidenceType> getTreatmentEvidenceTypes() {
        Set<EvidenceType> types = new HashSet<>();
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
        return types;
    }

    public static Set<EvidenceType> getImplicationEvidenceTypes(){
        Set<EvidenceType> types = new HashSet<>();
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
        types.add(EvidenceType.DIAGNOSTIC_IMPLICATION);
        types.add(EvidenceType.PROGNOSTIC_IMPLICATION);
        return types;
    }

    public static List<EvidenceType> getAllEvidenceTypes() {
        return Arrays.asList(EvidenceType.values());
    }
}
