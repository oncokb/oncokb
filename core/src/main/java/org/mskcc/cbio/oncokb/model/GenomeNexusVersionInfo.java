package org.mskcc.cbio.oncokb.model;

public class GenomeNexusVersionInfo {
    private final Version grch37;
    private final Version grch38;

    public GenomeNexusVersionInfo(Version grch37, Version grch38) {
        this.grch37 = grch37;
        this.grch38 = grch38;
    }

    public Version getGrch37() {
        return grch37;
    }

    public Version getGrch38() {
        return grch38;
    }

    static public class Version {
        private final String genomeNexusVersion;
        private final String genomeNexusVepVersion;
        private final String vepVersion;

        public Version(String genomeNexusVersion, String genomeNexusVepVersion, String vepVersion) {
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
}
