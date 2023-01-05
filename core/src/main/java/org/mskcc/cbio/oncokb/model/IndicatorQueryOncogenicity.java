package org.mskcc.cbio.oncokb.model;

public class IndicatorQueryOncogenicity {
    private Oncogenicity oncogenicity;
    private Evidence oncogenicityEvidence;

    public IndicatorQueryOncogenicity(Oncogenicity oncogenicity, Evidence oncogenicityEvidence) {
        this.oncogenicity = oncogenicity;
        this.oncogenicityEvidence = oncogenicityEvidence;
    }

    public Oncogenicity getOncogenicity() {
        return oncogenicity;
    }

    public Evidence getOncogenicityEvidence() {
        return oncogenicityEvidence;
    }
}
