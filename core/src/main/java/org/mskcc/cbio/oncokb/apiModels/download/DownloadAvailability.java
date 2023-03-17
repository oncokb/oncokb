package org.mskcc.cbio.oncokb.apiModels.download;

/**
 * Created by Hongxin Zhang on 10/21/19.
 */

public class DownloadAvailability {
    private String version;
    private Boolean hasReadme = false;
    private Boolean hasAllActionableGenes = false;
    private Boolean hasAllAnnotatedVariants = false;
    private Boolean hasAllCuratedGenes = false;
    private Boolean hasCancerGeneList = false;
    private Boolean hasSqlDump = false;

    public DownloadAvailability(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getHasReadme() {
        return hasReadme;
    }

    public void setHasReadme(Boolean hasReadme) {
        this.hasReadme = hasReadme;
    }

    public Boolean getHasAllActionableGenes() {
        return hasAllActionableGenes;
    }

    public void setHasAllActionableGenes(Boolean hasAllActionableGenes) {
        this.hasAllActionableGenes = hasAllActionableGenes;
    }

    public Boolean getHasAllAnnotatedVariants() {
        return hasAllAnnotatedVariants;
    }

    public void setHasAllAnnotatedVariants(Boolean hasAllAnnotatedVariants) {
        this.hasAllAnnotatedVariants = hasAllAnnotatedVariants;
    }

    public Boolean getHasAllCuratedGenes() {
        return hasAllCuratedGenes;
    }

    public void setHasAllCuratedGenes(Boolean hasAllCuratedGenes) {
        this.hasAllCuratedGenes = hasAllCuratedGenes;
    }

    public Boolean getHasCancerGeneList() {
        return hasCancerGeneList;
    }

    public void setHasCancerGeneList(Boolean hasCancerGeneList) {
        this.hasCancerGeneList = hasCancerGeneList;
    }

    public Boolean getHasSqlDump() {
        return hasSqlDump;
    }

    public void setHasSqlDump(Boolean hasSqlDump) {
        this.hasSqlDump = hasSqlDump;
    }
}
