package org.mskcc.cbio.oncokb.apiModels;

/**
 * Created by Hongxin on 10/28/16.
 */
public class CuratedGene {
    String isoform;
    String refSeq;
    Integer entrezGeneId;
    String hugoSymbol;
    private Boolean TSG;
    private Boolean oncogene;
    String highestSensitiveLevel;
    String highestResistanceLevel;
    String summary;

    public String getIsoform() {
        return isoform;
    }

    public void setIsoform(String isoform) {
        this.isoform = isoform;
    }

    public String getRefSeq() {
        return refSeq;
    }

    public void setRefSeq(String refSeq) {
        this.refSeq = refSeq;
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

    public Boolean getTSG() {
        return TSG;
    }

    public void setTSG(Boolean TSG) {
        this.TSG = TSG;
    }

    public Boolean getOncogene() {
        return oncogene;
    }

    public void setOncogene(Boolean oncogene) {
        this.oncogene = oncogene;
    }

    public String getHighestSensitiveLevel() {
        return highestSensitiveLevel;
    }

    public void setHighestSensitiveLevel(String highestSensitiveLevel) {
        this.highestSensitiveLevel = highestSensitiveLevel;
    }

    public String getHighestResistancLevel() {
        return highestResistanceLevel;
    }

    public void setHighestResistancLevel(String highestResistanceLevel) {
        this.highestResistanceLevel = highestResistanceLevel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public CuratedGene(String isoform, String refSeq, Integer entrezGeneId, String hugoSymbol, Boolean TSG, Boolean oncogene, String highestSensitiveLevel, String highestResistanceLevel, String summary) {
        this.isoform = isoform;
        this.refSeq = refSeq;
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
        this.TSG = TSG;
        this.oncogene = oncogene;
        this.highestSensitiveLevel = highestSensitiveLevel;
        this.highestResistanceLevel = highestResistanceLevel;
        this.summary = summary;
    }

    public CuratedGene() {
    }
}
