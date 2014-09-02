

package org.mskcc.cbio.oncokb.model;

import java.util.Objects;
import java.util.Set;


/**
 *
 * @author jgao
 */
public class ClinicalTrial implements java.io.Serializable {
    private Integer trialId;
    private String nctId;
    private String title;
    private String purpose;
    private String recuitingStatus;
    private String eligibilityCriteria;
    private String phase;
    private String diseaseCondition;
    private Set<TumorType> tumorTypes;
    private Set<Drug> drugs;
    

    public ClinicalTrial() {
    }

    public Integer getTrialId() {
        return trialId;
    }

    public void setTrialId(Integer trialId) {
        this.trialId = trialId;
    }

    public String getNctId() {
        return nctId;
    }

    public void setNctId(String nctId) {
        this.nctId = nctId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getRecuitingStatus() {
        return recuitingStatus;
    }

    public void setRecuitingStatus(String recuitingStatus) {
        this.recuitingStatus = recuitingStatus;
    }

    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getDiseaseCondition() {
        return diseaseCondition;
    }

    public void setDiseaseCondition(String diseaseCondition) {
        this.diseaseCondition = diseaseCondition;
    }

    public Set<TumorType> getTumorTypes() {
        return tumorTypes;
    }

    public void setTumorTypes(Set<TumorType> tumorTypes) {
        this.tumorTypes = tumorTypes;
    }

    public Set<Drug> getDrugs() {
        return drugs;
    }

    public void setDrugs(Set<Drug> drugs) {
        this.drugs = drugs;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.nctId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClinicalTrial other = (ClinicalTrial) obj;
        if (!Objects.equals(this.nctId, other.nctId)) {
            return false;
        }
        return true;
    }
    
    
}
