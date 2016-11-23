

package org.mskcc.cbio.oncokb.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jgao
 */
public enum Oncogenicity {
    YES ("Oncogenic"),
    LIKELY ("Likely Oncogenic"),
    LIKELY_NEUTRAL ("Likely Neutral"),
    INCONCLUSIVE ("Inconclusive");
    
    private Oncogenicity(String oncogenic) {
        this.oncogenic = oncogenic;
    }
    
    private final String oncogenic;

    public String getOncogenic() {
        return oncogenic;
    }
    
    private static final Map<String, Oncogenicity> map = new HashMap<String, Oncogenicity>();
    static {
        for (Oncogenicity levelOfEvidence : Oncogenicity.values()) {
            map.put(levelOfEvidence.getOncogenic(), levelOfEvidence);
        }
    }
    
    
    /**
     *
     * @param level
     * @return
     */
    public static Oncogenicity getByLevel(String level) {
        return map.get(level);
    }

}
