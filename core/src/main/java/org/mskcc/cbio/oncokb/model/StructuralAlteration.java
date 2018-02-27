package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin Zhang on 2/12/18.
 */
public enum StructuralAlteration implements OncoKBVariant {
    TRUNCATING_MUTATIONS("Truncating Mutations"),
    FUSIONS("Fusions"),
    AMPLIFICATION("Amplification"),
    DELETION("Deletion");

    private final String mutation;

    StructuralAlteration(String mutation) {
        this.mutation = mutation;
    }

    public String getVariant() {
        return mutation;
    }
}
