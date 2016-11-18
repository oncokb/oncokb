

package org.mskcc.cbio.oncokb.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jgao
 */
public enum Oncogenicity {
    YES ("1", "Oncogenic"),
    LIKELY ("2", "Likely Oncogenic"),
    LIKELY_NEUTRAL ("0", "Likely Neutral"),
    INCONCLUSIVE ("-1", "Inconclusive");
    
    private Oncogenicity(String oncogenic, String description) {
        this.oncogenic = oncogenic;
        this.description = description;
    }
    
    private final String oncogenic;
    private final String description;

    public String getOncogenic() {
        return oncogenic;
    }

    public String getDescription() {
        return description;
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
