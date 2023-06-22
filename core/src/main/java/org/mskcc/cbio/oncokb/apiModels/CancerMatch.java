package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class CancerMatch {
    private Gene gene;
    private Set<Alteration> alterations = new HashSet<>();
    private LevelOfEvidence levelOfEvidence;
    private TumorType cancer;
    private Double weight = 0.0;

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public Set<Alteration> getAlterations() {
        return alterations;
    }

    public void setAlterations(Set<Alteration> alterations) {
        this.alterations = alterations;
    }

    public LevelOfEvidence getLevelOfEvidence() {
        return levelOfEvidence;
    }

    public void setLevelOfEvidence(LevelOfEvidence levelOfEvidence) {
        this.levelOfEvidence = levelOfEvidence;
    }

    public TumorType getCancer() {
        return cancer;
    }

    public void setCancer(TumorType cancer) {
        this.cancer = cancer;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CancerMatch)) return false;
        CancerMatch cancerMatch = (CancerMatch) o;
        return getWeight() == cancerMatch.getWeight() &&
            Objects.equals(getGene(), cancerMatch.getGene()) &&
            Objects.equals(getAlterations(), cancerMatch.getAlterations()) &&
            getLevelOfEvidence() == cancerMatch.getLevelOfEvidence() &&
            Objects.equals(getCancer(), cancerMatch.getCancer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGene(), getAlterations(), getLevelOfEvidence(), getCancer(), getWeight());
    }
}
