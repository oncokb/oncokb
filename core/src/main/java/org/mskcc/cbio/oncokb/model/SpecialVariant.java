package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin Zhang on 2/12/18.
 */
public enum SpecialVariant implements OncoKBVariant {
    OVEREXPRESSION("Overexpression"),
    PROMOTER("Promoter"),
    WILDTYPE("Wildtype");

    private final String mutation;

    SpecialVariant(String mutation) {
        this.mutation = mutation;
    }

    public String getVariant() {
        return mutation;
    }

}
