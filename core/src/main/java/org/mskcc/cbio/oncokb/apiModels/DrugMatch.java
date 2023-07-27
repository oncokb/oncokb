package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2019-04-23.
 */
public class DrugMatch extends LevelsOfEvidenceMatch{

     private Drug drug;
     private Set<TumorType> tumorTypes = new HashSet<>();

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public Set<TumorType> getTumorTypes() {
        return tumorTypes;
    }

    public void setTumorTypes(Set<TumorType> tumorTypes) {
        this.tumorTypes = tumorTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrugMatch)) return false;
        DrugMatch drugMatch = (DrugMatch) o;
        return getWeight() == drugMatch.getWeight() &&
            Objects.equals(getGene(), drugMatch.getGene()) &&
            Objects.equals(getAlterations(), drugMatch.getAlterations()) &&
            getLevelOfEvidence() == drugMatch.getLevelOfEvidence() &&
            Objects.equals(getDrug(), drugMatch.getDrug());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGene(), getAlterations(), getLevelOfEvidence(), getDrug(), getWeight());
    }
}
