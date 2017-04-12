package org.mskcc.cbio.oncokb.apiModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.NccnGuideline;
import org.mskcc.cbio.oncokb.model.Treatment;
import org.mskcc.oncotree.model.TumorType;

import java.util.Date;
import java.util.List;

/**
 * Created by Hongxin on 4/12/17.
 */
public class TreatmentInfo extends Treatment {
    private LevelOfEvidence level;
    @JsonProperty("abstracts")
    private List<Abstract> abstractList;
    private List<NccnGuideline> guidelines;
    @JsonProperty("annotatedVariants")
    private List<Alteration> alterations;
    @JsonProperty("annotatedTumorTypes")
    private List<TumorType> tumorTypes;
    private String description;
    private Date lastUpdate;

    public LevelOfEvidence getLevel() {
        return level;
    }

    public void setLevel(LevelOfEvidence level) {
        this.level = level;
    }

    public List<Abstract> getAbstractList() {
        return abstractList;
    }

    public void setAbstractList(List<Abstract> abstractList) {
        this.abstractList = abstractList;
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
