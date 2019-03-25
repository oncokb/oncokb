package org.mskcc.cbio.oncokb.apiModels.annotation;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateMutationByGenomicChangeQuery extends AnnotationQuery implements java.io.Serializable {
    private String genomicLocation;

    public String getGenomicLocation() {
        return genomicLocation;
    }

    public void setGenomicLocation(String genomicLocation) {
        this.genomicLocation = genomicLocation;
    }
}
