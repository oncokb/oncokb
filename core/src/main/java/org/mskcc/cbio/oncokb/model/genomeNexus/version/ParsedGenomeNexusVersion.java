package org.mskcc.cbio.oncokb.model.genomeNexus.version;

public class ParsedGenomeNexusVersion {
    private final ParsedVersionInfo grch37;
    private final ParsedVersionInfo grch38;

    public ParsedGenomeNexusVersion(ParsedVersionInfo grch37, ParsedVersionInfo grch38) {
        this.grch37 = grch37;
        this.grch38 = grch38;
    }

    public ParsedVersionInfo getGrch37() {
        return grch37;
    }

    public ParsedVersionInfo getGrch38() {
        return grch38;
    }
}
