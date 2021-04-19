package org.mskcc.cbio.oncokb.apiModels.annotation;

import org.mskcc.cbio.oncokb.model.CopyNumberAlterationType;

import java.util.Objects;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */

public class AnnotateCopyNumberAlterationQuery extends AnnotationQuery implements java.io.Serializable {
    private QueryGene gene;
    private CopyNumberAlterationType copyNameAlterationType;

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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGene(), getCopyNameAlterationType());
    }
}
