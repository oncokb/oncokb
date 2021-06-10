package org.mskcc.cbio.oncokb.model;

import java.io.Serializable;

/**
 * Created by Hongxin Zhang on 7/13/18.
 */
public class Version implements Serializable {
    String version;
    String date;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
