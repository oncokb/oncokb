

package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * @author jgao, Hongxin Zhang
 */
@NamedQueries({
    @NamedQuery(
        name = "findClinicalTrialByNctId",
        query = "select c from ClinicalTrial c where c.nctId=?"
    )
})

@Entity
@Table(name = "clinical_trial")
public class ClinicalTrial implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(name = "nct_id", nullable = false)
    private String nctId;

    @Column(name = "cdr_id")
    private String cdrId;

    @Column(length = 2000)
    private String title;

    @Column(length = 65535)
    private String purpose;

    @Column(name = "recruiting_status")
    private String recruitingStatus;

    @Column(name = "eligibility_criteria", length = 65535)
    private String eligibilityCriteria;
    private String phase;

    @Column(name = "disease_condition")
    private String diseaseCondition;

    @Column(name = "last_changed_date")
    private String lastChangedDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clinical_trial_country", joinColumns = @JoinColumn(name = "trial_id", nullable = false))
    @Column(name = "country")
    private Set<String> countries = new HashSet<String>(0);

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "clinical_trial_drug", joinColumns = {
        @JoinColumn(name = "trial_id", nullable = false, updatable = false)},
        inverseJoinColumns = {@JoinColumn(name = "drug_id",
            nullable = false, updatable = false)})
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
