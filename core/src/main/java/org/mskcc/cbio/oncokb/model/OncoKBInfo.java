package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin Zhang on 7/13/18.
 */
public class OncoKBInfo {
    String oncoTreeVersion;
    Version dataVersion;

    public String getOncoTreeVersion() {
        return oncoTreeVersion;
    }

    public void setOncoTreeVersion(String oncoTreeVersion) {
        this.oncoTreeVersion = oncoTreeVersion;
    }

    public Version getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(Version dataVersion) {
        this.dataVersion = dataVersion;
    }
}
