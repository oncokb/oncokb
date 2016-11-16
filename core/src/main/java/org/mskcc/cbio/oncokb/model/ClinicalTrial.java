

package org.mskcc.cbio.oncokb.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * @author jgao
 */
public class ClinicalTrial implements java.io.Serializable {
    private Integer id;
    private String nctId;
    private String cdrId;
    private String title;
    private String purpose;
    private String recruitingStatus;
    private String eligibilityCriteria;
    private String phase;
    private String diseaseCondition;
    private String lastChangedDate;
    private Set<String> countries = new HashSet<String>(0);
    private Set<Drug> drugs = new HashSet<Drug>(0);

    public ClinicalTrial() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNctId() {
        return nctId;
    }

    public void setNctId(String nctId) {
        this.nctId = nctId;
    }

    public String getCdrId() {
        return cdrId;
    }

    public void setCdrId(String cdrId) {
        this.cdrId = cdrId;
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

    public String getRecruitingStatus() {
        return recruitingStatus;
    }

    public void setRecruitingStatus(String recruitingStatus) {
        this.recruitingStatus = recruitingStatus;
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

    public String getLastChangedDate() {
        return lastChangedDate;
    }

    public void setLastChangedDate(String lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    public Set<String> getCountries() {
        return countries;
    }

    public void setCountries(Set<String> countries) {
        this.countries = countries;
    }

    public boolean isInUSA() {
        return countries.contains("United States");
    }

    public Set<Drug> getDrugs() {
        return drugs;
    }

    public void setDrugs(Set<Drug> drugs) {
        this.drugs = drugs;
    }

    public boolean isOpen() {
        return recruitingStatus != null &&
            !recruitingStatus.equalsIgnoreCase("Terminated") &&
            !recruitingStatus.equalsIgnoreCase("Suspended") &&
            !recruitingStatus.equalsIgnoreCase("Completed") &&
            !recruitingStatus.equalsIgnoreCase("Closed") &&
            !recruitingStatus.equalsIgnoreCase("Active, not recruiting") &&
            !recruitingStatus.equalsIgnoreCase("Withdrawn");
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
