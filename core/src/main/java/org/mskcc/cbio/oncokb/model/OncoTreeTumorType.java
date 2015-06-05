package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public class OncoTreeTumorType {
    private String primary;
    private String secondary;
    private String tertiary;
    private String quaternary;

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getSecondary() {
        return secondary;
    }

    public void setSecondary(String secondary) {
        this.secondary = secondary;
    }

    public String getTertiary() {
        return tertiary;
    }

    public void setTertiary(String tertiary) {
        this.tertiary = tertiary;
    }

    public String getQuaternary() {
        return quaternary;
    }

    public void setQuaternary(String quaternary) {
        this.quaternary = quaternary;
    }
}
