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
