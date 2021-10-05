package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Alteration;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Hongxin on 10/28/16.
 */
public class FdaAlteration implements Serializable {
    String level;
    Alteration alteration;
    String cancerType;

    public FdaAlteration() {
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Alteration getAlteration() {
        return alteration;
    }

    public void setAlteration(Alteration alteration) {
        this.alteration = alteration;
    }

    public String getCancerType() {
        return cancerType;
    }

    public void setCancerType(String cancerType) {
        this.cancerType = cancerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FdaAlteration that = (FdaAlteration) o;
        return Objects.equals(level, that.level) && Objects.equals(alteration, that.alteration) && Objects.equals(cancerType, that.cancerType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, alteration, cancerType);
    }
}
