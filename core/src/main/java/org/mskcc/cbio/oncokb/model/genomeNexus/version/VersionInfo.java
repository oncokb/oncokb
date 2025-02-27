package org.mskcc.cbio.oncokb.model.genomeNexus.version;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VersionInfo {
    private String version;
    @JsonProperty("static")
    private boolean isStatic;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isStatic() {
        return isStatic;
    }
    
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
}
