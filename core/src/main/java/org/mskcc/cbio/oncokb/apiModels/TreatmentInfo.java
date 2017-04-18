package org.mskcc.cbio.oncokb.apiModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.oncotree.model.TumorType;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Hongxin on 4/12/17.
 */
public class TreatmentInfo extends Treatment {
    private LevelOfEvidence level;
    private List<NccnGuideline> guidelines;
    private References references;
    @JsonProperty("annotatedVariants")
    private List<Alteration> alterations;
    @JsonProperty("annotatedTumorTypes")
    private List<TumorType> tumorTypes;
    private String description;
    private Date lastUpdate;

    public TreatmentInfo(Set<Drug> drugs, Set<String> approvedIndications, LevelOfEvidence level, References references, List<NccnGuideline> guidelines, List<Alteration> alterations, List<TumorType> tumorTypes, String description, Date lastUpdate) {
        this.setDrugs(drugs);
        this.setApprovedIndications(approvedIndications);
        this.level = level;
        this.references = references;
        this.guidelines = guidelines;
        this.alterations = alterations;
        this.tumorTypes = tumorTypes;
        this.description = description;
        this.lastUpdate = lastUpdate;
    }

    public LevelOfEvidence getLevel() {
        return level;
    }

    public void setLevel(LevelOfEvidence level) {
        this.level = level;
    }

    public References getReferences() {
        return references;
    }

    public void setReferences(References references) {
        this.references = references;
    }

    public List<NccnGuideline> getGuidelines() {
        return guidelines;
    }

    public void setGuidelines(List<NccnGuideline> guidelines) {
        this.guidelines = guidelines;
    }

    public List<Alteration> getAlterations() {
        return alterations;
    }

    public void setAlterations(List<Alteration> alterations) {
        this.alterations = alterations;
    }

    public List<TumorType> getTumorTypes() {
        return tumorTypes;
    }

    public void setTumorTypes(List<TumorType> tumorTypes) {
        this.tumorTypes = tumorTypes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
