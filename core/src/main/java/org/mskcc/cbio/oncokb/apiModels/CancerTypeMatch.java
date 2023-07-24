package org.mskcc.cbio.oncokb.apiModels;

import kotlin.Pair;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.TumorType;
import java.util.*;

import java.util.Objects;


public class CancerTypeMatch extends LevelsOfEvidenceMatch {
    private TumorType cancerType;

    private Map<LevelOfEvidence, Set<Alteration>> alterationsByLevel;

    public TumorType getCancerType() {
        return cancerType;
    }

    public void setCancerType(TumorType cancerType) {
        this.cancerType = cancerType;
    }

    public Map<LevelOfEvidence, Set<Alteration>> getAlterationsByLevel() {
        return alterationsByLevel;
    }

    public void setAlterationsByLevel(Map<LevelOfEvidence, Set<Alteration>> alterationsByLevel) {
        this.alterationsByLevel = alterationsByLevel;
    }

    public LevelOfEvidence findHighestLevel(List<LevelOfEvidence> txLevels) {
        // Map<LevelOfEvidence, Set<Alteration>> alterationsByLevel = cancerTypeMatch.getAlterationsByLevel();
        if (alterationsByLevel != null) {
            LevelOfEvidence highestLevel = null;
            for (int i = txLevels.size() - 1; i >= 0; --i){
                if (alterationsByLevel.containsKey(txLevels.get(i))){
                    highestLevel = txLevels.get(i);
                }
            }
            return highestLevel;
        }
        return null;
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
