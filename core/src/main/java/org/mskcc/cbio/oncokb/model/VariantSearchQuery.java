package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin Zhang on 7/18/17.
 */
public class VariantSearchQuery implements java.io.Serializable {
    private Integer entrezGeneId;
    private String hugoSymbol;
    private String variant;
    private String variantType;
    private String consequence;
    private Integer proteinStart;
    private Integer proteinEnd;
    private String hgvs;
    private ReferenceGenome referenceGenome;

    public VariantSearchQuery() {
    }

    public VariantSearchQuery(Integer entrezGeneId, String hugoSymbol, String variant, String variantType, String consequence, Integer proteinStart, Integer proteinEnd, String hgvs, ReferenceGenome referenceGenome) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
        this.variant = variant;
        this.variantType = variantType;
        this.consequence = consequence;
        this.proteinStart = proteinStart;
        this.proteinEnd = proteinEnd;
        this.hgvs = hgvs;
        this.referenceGenome = referenceGenome;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getConsequence() {
        return consequence;
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }

    public Integer getProteinStart() {
        return proteinStart;
    }

    public void setProteinStart(Integer proteinStart) {
        this.proteinStart = proteinStart;
    }

    public Integer getProteinEnd() {
        return proteinEnd;
    }

    public void setProteinEnd(Integer proteinEnd) {
        this.proteinEnd = proteinEnd;
    }

    public String getHgvs() {
        return hgvs;
    }

    public void setHgvs(String hgvs) {
        this.hgvs = hgvs;
    }

    public ReferenceGenome getReferenceGenome() {
        return referenceGenome;
    }

    public void setReferenceGenome(ReferenceGenome referenceGenome) {
        this.referenceGenome = referenceGenome;
    }
}
