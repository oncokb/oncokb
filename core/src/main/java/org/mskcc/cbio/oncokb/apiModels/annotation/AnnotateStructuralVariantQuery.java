package org.mskcc.cbio.oncokb.apiModels.annotation;

import org.mskcc.cbio.oncokb.model.StructuralVariantType;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateStructuralVariantQuery extends AnnotationQuery {
    private GeneQueryPair geneA;
    private GeneQueryPair geneB;
    private StructuralVariantType structuralVariantType;
    private Boolean isFusion = false;
}
