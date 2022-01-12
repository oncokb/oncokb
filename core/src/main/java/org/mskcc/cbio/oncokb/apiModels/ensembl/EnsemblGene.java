package org.mskcc.cbio.oncokb.apiModels.ensembl;

import java.io.Serializable;

public class EnsemblGene implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ensemblGeneId = "";

    private Boolean canonical = false;

    private String chromosome = "";

    private String referenceGenome = "";

    private Integer strand = null;

    private Integer start = null;

    private Integer end = null;

    public EnsemblGene(org.oncokb.oncokb_transcript.client.EnsemblGene ensemblGene) {
        this.ensemblGeneId=ensemblGene.getEnsemblGeneId();
        this.canonical = ensemblGene.getCanonical();
        this.chromosome = ensemblGene.getChromosome();
        this.referenceGenome = ensemblGene.getReferenceGenome();
        this.strand = ensemblGene.getStrand();
        this.start = ensemblGene.getStart();
        this.end = ensemblGene.getEnd();
    }

    public String getEnsemblGeneId() {
        return ensemblGeneId;
    }

    public void setEnsemblGeneId(String ensemblGeneId) {
        this.ensemblGeneId = ensemblGeneId;
    }

    public Boolean getCanonical() {
        return canonical;
    }

    public void setCanonical(Boolean canonical) {
        this.canonical = canonical;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getReferenceGenome() {
        return referenceGenome;
    }

    public void setReferenceGenome(String referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public Integer getStrand() {
        return strand;
    }

    public void setStrand(Integer strand) {
        this.strand = strand;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
