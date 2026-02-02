package org.mskcc.cbio.oncokb.model;

public class SomaticAnnotationSearchResult {
    AnnotationSearchQueryType queryType;
    SomaticIndicatorQueryResp indicatorQueryResp;


    public AnnotationSearchQueryType getQueryType() {
        return this.queryType;
    }

    public void setQueryType(AnnotationSearchQueryType queryType) {
        this.queryType = queryType;
    }

    public SomaticIndicatorQueryResp getSomaticIndicatorQueryResp() {
        return this.indicatorQueryResp;
    }

    public void setSomaticIndicatorQueryResp(SomaticIndicatorQueryResp indicatorQueryResp) {
        this.indicatorQueryResp = indicatorQueryResp;
    }

}
