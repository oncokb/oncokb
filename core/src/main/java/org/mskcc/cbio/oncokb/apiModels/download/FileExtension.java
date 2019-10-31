package org.mskcc.cbio.oncokb.apiModels.download;

/**
 * Created by Hongxin Zhang on 10/21/19.
 */
public enum FileExtension {
    JSON(".json"), TEXT(".txt"), MARK_DOWN(".md"), ZIP(".zip");
    String extension;

    FileExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
