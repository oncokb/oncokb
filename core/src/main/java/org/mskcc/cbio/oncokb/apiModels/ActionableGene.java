package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Hongxin on 10/28/16.
 */
public class ActionableGene {
    String isoform;
    String refSeq;
    Integer entrezGeneId;
    String gene;
    String variant;
    String cancerType;
    String level;
    String drugs;
    String pmids;
    String abstracts;

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

    public String getCancerType() {
        return cancerType;
    }

    public void setCancerType(String cancerType) {
        this.cancerType = cancerType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDrugs() {
        return drugs;
    }

    public void setDrugs(String drugs) {
        this.drugs = drugs;
    }

    public String getPmids() {
        return pmids;
    }

    public void setPmids(String pmids) {
        this.pmids = pmids;
    }

    public String getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(String abstracts) {
        this.abstracts = abstracts;
    }

    public ActionableGene(String isoform, String refSeq, Integer entrezGeneId, String gene, String variant, String cancerType, String level, String drugs, String pmids, String abstracts) {
        this.isoform = isoform;
        this.refSeq = refSeq;
        this.entrezGeneId = entrezGeneId;
        this.gene = gene;
        this.variant = variant;
        this.cancerType = cancerType;
        this.level = level;
        this.drugs = drugs;
        this.pmids = pmids;
        this.abstracts = abstracts;
    }

    public ActionableGene() {
    }
}
