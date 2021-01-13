package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin Zhang on 12/15/20.
 */
public class RelevantCancerTypeQuery implements java.io.Serializable {
    String mainType;
    String code;

    public String getMainType() {
        return mainType;
    }

    public void setMainType(String mainType) {
        this.mainType = mainType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
