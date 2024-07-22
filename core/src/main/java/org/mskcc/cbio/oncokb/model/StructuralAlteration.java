package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin Zhang on 2/12/18.
 */
public enum StructuralAlteration implements OncoKBVariant {
    TRUNCATING_MUTATIONS("Truncating Mutations"),
    FUSIONS("Fusions"),
    AMPLIFICATION("Amplification"),
    DELETION("Deletion");

    private final String variant;

    StructuralAlteration(String variant) {
        this.variant = variant;
    }

    public String getVariant() {
        return variant;
    }
}
