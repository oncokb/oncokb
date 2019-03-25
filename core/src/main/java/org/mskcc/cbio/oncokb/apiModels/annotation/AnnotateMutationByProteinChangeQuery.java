package org.mskcc.cbio.oncokb.apiModels.annotation;

import org.mskcc.cbio.oncokb.model.AlterationType;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateMutationByProteinChangeQuery extends AnnotationQuery{
    private GeneQueryPair gene;
    private String alteration;
    private String consequence;
    private Integer proteinStart;
    private Integer proteinEnd;
}
