

package org.mskcc.cbio.oncokb.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jgao
 */
public enum LevelOfEvidence {
    // Levels for therapeutic implications
    LEVEL_0 ("0", ""),
    LEVEL_1 ("1", "FDA-recognized biomarker predictive of response to an FDA-approved drug in this indication"),
    LEVEL_2A ("2A", "Standard care biomarker predictive of response to an FDA-approved drug in this indication"),
    LEVEL_2B ("2B", "Standard care biomarker predictive of response to an FDA-approved drug in another indication but not standard care for this indication"),
    LEVEL_3A ("3A", "Compelling clinical evidence supports the biomarker as being predictive of response to a drug in this indication"),
    LEVEL_3B ("3B", "Compelling clinical evidence supports the biomarker as being predictive of response to a drug in another indication"),
    LEVEL_4 ("4", "Compelling biological evidence supports the biomarker as being predictive of response to a drug"),
    LEVEL_R1 ("R1", "Standard care biomarker predictive of resistance to an FDA-approved drug in this indication"),
    LEVEL_R2 ("R2", "Compelling clinical evidence supports the biomarker as being predictive of resistance to a drug"),
    LEVEL_R3 ("R3", ""),

    // Levels for prognostic implications
    LEVEL_Px1("Px1", "FDA and/or professional guideline-recognized biomarker prognostic in this indication based on well-powered studie(s)"),
    LEVEL_Px2("Px2", "FDA and/or professional guideline-recognized biomarker prognostic in this indication based on a single or multiple small studies"),
    LEVEL_Px3("Px3", "Biomarker prognostic in this indication based on clinical evidence in well powered studies"),

    // Levels for diagnostic implications
    LEVEL_Dx1("Dx1", "FDA and/or professional guideline-recognized biomarker required for diagnosis in this indication"),
    LEVEL_Dx2("Dx2", "FDA and/or professional guideline-recognized biomarker that supports diagnosis in this indication"),
    LEVEL_Dx3("Dx3", "Biomarker that may assist disease diagnosis in this indication based on clinical evidence");

    LevelOfEvidence(String level, String description) {
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
