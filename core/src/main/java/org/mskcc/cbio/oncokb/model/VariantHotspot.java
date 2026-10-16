package org.mskcc.cbio.oncokb.model;

import java.io.Serializable;

/**
 * The cancer hotspot annotation of a variant: whether it is a hotspot and, when
 * it is, the kind of hotspot it matches — "single residue", "in-frame indel" or
 * "splice site". The type matters to a caller looking at one hotspot: an
 * in-frame indel covering a single residue hotspot is a hotspot of the range it
 * falls in, not of that residue.
 */
public class VariantHotspot implements Serializable {

    private Boolean isHotspot = false;
    private String type = null;
    private String residue = null;
    private String name = null;

    public Boolean getIsHotspot() {
        return isHotspot;
    }

    public void setIsHotspot(Boolean isHotspot) {
        this.isHotspot = isHotspot;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariantHotspot that = (VariantHotspot) o;

        if (isHotspot != null ? !isHotspot.equals(that.isHotspot) : that.isHotspot != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (residue != null ? !residue.equals(that.residue) : that.residue != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "VariantHotspot{" +
            "isHotspot=" + isHotspot +
            ", type='" + type + '\'' +
            ", residue='" + residue + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
