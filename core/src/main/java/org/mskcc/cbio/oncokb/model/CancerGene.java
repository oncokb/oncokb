package org.mskcc.cbio.oncokb.model;

/**
 * Created by jiaojiao on 6/8/17.
 */
public class CancerGene {
    private String hugoSymbol;
    private String entrezGeneId;
    private Boolean oncokbAnnotated;
    private Integer occurrenceCount;
    private Boolean mSKImpact;
    private Boolean mSKHeme;
    private Boolean foundation;
    private Boolean foundationHeme;
    private Boolean vogelstein;
    private Boolean sangerCGC;

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(String entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Boolean getOncokbAnnotated() {
        return oncokbAnnotated;
    }

    public void setOncokbAnnotated(Boolean oncokbAnnotated) {
        this.oncokbAnnotated = oncokbAnnotated;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public Boolean getmSKImpact() {
        return mSKImpact;
    }

    public void setmSKImpact(Boolean mSKImpact) {
        this.mSKImpact = mSKImpact;
    }

    public Boolean getmSKHeme() {
        return mSKHeme;
    }

    public void setmSKHeme(Boolean mSKHeme) {
        this.mSKHeme = mSKHeme;
    }

    public Boolean getFoundation() {
        return foundation;
    }

    public void setFoundation(Boolean foundation) {
        this.foundation = foundation;
    }

    public Boolean getFoundationHeme() {
        return foundationHeme;
    }

    public void setFoundationHeme(Boolean foundationHeme) {
        this.foundationHeme = foundationHeme;
    }

    public Boolean getVogelstein() {
        return vogelstein;
    }

    public void setVogelstein(Boolean vogelstein) {
        this.vogelstein = vogelstein;
    }

    public Boolean getSangerCGC() {
        return sangerCGC;
    }

    public void setSangerCGC(Boolean sangerCGC) {
        this.sangerCGC = sangerCGC;
    }
}
