package org.mskcc.cbio.oncokb.model.genomeNexus.version;

import java.util.List;

public class GenomeNexusVersion {
    private GenomeNexus genomeNexus;
    private VEP vep;
    private List<AnnotationSourcesInfo> annotationSourcesInfo;

    public GenomeNexus getGenomeNexus() {
        return genomeNexus;
    }

    public void setGenomeNexus(GenomeNexus genomeNexus) {
        this.genomeNexus = genomeNexus;
    }

    public VEP getVep() {
        return vep;
    }

    public void setVep(VEP vep) {
        this.vep = vep;
    }

    public List<AnnotationSourcesInfo> getAnnotationSourcesInfo() {
        return annotationSourcesInfo;
    }

    public void setAnnotationSourcesInfo(List<AnnotationSourcesInfo> annotationSourcesInfo) {
        this.annotationSourcesInfo = annotationSourcesInfo;
    }

    public ParsedVersionInfo toParsedVersionInfo() {
        return new ParsedVersionInfo(genomeNexus.getServer().getVersion(), vep.getServer().getVersion(), vep.getCache().getVersion());
    }
}
