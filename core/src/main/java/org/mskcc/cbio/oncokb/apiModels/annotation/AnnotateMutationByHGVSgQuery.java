package org.mskcc.cbio.oncokb.apiModels.annotation;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateMutationByHGVSgQuery extends AnnotationQuery implements java.io.Serializable{
    private String hgvsg;

    public String getHgvsg() {
        return hgvsg;
    }

    public void setHgvsg(String hgvsg) {
        this.hgvsg = hgvsg;
    }
}
