

package org.mskcc.cbio.oncokb.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jgao
 */
public enum LevelOfEvidence {
    LEVEL_1 ("1",   "FDA-approved biomarker in approved indication"),
    LEVEL_2A ("2a", "FDA-approved biomarker in unapproved indication with NCCN-guideines listing"),
    LEVEL_2B ("2b", "FDA-approved biomarker in unapproved indication"),
    LEVEL_3 ("3", "Clinical evidence linking unapproved biomarker to response"),
    LEVEL_4 ("4", "Preclinical evidence linking unapproved biomarker to response");
    
    private LevelOfEvidence(String level, String description) {
        this.level = level;
        this.description = description;
    }
    
    private final String level;
    private final String description;

    public String getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
    
    private static final Map<String, LevelOfEvidence> map = new HashMap<String, LevelOfEvidence>();
    static {
        for (LevelOfEvidence levelOfEvidence : LevelOfEvidence.values()) {
            map.put(levelOfEvidence.getLevel(), levelOfEvidence);
        }
    }
    
    /**
     *
     * @param level
     * @return
     */
    public static LevelOfEvidence getByLevel(String level) {
        return map.get(level);
    }
}
