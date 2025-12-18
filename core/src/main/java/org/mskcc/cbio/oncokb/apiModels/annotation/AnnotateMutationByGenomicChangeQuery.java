package org.mskcc.cbio.oncokb.apiModels.annotation;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateMutationByGenomicChangeQuery extends AnnotationQuery implements java.io.Serializable {
    private String genomicLocation;
    // @JsonUnwrapped
    @JsonIgnore
    private GermlineQuery germlineQuery = new GermlineQuery();;

    public String getGenomicLocation() {
        return genomicLocation;
    }

    public void setGenomicLocation(String genomicLocation) {
        this.genomicLocation = genomicLocation;
    }

    public GermlineQuery getGermlineQuery() {
        return germlineQuery;
    }

    public void setGermlineQuery(GermlineQuery germlineQuery) {
        this.germlineQuery = germlineQuery;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGenomicLocation());
    }
}
