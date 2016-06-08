package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin on 5/27/16.
 */
public class OncoTreeType {
    private String code;
    private String subtype;
    private String cancerType;
    private String level;
    private String tissue;

    public OncoTreeType() {}

    public OncoTreeType(String code, String subtype, String cancerType, String level, String tissue) {
        this.code = code;
        this.subtype = subtype;
        this.cancerType = cancerType;
        this.level = level;
        this.tissue = tissue;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getCancerType() {
        return cancerType;
    }

    public void setCancerType(String cancerType) {
        this.cancerType = cancerType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTissue() {
        return tissue;
    }

    public void setTissue(String tissue) {
        this.tissue = tissue;
    }
}
