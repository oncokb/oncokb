package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.HashSet;
import java.util.Set;

public class ActionableGeneEvidence {
    private Gene gene;
    private Set<Alteration> alterations;
    private LevelOfEvidence levelOfEvidence;
    private LevelOfEvidence fdaLevel;
    private Set<TumorType> cancerTypes = new HashSet<>();
    private Set<TumorType> excludedCancerTypes = new HashSet<>();
    private Set<Treatment> treatments = new HashSet<>(0);

    private Citations citations = new Citations();

    public ActionableGeneEvidence() {
    }

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

    public LevelOfEvidence getFdaLevel() {
        return fdaLevel;
    }

    public void setFdaLevel(LevelOfEvidence fdaLevel) {
        this.fdaLevel = fdaLevel;
    }


    public Set<TumorType> getCancerTypes() {
        return cancerTypes;
    }

    public void setCancerTypes(Set<TumorType> cancerTypes) {
        this.cancerTypes = cancerTypes;
    }

    public Set<TumorType> getExcludedCancerTypes() {
        return excludedCancerTypes;
    }

    public void setExcludedCancerTypes(Set<TumorType> excludedCancerTypes) {
        this.excludedCancerTypes = excludedCancerTypes;
    }

    public Set<Treatment> getTreatments() {
        return treatments;
    }

    public void setTreatments(Set<Treatment> treatments) {
        this.treatments = treatments;
    }

    public Citations getCitations() {
        return citations;
    }

    public void setCitations(Citations citations) {
        this.citations = citations;
    }
}
