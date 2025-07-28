package org.mskcc.cbio.oncokb.model.genomeNexus.version;

public class VEP {
    private VersionInfo server;
    private VersionInfo cache;
    private String comment;

    public VersionInfo getServer() {
        return server;
    }

    public void setServer(VersionInfo server) {
        this.server = server;
    }

    public VersionInfo getCache() {
        return cache;
    }

    public void setCache(VersionInfo cache) {
        this.cache = cache;
    }

    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
}
