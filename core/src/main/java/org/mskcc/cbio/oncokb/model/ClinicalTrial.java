

package org.mskcc.cbio.oncokb.model;

import java.util.Objects;


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
    private String location;
    private Boolean isMskccTrial;

    public ClinicalTrial() {
    }

    public ClinicalTrial(String nctId) {
        this.nctId = nctId;
    }

    public ClinicalTrial(String nctId, String title, String purpose, String recuitingStatus, String eligibilityCriteria, String phase, String location, Boolean isMskccTrial) {
        this.nctId = nctId;
        this.title = title;
        this.purpose = purpose;
        this.recuitingStatus = recuitingStatus;
        this.eligibilityCriteria = eligibilityCriteria;
        this.phase = phase;
        this.isMskccTrial = isMskccTrial;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public Boolean isIsMskccTrial() {
        return isMskccTrial;
    }

    public void setIsMskccTrial(Boolean isMskccTrial) {
        this.isMskccTrial = isMskccTrial;
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
