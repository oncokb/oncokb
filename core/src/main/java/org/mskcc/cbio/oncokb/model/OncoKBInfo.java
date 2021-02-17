package org.mskcc.cbio.oncokb.model;

import org.mskcc.cbio.oncokb.apiModels.InfoLevel;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;

import javax.sound.sampled.Line;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.mskcc.cbio.oncokb.Constants.IS_PUBLIC_INSTANCE;
import static org.mskcc.cbio.oncokb.Constants.PUBLIC_API_VERSION;

/**
 * Created by Hongxin Zhang on 7/13/18.
 */
public class OncoKBInfo {
    String oncoTreeVersion;
    String ncitVersion;
    List<InfoLevel> levels;
    Version dataVersion;
    String apiVersion;
    Boolean publicInstance;

    public OncoKBInfo(Info info) {
        this.oncoTreeVersion = info.getOncoTreeVersion();
        this.ncitVersion = info.getNcitVersion();
        this.levels = LevelUtils.getInfoLevels();

        Version dataVersion = new Version();
        dataVersion.setVersion(info.getDataVersion());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dataVersion.setDate(dateFormat.format(info.getDataVersionDate()));

        this.dataVersion = dataVersion;
        this.apiVersion = PUBLIC_API_VERSION;

        String isPublicInstance = PropertiesUtils.getProperties(IS_PUBLIC_INSTANCE);

        if (isPublicInstance != null && Boolean.valueOf(isPublicInstance)) {
            this.publicInstance = true;
        } else {
            this.publicInstance = false;
        }
    }

    public String getOncoTreeVersion() {
        return oncoTreeVersion;
    }

    public String getNcitVersion() {
        return ncitVersion;
    }

    public List<InfoLevel> getLevels() {
        return levels;
    }

    public Version getDataVersion() {
        return dataVersion;
    }

    public Boolean getPublicInstance() {
        return publicInstance;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}
