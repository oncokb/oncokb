

package org.mskcc.cbio.oncokb.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jgao
 */
public enum LevelOfEvidence {
    LEVEL_0 ("0", "FDA-approved drug in this indication irrespective of gene/variant biomarker"),
    LEVEL_1 ("1", "FDA-approved biomarker and drug in this indication"),
    LEVEL_2A ("2A", "Standard of care biomarker predictive of response to an FDA-approved drug in this indication"),
    LEVEL_2B ("2B", "Standard of care biomarker predictive of response to an FDA-approved drug in another indication but not standard of care for this indication"),
    LEVEL_3 ("3", "Clinical evidence links this biomarker to drug response but no FDA-approved or NCCN compendium-listed biomarker and drug association"),
    LEVEL_3A ("3A", "Compelling clinical evidence supports the biomarker as being predictive of response to a drug in this indication but neither biomarker and drug are standard of care"),
    LEVEL_3B ("3B", "Compelling clinical evidence supports the biomarker as being predictive of response to a drug in another indication but neither biomarker and drug are standard of care"),
    LEVEL_4 ("4", "Compelling biological evidence supports the biomarker as being predictive of response to a drug but neither biomarker and drug are standard of care"),
    LEVEL_R1 ("R1", "Standard of care biomarker predictive of resistance to an FDA-approved drug in this indication"),
    LEVEL_R2 ("R2", "Compelling clinical evidence supports the biomarker as being predictive of resistance to a drug, but neither biomarker nor drug are standard care"),
    LEVEL_R3 ("R3", "Compelling biological evidence supports the biomarker as being predictive of resistance to a drug, but neither biomarker nor drug are standard care");

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

    public static LevelOfEvidence getByName(String name) {
        for (LevelOfEvidence levelOfEvidence : LevelOfEvidence.values()) {
            if(levelOfEvidence.name().equals(name)) {
                return levelOfEvidence;
            }
        }
        return null;
    }
}
