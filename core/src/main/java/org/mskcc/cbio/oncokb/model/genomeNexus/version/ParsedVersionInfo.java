package org.mskcc.cbio.oncokb.model.genomeNexus.version;

import java.io.Serializable;

public class ParsedVersionInfo implements Serializable {
    private final String genomeNexusVersion;
    private final String genomeNexusVepVersion;
    private final String vepVersion;

    public ParsedVersionInfo(String genomeNexusVersion, String genomeNexusVepVersion, String vepVersion) {
        this.genomeNexusVersion = genomeNexusVersion;
        this.genomeNexusVepVersion = genomeNexusVepVersion;
        this.vepVersion = vepVersion;
    }

    public String getGenomeNexusVersion() {
        return genomeNexusVersion;
    }

    public String getGenomeNexusVepVersion() {
        return genomeNexusVepVersion;
    }

    public String getVepVersion() {
        return vepVersion;
    }
}
