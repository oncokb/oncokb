package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.Objects;


public class CancerTypeMatch extends LevelsOfEvidence {
    private TumorType cancer;

    public TumorType getCancer() {
        return cancer;
    }

    public void setCancer(TumorType cancer) {
        this.cancer = cancer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CancerTypeMatch)) return false;
        CancerTypeMatch cancerTypeMatch = (CancerTypeMatch) o;
        return getWeight() == cancerTypeMatch.getWeight() &&
            Objects.equals(getGene(), cancerTypeMatch.getGene()) &&
            Objects.equals(getAlterations(), cancerTypeMatch.getAlterations()) &&
            getLevelOfEvidence() == cancerTypeMatch.getLevelOfEvidence() &&
            Objects.equals(getCancer(), cancerTypeMatch.getCancer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGene(), getAlterations(), getLevelOfEvidence(), getCancer(), getWeight());
    }
}
