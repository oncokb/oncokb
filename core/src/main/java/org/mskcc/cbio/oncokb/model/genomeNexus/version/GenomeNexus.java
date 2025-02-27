package org.mskcc.cbio.oncokb.model.genomeNexus.version;

public class GenomeNexus {
    private VersionInfo server;
    private VersionInfo database;

    public VersionInfo getServer() {
        return server;
    }

    public void setServer(VersionInfo server) {
        this.server = server;
    }

    public VersionInfo getDatabase() {
        return database;
    }
    
    public void setDatabase(VersionInfo database) {
        this.database = database;
    }
}
