package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.LevelOfEvidence;

/**
 * Created by Hongxin Zhang on 2019-04-24.
 */
public class InfoLevel {
    private LevelOfEvidence levelOfEvidence;
    private String description;
    private String htmlDescription;
    private String colorHex;

    public InfoLevel(LevelOfEvidence levelOfEvidence) {
        this.levelOfEvidence = levelOfEvidence;
        this.description = levelOfEvidence.getDescription();
        this.htmlDescription = levelOfEvidence.getHtmlDescription();
        this.colorHex = levelOfEvidence.getColorHex();
    }

    public LevelOfEvidence getLevelOfEvidence() {
        return levelOfEvidence;
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
}
