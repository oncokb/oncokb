package org.mskcc.cbio.oncokb.apiModels.download;

/**
 * Created by Hongxin Zhang on 10/21/19.
 */
public enum FileName {
    ALL_ANNOTATED_VARIANTS("allAnnotatedVariants"),
    ALL_ACTIONABLE_GENES("allActionableGenes"),
    ALL_CURATED_GENES("allCuratedGenes"),
    CANCER_GENE_LIST("cancerGeneList"),
    README("README");

    private String name;

    FileName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
