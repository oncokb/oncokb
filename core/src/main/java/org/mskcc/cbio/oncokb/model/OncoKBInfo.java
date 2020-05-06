package org.mskcc.cbio.oncokb.model;

import org.mskcc.cbio.oncokb.apiModels.InfoLevel;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.List;

import static org.mskcc.cbio.oncokb.Constants.PUBLIC_API_VERSION;

/**
 * Created by Hongxin Zhang on 7/13/18.
 */
public class OncoKBInfo {
    String oncoTreeVersion;
    String ncitVersion = "19.03d";
    List<InfoLevel> levels = new ArrayList<>();
    Version dataVersion;
    String apiVersion = PUBLIC_API_VERSION;
    Boolean publicInstance = false;

    public String getOncoTreeVersion() {
        return oncoTreeVersion;
    }

    public void setOncoTreeVersion(String oncoTreeVersion) {
        this.oncoTreeVersion = oncoTreeVersion;
    }

    public String getNcitVersion() {
        return ncitVersion;
    }

    public List<InfoLevel> getLevels() {
        return levels;
    }

    public void setLevels(List<InfoLevel> levels) {
        this.levels = levels;
    }

    public Version getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(Version dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Boolean getPublicInstance() {
        return publicInstance;
    }

    public void setPublicInstance(Boolean publicInstance) {
        this.publicInstance = publicInstance;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}
