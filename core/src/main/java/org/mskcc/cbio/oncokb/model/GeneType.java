

package org.mskcc.cbio.oncokb.model;

/**
 * @author zhangh2
 */
public enum GeneType {
    ONCOGENE("Oncogene"),
    TSG("Tumor Suppressor Gene");

    private GeneType(String geneType) {
        this.geneType = geneType;
    }

    private final String geneType;

    public String getGeneType() {
        return geneType;
    }
}
