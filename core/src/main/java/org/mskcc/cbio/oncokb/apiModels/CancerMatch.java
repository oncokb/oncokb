package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class CancerMatch extends LevelsOfEvidence {
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
