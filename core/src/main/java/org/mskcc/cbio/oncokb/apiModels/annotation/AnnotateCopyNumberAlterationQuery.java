package org.mskcc.cbio.oncokb.apiModels.annotation;

import org.mskcc.cbio.oncokb.model.CopyNumberAlterationType;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateCopyNumberAlterationQuery extends AnnotationQuery{
    private GeneQueryPair gene;
    private CopyNumberAlterationType copyNameAlterationType;
}
