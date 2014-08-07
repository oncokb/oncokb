

package org.mskcc.cbio.oncokb.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jgao
 */
public enum LevelOfEvidence {
    LEVEL_1 ("1", "FDA-approved biomarker in approved indication. The alteration is"
            + " used or has been used as an elgibility criterion for clinical"
            + " trials of tis agent or class of agents."),
    LEVEL_2 ("2", "FDA-approved biomarker in unapproved indication. There is clinical"
            + " evidence for an association between this biomarker and response/resistance"
            + " to this agent or class of agents in another tumor type ONLY."),
    LEVEL_3 ("3", "Clinical evidence lining unaproved biomarker to response. There"
            + " is limited clinical evidence (early or conflicting data) for an"
            + " association between this alteration and response/resistance to"
            + " this agent or class of agents."),
    LEVEL_4 ("4", "Preclinical evidence linking unapproved biomarker to response.");
    
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
