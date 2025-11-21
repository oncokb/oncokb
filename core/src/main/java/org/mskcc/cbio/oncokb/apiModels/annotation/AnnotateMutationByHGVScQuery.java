package org.mskcc.cbio.oncokb.apiModels.annotation;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class AnnotateMutationByHGVScQuery extends AnnotationQuery implements java.io.Serializable{
    private String hgvsc;
    @JsonUnwrapped
    private GermlineQuery germlineQuery = new GermlineQuery();;

    public String getHgvsc() {
        return hgvsc;
    }

    public void setHgvsc(String hgvsc) {
        this.hgvsc = hgvsc;
    }

    public String getGene() {
        return hgvsc.split(":")[0];
    }

    public String getAlteration() {
        return hgvsc.split(":")[1];
    }

    public GermlineQuery getGermlineQuery() {
        return germlineQuery;
    }

    public void setGermlineQuery(GermlineQuery germlineQuery) {
        this.germlineQuery = germlineQuery;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getHgvsc());
    }
}
