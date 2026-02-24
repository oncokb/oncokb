

package org.mskcc.cbio.oncokb.model;

import static org.mskcc.cbio.oncokb.Constants.IN_FRAME_DELETION;
import static org.mskcc.cbio.oncokb.Constants.IN_FRAME_INSERTION;
import static org.mskcc.cbio.oncokb.Constants.MISSENSE_VARIANT;

import java.util.HashMap;
import java.util.Map;

public enum MutationType {
    MISSENSE("Missense"),
    INSERTION("Insertion"),
    DELETION("Deletion"),
    OTHER("Other");

    private MutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    private static final Map<String, MutationType> map = new HashMap<String, MutationType>();

    static {
        for (MutationType mutationType : MutationType.values()) {
            map.put(mutationType.getMutationType(), mutationType);
        }
    }

    public static MutationType fromString(String mutationType) {
        return map.get(mutationType);
    }

    public static MutationType fromVariantConsequence(VariantConsequence variantConsequence) {
        if (variantConsequence.getTerm().equals(MISSENSE_VARIANT)) {
            return MutationType.MISSENSE;
        }
        if (variantConsequence.getTerm().equals(IN_FRAME_INSERTION)) {
            return  MutationType.INSERTION;
        }
        if (variantConsequence.getTerm().equals(IN_FRAME_DELETION)) {
            return MutationType.DELETION;
        }
        return null;
    }

    private final String mutationType;

    public String getMutationType() {
        return mutationType;
    }
}
