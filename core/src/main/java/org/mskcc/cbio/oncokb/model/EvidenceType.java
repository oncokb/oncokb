package org.mskcc.cbio.oncokb.model;

/**
 * @author jgao
 */
public enum EvidenceType {
    GENE_SUMMARY("Gene summary"),
    MUTATION_SUMMARY("Mutation summary"),
    TUMOR_TYPE_SUMMARY("Tumor type summary"),
    GENE_TUMOR_TYPE_SUMMARY("Gene tumor type summary"),
    GENE_BACKGROUND("Gene background"),
    ONCOGENIC("Oncogenic"),
    MUTATION_EFFECT("Mutation effect"),
    VUS("Variant of unknown significance"),
    //    DRUG_SENSITIVITY("Drug sensivity"),
    PREVALENCE("Prevalence"),
    PROGNOSTIC_IMPLICATION("Prognostic implications"),
    NCCN_GUIDELINES("NCCN Guidelines"),
    STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY("Standard therapeutic implications for drug sensitivity"),
    STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE("Standard therapeutic implications for drug resistance"),
    INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY("Investigational therapeutic implications for drug sensitivity"),
    INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE("Investigational therapeutic implications for drug resistance"),
    CLINICAL_TRIAL("clinical trial");

    private EvidenceType(String label) {
        this.label = label;
    }

    private final String label;

    public String label() {
        return label;
    }
}
