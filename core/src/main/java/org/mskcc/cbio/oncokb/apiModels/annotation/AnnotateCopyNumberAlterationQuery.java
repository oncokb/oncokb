package org.mskcc.cbio.oncokb.apiModels.annotation;

import org.mskcc.cbio.oncokb.model.CopyNumberAlterationType;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */

public class AnnotateCopyNumberAlterationQuery extends AnnotationQuery implements java.io.Serializable {
    private QueryGene gene;
    private CopyNumberAlterationType copyNameAlterationType;
    @JsonUnwrapped
    private GermlineQuery germlineQuery = new GermlineQuery();

    public QueryGene getGene() {
        return gene;
    }

    public void setGene(QueryGene gene) {
        this.gene = gene;
    }

    public CopyNumberAlterationType getCopyNameAlterationType() {
        return copyNameAlterationType;
    }

    public void setCopyNameAlterationType(CopyNumberAlterationType copyNameAlterationType) {
        this.copyNameAlterationType = copyNameAlterationType;
    }

    public GermlineQuery getGermlineQuery() {
        return germlineQuery;
    }

    public void setGermlineQuery(GermlineQuery germlineQuery) {
        this.germlineQuery = germlineQuery;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGene(), getCopyNameAlterationType());
    }
}
