package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * @author jgao, Hongxin Zhang
 */

@Entity
@Table(name = "treatment")
public class Treatment implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @JsonIgnore
    @Column(length = 40)
    private String uuid;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "treatmentDrugId.treatment", cascade = CascadeType.ALL)
    private Set<TreatmentDrug> treatmentDrugs = new HashSet<>(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "treatment_approved_indications",
        joinColumns = @JoinColumn(name = "treatment_id", nullable = false))
    @Column(name = "approved_indications")
    private Set<String> approvedIndications = new HashSet<String>(0);

    public Treatment() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<TreatmentDrug> getTreatmentDrugs() {
        return treatmentDrugs;
    }

    public void setTreatmentDrugs(Set<TreatmentDrug> treatmentDrugs) {
        this.treatmentDrugs = treatmentDrugs;
    }

    @Transient
    public Set<Drug> getDrugs() {
        if (this.treatmentDrugs == null) {
            return null;
        } else {
            Set<Drug> drugs = new HashSet<>();
            for (TreatmentDrug treatmentDrug : treatmentDrugs) {
                drugs.add(treatmentDrug.getDrug());
            }
            return drugs;
        }
    }

    public void setDrugs(Set<Drug> drugs) {
        if (drugs == null) {
            this.treatmentDrugs = null;
        } else {
            Set<TreatmentDrug> treatmentDrugs = new HashSet<>();
            for (Drug drug : drugs) {
                TreatmentDrug treatmentDrug = new TreatmentDrug();
                treatmentDrug.setTreatment(this);
                treatmentDrug.setDrug(drug);
                treatmentDrugs.add(treatmentDrug);
            }
            this.treatmentDrugs = treatmentDrugs;
        }
    }

    public Set<String> getApprovedIndications() {
        return approvedIndications;
    }

    public void setApprovedIndications(Set<String> approvedIndications) {
        this.approvedIndications = approvedIndications;
    }
}


