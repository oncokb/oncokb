package org.mskcc.cbio.oncokb.model.genomeNexusPreAnnotations;

import org.mskcc.cbio.oncokb.model.ReferenceGenome;

public class GenomeNexusAnnotatedVariantInfo {
    private String originalVariantQuery;
    private String hgvsg;
    private String genomicLocation;
    private ReferenceGenome referenceGenome;
    private String hugoSymbol;
    private Integer entrezGeneId;
    private String hgvspShort;
    private Integer proteinStart;
    private Integer proteinEnd;
    private String consequenceTerms;

    public String getGenomicLocation() {
        return this.genomicLocation;
    }

    public void setGenomicLocation(String genomicLocation) {
        this.genomicLocation = genomicLocation;
    }

    public String getOriginalVariantQuery() {
        return this.originalVariantQuery;
    }

    public void setOriginalVariantQuery(String originalVariantQuery) {
        this.originalVariantQuery = originalVariantQuery;
    }

    public String getHgvsg() {
        return this.hgvsg;
    }

    public void setHgvsg(String hgvsg) {
        this.hgvsg = hgvsg;
    }

    public ReferenceGenome getReferenceGenome() {
        return this.referenceGenome;
    }

    public void setReferenceGenome(ReferenceGenome referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public String getHugoSymbol() {
        return this.hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public Integer getEntrezGeneId() {
        return this.entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHgvspShort() {
        return this.hgvspShort;
    }

    public void setHgvspShort(String hgvspShort) {
        this.hgvspShort = hgvspShort;
    }

    public Integer getProteinStart() {
        return this.proteinStart;
    }

    public void setProteinStart(Integer proteinStart) {
        this.proteinStart = proteinStart;
    }

    public Integer getProteinEnd() {
        return this.proteinEnd;
    }

    public void setProteinEnd(Integer proteinEnd) {
        this.proteinEnd = proteinEnd;
    }

    public String getConsequenceTerms() {
        return this.consequenceTerms;
    }

    public void setConsequenceTerms(String consequenceTerms) {
        this.consequenceTerms = consequenceTerms;
    }

}
