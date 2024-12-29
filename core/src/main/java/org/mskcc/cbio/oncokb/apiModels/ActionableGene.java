package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Hongxin on 10/28/16.
 */
public class ActionableGene {
    String grch37Isoform;
    String grch37RefSeq;
    String grch38Isoform;
    String grch38RefSeq;
    Integer entrezGeneId;
    String gene;
    String referenceGenome;
    String variant;
    String proteinChange;
    String cancerType;
    String level;
    String drugs;
    String pmids;
    String abstracts;
    String description;

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

    public String getReferenceGenome() {
        return referenceGenome;
    }

    public void setReferenceGenome(String referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActionableGene(String grch37Isoform, String grch37RefSeq, String grch38Isoform, String grch38RefSeq, Integer entrezGeneId, String gene, String referenceGenome, String variant, String proteinChange, String cancerType, String level, String drugs, String pmids, String abstracts, String description) {
        this.grch37Isoform = grch37Isoform;
        this.grch37RefSeq = grch37RefSeq;
        this.grch38Isoform = grch38Isoform;
        this.grch38RefSeq = grch38RefSeq;
        this.entrezGeneId = entrezGeneId;
        this.gene = gene;
        this.referenceGenome = referenceGenome;
        this.variant = variant;
        this.proteinChange = proteinChange;
        this.cancerType = cancerType;
        this.level = level;
        this.drugs = drugs;
        this.pmids = pmids;
        this.abstracts = abstracts;
        this.description = description;
    }

    public ActionableGene() {
    }
}
