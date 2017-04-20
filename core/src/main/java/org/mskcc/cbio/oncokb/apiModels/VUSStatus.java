package org.mskcc.cbio.oncokb.apiModels;

import java.util.Date;

/**
 * Created by Hongxin on 4/12/17.
 */
public class VUSStatus {
    private Boolean status;
    private Date lastUpdate;

    public VUSStatus() {
    }

    public VUSStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
