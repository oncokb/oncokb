

package org.mskcc.cbio.oncokb.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jgao
 */
public enum LevelOfEvidence {
    LEVEL_0 ("0", "FDA-approved drug in this indication irrespective of gene/variant biomarker"),
    LEVEL_1 ("1", "FDA-approved biomarker and drug association in this indication"),
    LEVEL_2A ("2a", "FDA-approved biomarker and drug association in another indication, and NCCN-compendium listed for this indication"),
    LEVEL_2B ("2b", "FDA-approved biomarker in another indication, but not FDA or NCCN-compendium-listed for this indication"),
    LEVEL_3 ("3", "Clinical evidence links this biomarker to drug response but no FDA-approved or NCCN compendium-listed biomarker and drug association"),
    LEVEL_4 ("4", "Preclinical evidence potentially links this biomarker to response but no FDA-approved or NCCN compendium-listed biomarker and drug association"),
    LEVEL_R1 ("r1", "NCCN-compendium listed biomarker for resistance to a FDA-approved drug"),
    LEVEL_R2 ("r2", "Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance"),
    LEVEL_R3 ("r3", "Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance");
    
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
