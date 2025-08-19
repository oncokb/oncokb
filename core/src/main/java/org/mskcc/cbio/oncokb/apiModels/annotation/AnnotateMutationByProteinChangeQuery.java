package org.mskcc.cbio.oncokb.apiModels.annotation;

import java.util.Objects;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateMutationByProteinChangeQuery extends SomaticAnnotationQuery implements java.io.Serializable{
    private QueryGene gene;
    private String alteration;
    private String consequence;
    private Integer proteinStart;
    private Integer proteinEnd;

    public QueryGene getGene() {
        return gene;
    }

    public void setGene(QueryGene gene) {
        this.gene = gene;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGene(), getAlteration(), getConsequence(), getProteinStart(), getProteinEnd());
    }
}
