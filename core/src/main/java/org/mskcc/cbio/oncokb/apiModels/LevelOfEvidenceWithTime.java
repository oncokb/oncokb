package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.LevelOfEvidence;

import java.util.Date;

/**
 * Created by Hongxin on 4/12/17.
 */
public class LevelOfEvidenceWithTime {
    private LevelOfEvidence level;
    private Date lastUpdate;

    public LevelOfEvidenceWithTime(LevelOfEvidence level) {
        this.level = level;
    }

    public LevelOfEvidenceWithTime(LevelOfEvidence level, Date lastUpdate) {
        this.level = level;
        this.lastUpdate = lastUpdate;
    }

    public LevelOfEvidence getLevel() {
        return level;
    }

    public void setLevel(LevelOfEvidence level) {
        this.level = level;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
