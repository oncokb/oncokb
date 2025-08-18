package org.mskcc.cbio.oncokb.apiModels;

public enum GeneticType {
    SOMATIC("Somatic"),
    GERMLINE("Germline");

    private String geneticType;

    private GeneticType(String geneticType) {
        this.geneticType = geneticType;
    }

    public String toLowerCase() {
        return geneticType.toLowerCase();
    }
}
