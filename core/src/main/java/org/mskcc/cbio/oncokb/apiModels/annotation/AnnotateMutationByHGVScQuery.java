package org.mskcc.cbio.oncokb.apiModels.annotation;

import java.util.Objects;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotateMutationByHGVScQuery extends AnnotationQuery implements java.io.Serializable{
    private String hgvsc;

    public String getHgvsc() {
        return hgvsc;
    }

    public void setHgvsc(String hgvsc) {
        this.hgvsc = hgvsc;
    }

    public String getGene() {
        return hgvsc.split(":")[0];
    }

    public String getAlteration() {
        return hgvsc.split(":")[1];
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getHgvsc());
    }
}
