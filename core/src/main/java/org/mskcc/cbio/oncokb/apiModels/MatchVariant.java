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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchVariant)) return false;

        MatchVariant that = (MatchVariant) o;

        if (getHugoSymbol() != null ? !getHugoSymbol().equals(that.getHugoSymbol()) : that.getHugoSymbol() != null)
            return false;
        return getAlteration() != null ? getAlteration().equals(that.getAlteration()) : that.getAlteration() == null;
    }

    @Override
    public int hashCode() {
        int result = getHugoSymbol() != null ? getHugoSymbol().hashCode() : 0;
        result = 31 * result + (getAlteration() != null ? getAlteration().hashCode() : 0);
        return result;
    }
}
