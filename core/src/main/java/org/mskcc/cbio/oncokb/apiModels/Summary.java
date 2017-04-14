package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.util.MainUtils;

import java.util.Date;

/**
 * Created by Hongxin on 4/12/17.
 */
public class Summary {
    private String summary;
    private Date lastUpdate;

    public Summary() {
    }

    public Summary(String summary) {
        this.summary = summary;
    }

    public Summary(String summary, Date lastUpdate) {
        this.summary = summary;
        this.lastUpdate = lastUpdate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
