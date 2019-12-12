

package org.mskcc.cbio.oncokb.model;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jgao
 */
public enum LevelOfEvidence {
    // Levels for therapeutic implications
    LEVEL_0 ("0", "", "", ""),
    LEVEL_1 ("1", "FDA-recognized biomarker predictive of response to an FDA-approved drug in this indication", "<span><b>FDA-recognized</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>", "#33A02C"),
    LEVEL_2 ("2", "Standard care biomarker predictive of response to an FDA-approved drug in this indication", "<span><b>Standard of care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>", "#1F78B4"),
    LEVEL_2A ("2A", "Standard care biomarker predictive of response to an FDA-approved drug in this indication", "<span><b>Standard of care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>", "#1F78B4"),
    LEVEL_2B ("2B", "Standard care biomarker predictive of response to an FDA-approved drug in another indication but not standard care for this indication", "<span><b>Standard of care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in another indication</b> but not standard of care for this indication</span>", "#80B1D3"),
    LEVEL_3A ("3A", "Compelling clinical evidence supports the biomarker as being predictive of response to a drug in this indication", "<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in this indication</b> but neither biomarker and drug are standard of care</span>", "#984EA3"),
    LEVEL_3B ("3B", "Compelling clinical evidence supports the biomarker as being predictive of response to a drug in another indication", "<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in another indication</b> but neither biomarker and drug are standard of care</span>", "#BE98CE"),
    LEVEL_4 ("4", "Compelling biological evidence supports the biomarker as being predictive of response to a drug", "<span><b>Compelling biological evidence</b> supports the biomarker as being predictive of response to a drug but neither biomarker and drug are standard of care</span>", "#424242"),
    LEVEL_R1 ("R1", "Standard care biomarker predictive of resistance to an FDA-approved drug in this indication", "<span><b>Standard of care</b> biomarker predictive of <b>resistance</b> to an <b>FDA-approved</b> drug <b>in this indication</b></span>", "#EE3424"),
    LEVEL_R2 ("R2", "Compelling clinical evidence supports the biomarker as being predictive of resistance to a drug", "<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of <b>resistance</b> to a drug</span>", "#F79A92"),
    LEVEL_R3 ("R3", "", "", "#FCD6D3"),

    // Levels for prognostic implications
    LEVEL_Px1("Px1", "FDA and/or professional guideline-recognized biomarker prognostic in this indication based on well-powered studie(s)", "FDA and/or professional guideline-recognized biomarker prognostic in this indication based on well-powered studie(s)", ""),
    LEVEL_Px2("Px2", "FDA and/or professional guideline-recognized biomarker prognostic in this indication based on a single or multiple small studies", "FDA and/or professional guideline-recognized biomarker prognostic in this indication based on a single or multiple small studies", ""),
    LEVEL_Px3("Px3", "Biomarker prognostic in this indication based on clinical evidence in well powered studies", "Biomarker prognostic in this indication based on clinical evidence in well powered studies", ""),

    // Levels for diagnostic implications
    LEVEL_Dx1("Dx1", "FDA and/or professional guideline-recognized biomarker required for diagnosis in this indication", "FDA and/or professional guideline-recognized biomarker required for diagnosis in this indication", ""),
    LEVEL_Dx2("Dx2", "FDA and/or professional guideline-recognized biomarker that supports diagnosis in this indication", "FDA and/or professional guideline-recognized biomarker that supports diagnosis in this indication", ""),
    LEVEL_Dx3("Dx3", "Biomarker that may assist disease diagnosis in this indication based on clinical evidence", "Biomarker that may assist disease diagnosis in this indication based on clinical evidence", ""),
    ;
    LevelOfEvidence(String level, String description, String htmlDescription, String colorHex) {
        this.level = level;
        this.description = description;
        this.htmlDescription = htmlDescription;
        this.colorHex = colorHex;
    }

    private final String level;
    private final String description;
    private final String htmlDescription;
    private final String colorHex;

    public String getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public String getColorHex() {
        return colorHex;
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
