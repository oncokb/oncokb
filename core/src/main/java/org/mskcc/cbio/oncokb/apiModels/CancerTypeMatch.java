package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.Objects;


public class CancerTypeMatch extends LevelsOfEvidenceMatch {
    private TumorType cancerType;

    public TumorType getCancerType() {
        return cancerType;
    }

    public void setCancer(TumorType cancerType) {
        this.cancerType = cancerType;
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
            Objects.equals(getCancerType(), cancerTypeMatch.getCancerType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGene(), getAlterations(), getLevelOfEvidence(), getCancerType(), getWeight());
    }
}
