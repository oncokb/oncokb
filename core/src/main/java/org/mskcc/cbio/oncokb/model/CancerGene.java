package org.mskcc.cbio.oncokb.model;

import java.util.Objects;

/**
 * Created by jiaojiao on 6/8/17.
 */
public class CancerGene {
    private String hugoSymbol;
    private Integer entrezGeneId;
    private String grch37Isoform = "";
    private String grch37RefSeq = "";
    private String grch38Isoform = "";
    private String grch38RefSeq = "";
    private Boolean oncokbAnnotated = false;
    private Integer occurrenceCount = 0;
    private Boolean mSKImpact = false;
    private Boolean mSKHeme = false;
    private Boolean foundation = false;
    private Boolean foundationHeme = false;
    private Boolean vogelstein = false;
    private Boolean sangerCGC = false;
    private Boolean isOncogene = false;
    private Boolean isTSG = false;

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

    public String getGrch37Isoform() {
        return grch37Isoform;
    }

    public void setGrch37Isoform(String grch37Isoform) {
        this.grch37Isoform = grch37Isoform;
    }

    public String getGrch37RefSeq() {
        return grch37RefSeq;
    }

    public void setGrch37RefSeq(String grch37RefSeq) {
        this.grch37RefSeq = grch37RefSeq;
    }

    public String getGrch38Isoform() {
        return grch38Isoform;
    }

    public void setGrch38Isoform(String grch38Isoform) {
        this.grch38Isoform = grch38Isoform;
    }

    public String getGrch38RefSeq() {
        return grch38RefSeq;
    }

    public void setGrch38RefSeq(String grch38RefSeq) {
        this.grch38RefSeq = grch38RefSeq;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CancerGene)) return false;
        CancerGene that = (CancerGene) o;
        return getEntrezGeneId().equals(that.getEntrezGeneId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntrezGeneId());
    }
}
