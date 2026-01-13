package org.mskcc.cbio.oncokb.apiModels.annotation;

import io.swagger.annotations.ApiModel;
import org.mskcc.cbio.oncokb.model.StructuralVariantType;


import java.util.Objects;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateStructuralVariantQuery extends AnnotationQuery implements java.io.Serializable {
    private QueryGene geneA;
    private QueryGene geneB;
    private StructuralVariantType structuralVariantType;
    private Boolean isFunctionalFusion = false;
    public QueryGene getGeneA() {
        return geneA;
    }

    public void setGeneA(QueryGene geneA) {
        this.geneA = geneA;
    }

    public QueryGene getGeneB() {
        return geneB;
    }

    public void setGeneB(QueryGene geneB) {
        this.geneB = geneB;
    }

    public StructuralVariantType getStructuralVariantType() {
        return structuralVariantType;
    }

    public void setStructuralVariantType(StructuralVariantType structuralVariantType) {
        this.structuralVariantType = structuralVariantType;
    }

    public Boolean getFunctionalFusion() {
        return isFunctionalFusion;
    }

    public void setFunctionalFusion(Boolean functionalFusion) {
        isFunctionalFusion = functionalFusion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGeneA(), getGeneB(), getStructuralVariantType(), isFunctionalFusion);
    }
}
