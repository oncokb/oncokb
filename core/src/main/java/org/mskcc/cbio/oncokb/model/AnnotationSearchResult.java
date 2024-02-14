package org.mskcc.cbio.oncokb.model;

public class AnnotationSearchResult {
    AnnotationSearchQueryType queryType;
    IndicatorQueryResp indicatorQueryResp;


    public AnnotationSearchQueryType getQueryType() {
        return this.queryType;
    }

    public void setQueryType(AnnotationSearchQueryType queryType) {
        this.queryType = queryType;
    }

    public IndicatorQueryResp getIndicatorQueryResp() {
        return this.indicatorQueryResp;
    }

    public void setIndicatorQueryResp(IndicatorQueryResp indicatorQueryResp) {
        this.indicatorQueryResp = indicatorQueryResp;
    }

}
