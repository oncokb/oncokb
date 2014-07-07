package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public enum EvidenceType {
    GENE_BACKGROUND("Gene background"),
    ACTIVITY ("Activity"),
    DRUG_SENSITIVITY("Drug sensivity"),
    PREVALENCE ("Prevalence");

    private EvidenceType(String label) {
        this.label = label;
    }

    private final String label;

    public String label() {
        return label;
    }
}
