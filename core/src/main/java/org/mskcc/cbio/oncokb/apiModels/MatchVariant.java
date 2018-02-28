package org.mskcc.cbio.oncokb.apiModels;

/**
 * Created by Hongxin Zhang on 2/13/18.
 */
public class MatchVariant implements java.io.Serializable {
    String hugoSymbol;
    String alteration;

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }
}
