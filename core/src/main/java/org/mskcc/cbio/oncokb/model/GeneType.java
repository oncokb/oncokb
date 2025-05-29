

package org.mskcc.cbio.oncokb.model;

/**
 * @author zhangh2
 */
public enum GeneType {
    ONCOGENE_AND_TSG("Oncogene and TSG"),
    ONCOGENE("Oncogene"),
    TSG("Tumor Suppressor Gene"),
    UNKNOWN("Unknown"),
    NEITHER("Neither");

    private GeneType(String geneType) {
        this.geneType = geneType;
    }

    private final String geneType;

    public String getGeneType() {
        return geneType;
    }
}
