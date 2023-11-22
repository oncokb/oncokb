package org.mskcc.cbio.oncokb.model;

/**
 * @author jgao
 */
public enum EvidenceType {
    GENE_SUMMARY("Gene summary"),
    GERMLINE_GENE_SUMMARY("Gene summary"),
    MUTATION_SUMMARY("Mutation summary"),
    TUMOR_TYPE_SUMMARY("Tumor type summary"),
    GENE_TUMOR_TYPE_SUMMARY("Gene tumor type summary"),
    PROGNOSTIC_SUMMARY("Prognostic summary"),
    DIAGNOSTIC_SUMMARY("Diagnostic summary"),
    GENE_BACKGROUND("Gene background"),
    ONCOGENIC("Oncogenic"),
    MUTATION_EFFECT("Mutation effect"),
    PATHOGENIC("Pathogenic"),
    GENE_PENETRANCE("Gene Penetrance"),
    GERMLINE_PENETRANCE("Penetrance"),
    GERMLINE_INHERITANCE_MECHANISM("Inheritance Mechanism"),
    GERMLINE_CANCER_RISK("Inheritance Mechanism"),
    VUS("Variant of unknown significance"),
    PROGNOSTIC_IMPLICATION("Prognostic implications"),
    DIAGNOSTIC_IMPLICATION("Diagnostic implications"),
    STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY("Standard therapeutic implications for drug sensitivity"),
    STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE("Standard therapeutic implications for drug resistance"),
    INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY("Investigational therapeutic implications for drug sensitivity"),
    INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE("Investigational therapeutic implications for drug resistance");

    EvidenceType(String label) {
        this.label = label;
    }

    private final String label;

    public String label() {
        return label;
    }
}
