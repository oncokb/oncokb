package org.mskcc.cbio.oncokb.apiModels.annotation;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotationQuery {
    private String id; //Optional, This id is passed from request. The identifier used to distinguish the query
    private AnnotationQueryType queryType = AnnotationQueryType.REGULAR; //Optional, This id is passed from request. The identifier used to distinguish the query
    private String tumorType;
}
