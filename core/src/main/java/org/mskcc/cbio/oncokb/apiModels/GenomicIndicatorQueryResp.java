
package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "The genomic indicators associated with a queried gene and variant")
public class GenomicIndicatorQueryResp implements Serializable {
    @ApiModelProperty(value = "The query the genomic indicators were retrieved for")
    private GenomicIndicatorQuery query;

    @ApiModelProperty(value = "The germline genomic indicators associated with the query")
    private List<GenomicIndicator> genomicIndicators = new ArrayList<>();

    public GenomicIndicatorQuery getQuery() {
        return query;
    }

    public void setQuery(GenomicIndicatorQuery query) {
        this.query = query;
    }

    public List<GenomicIndicator> getGenomicIndicators() {
        return genomicIndicators;
    }

    public void setGenomicIndicators(List<GenomicIndicator> genomicIndicators) {
        this.genomicIndicators = genomicIndicators;
    }
}
