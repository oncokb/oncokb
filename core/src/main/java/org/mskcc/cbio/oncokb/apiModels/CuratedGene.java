package org.mskcc.cbio.oncokb.apiModels;

/**
 * Created by Hongxin on 10/28/16.
 */
public class CuratedGene {
    String grch37Isoform;
    String grch37RefSeq;
    String grch38Isoform;
    String grch38RefSeq;
    Integer entrezGeneId;
    String hugoSymbol;
    private Boolean TSG;
    private Boolean oncogene;
    String highestSensitiveLevel;
    String highestResistanceLevel;
    String summary;

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

    public String getHighestResistanceLevel() {
        return highestResistanceLevel;
    }

    public void setHighestResistanceLevel(String highestResistanceLevel) {
        this.highestResistanceLevel = highestResistanceLevel;
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

    public CuratedGene(String grch37Isoform, String grch37RefSeq, String grch38Isoform, String grch38RefSeq, Integer entrezGeneId, String hugoSymbol, Boolean TSG, Boolean oncogene, String highestSensitiveLevel, String highestResistanceLevel, String summary) {
        this.grch37Isoform = grch37Isoform;
        this.grch37RefSeq = grch37RefSeq;
        this.grch38Isoform = grch38Isoform;
        this.grch38RefSeq = grch38RefSeq;
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
