package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public enum EvidenceType {
    GENE_BACKGROUND("Gene background"),
    MUTATION_EFFECT ("Mutation effect"),
//    DRUG_SENSITIVITY("Drug sensivity"),
    PREVALENCE ("Prevalence"),
    PROGNOSTIC_IMPLICATION ("Prognostic implications"),
    STANDARD_THERAPEUTIC_IMPLICATIONS("Standard therapeutic implications"),
    INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS("Investigational therapeutic implications"),
    CLINICAL_TRIAL("clinical trial");

    private EvidenceType(String label) {
        this.label = label;
    }

    private final String label;

    public String label() {
        return label;
    }
}
