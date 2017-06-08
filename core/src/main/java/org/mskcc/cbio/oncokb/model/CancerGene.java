package org.mskcc.cbio.oncokb.model;

/**
 * Created by jiaojiao on 6/8/17.
 */
public class CancerGene {
    private String hugoSymbol;
    private String entrezGeneId;
    private Boolean oncokbAnnotated;
    private Integer occurrenceCount;
    private Boolean MSKImpact;
    private Boolean MSKHeme;
    private Boolean foundation;
    private Boolean foundationHeme;
    private Boolean Vogelstein;
    private Boolean SangerCGC;

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

    public Boolean getMSKImpact() {
        return MSKImpact;
    }

    public void setMSKImpact(Boolean MSKImpact) {
        this.MSKImpact = MSKImpact;
    }

    public Boolean getMSKHeme() {
        return MSKHeme;
    }

    public void setMSKHeme(Boolean MSKHeme) {
        this.MSKHeme = MSKHeme;
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
        return Vogelstein;
    }

    public void setVogelstein(Boolean vogelstein) {
        Vogelstein = vogelstein;
    }

    public Boolean getSangerCGC() {
        return SangerCGC;
    }

    public void setSangerCGC(Boolean sangerCGC) {
        SangerCGC = sangerCGC;
    }
}
