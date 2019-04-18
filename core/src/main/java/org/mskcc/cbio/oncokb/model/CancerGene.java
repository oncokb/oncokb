package org.mskcc.cbio.oncokb.model;

/**
 * Created by jiaojiao on 6/8/17.
 */
public class CancerGene {
    private String hugoSymbol;
    private Integer entrezGeneId;
    private Boolean oncokbAnnotated = false;
    private Integer occurrenceCount;
    private Boolean mSKImpact = false;
    private Boolean mSKHeme = false;
    private Boolean foundation = false;
    private Boolean foundationHeme = false;
    private Boolean vogelstein = false;
    private Boolean sangerCGC = false;
    private Boolean isOncogene;
    private Boolean isTSG;

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
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

    public Boolean getOncogene() {
        return isOncogene;
    }

    public void setOncogene(Boolean oncogene) {
        isOncogene = oncogene;
    }

    public Boolean getTSG() {
        return isTSG;
    }

    public void setTSG(Boolean TSG) {
        isTSG = TSG;
    }
}
