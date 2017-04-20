package org.mskcc.cbio.oncokb.apiModels;

import java.util.Date;

/**
 * Created by Hongxin on 4/12/17.
 */
public class Summary {
    private String summary;
    private References references;
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

    public Summary(String summary, References references, Date lastUpdate) {
        this.summary = summary;
        this.references = references;
        this.lastUpdate = lastUpdate;
    }

    public References getReferences() {
        return references;
    }

    public void setReferences(References references) {
        this.references = references;
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
