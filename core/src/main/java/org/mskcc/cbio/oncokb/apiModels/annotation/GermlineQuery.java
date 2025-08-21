package org.mskcc.cbio.oncokb.apiModels.annotation;

public class GermlineQuery implements java.io.Serializable {
    private Boolean isGermline = false;
    private String alleleState;

    public Boolean isGermline() {
        return isGermline;
    }

    public void setGermline(Boolean germline) {
        isGermline = germline;
    }

    public Boolean getGermline() {
        return isGermline;
    }

    public String getAlleleState() {
        return alleleState;
    }

    public void setAlleleState(String alleleState) {
        this.alleleState = alleleState;
    }
}
