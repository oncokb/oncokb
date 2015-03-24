package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public class PubMed {
    private String gene;
    private String links;
    private String mutationLinks;

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public String getMutationLinks() {
        return mutationLinks;
    }

    public void setMutationLinks(String mutationLinks) {
        this.mutationLinks = mutationLinks;
    }
}
