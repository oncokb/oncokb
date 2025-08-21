package org.mskcc.cbio.oncokb.apiModels.annotation;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateMutationByHGVSgQuery extends AnnotationQuery implements java.io.Serializable{
    private String hgvsg;
    @JsonUnwrapped
    private GermlineQuery germlineQuery = new GermlineQuery();;

    public String getHgvsg() {
        return hgvsg;
    }

    public void setHgvsg(String hgvsg) {
        this.hgvsg = hgvsg;
    }

    public GermlineQuery getGermlineQuery() {
        return germlineQuery;
    }

    public void setGermlineQuery(GermlineQuery germlineQuery) {
        this.germlineQuery = germlineQuery;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getHgvsg());
    }
}
