package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public class CurationSuggestion {
    private String gene;
    private String mutations;

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getMutations() {
        return mutations;
    }

    public void setMutations(String mutations) {
        this.mutations = mutations;
    }
}
