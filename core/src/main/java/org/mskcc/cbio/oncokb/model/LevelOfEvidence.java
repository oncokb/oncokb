

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
    LEVEL_1 ("1", "FDA-recognized biomarker predictive of response to an FDA-approved drug in this indication", "<span><b>FDA-recognized</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>", "#33A02C"),
    LEVEL_2 ("2", "Standard care biomarker recommended by the NCCN or other expert panels predictive of response to an FDA-approved drug in this indication", "<span><b>Standard care</b> biomarker recommended by the NCCN or other expert panels predictive of response to an <b>FDA-approved drug</b> in this indication</span>", "#1F78B4"),
    LEVEL_3A ("3A", "Compelling clinical evidence supports the biomarker as being predictive of response to a drug in this indication", "<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in this indication</b> but neither biomarker and drug are standard of care</span>", "#984EA3"),
    LEVEL_3B ("3B", "Standard care or investigational biomarker predictive of response to an FDA-approved or investigational drug in another indication", "<span><b>Standard care</b> or <b>investigational</b> biomarker predictive of response to an <b>FDA-approved</b> or <b>investigational</b> drug in another indication</span>", "#BE98CE"),
    LEVEL_4 ("4", "Compelling biological evidence supports the biomarker as being predictive of response to a drug", "<span><b>Compelling biological evidence</b> supports the biomarker as being predictive of response to a drug but neither biomarker and drug are standard of care</span>", "#424242"),
    LEVEL_R1 ("R1", "Standard care biomarker predictive of resistance to an FDA-approved drug in this indication", "<span><b>Standard of care</b> biomarker predictive of <b>resistance</b> to an <b>FDA-approved</b> drug <b>in this indication</b></span>", "#EE3424"),
    LEVEL_R2 ("R2", "Compelling clinical evidence supports the biomarker as being predictive of resistance to a drug", "<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of <b>resistance</b> to a drug</span>", "#F79A92"),

    // Levels for prognostic implications
    LEVEL_Px1("Px1", "FDA and/or professional guideline-recognized biomarker prognostic in this indication based on well-powered studie(s)", "<span><b>FDA and/or professional guideline-recognized</b> biomarker prognostic in this indication based on <b>well-powered studie(s)</b></span>", "#33A02C"),
    LEVEL_Px2("Px2", "FDA and/or professional guideline-recognized biomarker prognostic in this indication based on a single or multiple small studies", "<span><b>FDA and/or professional guideline-recognized</b> biomarker prognostic in this indication based on <b>a single or multiple small studies</b></span>", "#1F78B4"),
    LEVEL_Px3("Px3", "Biomarker is prognostic in this indication based on clinical evidence in well-powered studies", "<span>Biomarker is prognostic in this indication based on <b>clinical evidence</b> in <b>well-powered studies</b></span>", "#984EA3"),

    // Levels for diagnostic implications
    LEVEL_Dx1("Dx1", "FDA and/or professional guideline-recognized biomarker required for diagnosis in this indication", "<span><b>FDA and/or professional guideline-recognized</b> biomarker required for diagnosis in this indication</span>", "#33A02C"),
    LEVEL_Dx2("Dx2", "FDA and/or professional guideline-recognized biomarker that supports diagnosis in this indication", "<span><b>FDA and/or professional guideline-recognized</b> biomarker that supports diagnosis in this indication</span>", "#1F78B4"),
    LEVEL_Dx3("Dx3", "Biomarker that may assist disease diagnosis in this indication based on clinical evidence", "<span>Biomarker that <b>may assist disease diagnosis</b> in this indication based on <b>clinical evidence</b></span>", "#984EA3"),

    // FDA Levels for tumor profiling NGS tests
    LEVEL_Fda1("Fda1", "Companion Diagnostics", "Companion Diagnostics", ""),
    LEVEL_Fda2("Fda2", "Cancer Mutations with Evidence of Clinical Significance", "Cancer Mutations with Evidence of Clinical Significance", ""),
    LEVEL_Fda3("Fda3", "Cancer Mutations with Potential of Clinical Significance", "Cancer Mutations with Potential of Clinical Significance", ""),

    NO("NO", "No level", "No Level", ""),
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
        if (level == null) return null;
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
