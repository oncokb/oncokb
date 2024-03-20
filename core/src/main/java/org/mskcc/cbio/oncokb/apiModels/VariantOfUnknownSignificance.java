package org.mskcc.cbio.oncokb.apiModels;

public class VariantOfUnknownSignificance {
    Integer entrezGeneId;
    String gene;
    String variant;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public VariantOfUnknownSignificance(Integer entrezGeneId, String gene, String variant) {
        this.entrezGeneId = entrezGeneId;
        this.gene = gene;
        this.variant = variant;
    }
}
