package org.mskcc.cbio.oncokb.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mskcc.cbio.oncokb.util.DrugUtils;
import org.mskcc.cbio.oncokb.util.TreatmentUtils;

import javax.persistence.*;
import java.util.*;


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
    @JsonProperty(value = "drugs")
    private Set<TreatmentDrug> treatmentDrugs = new HashSet<>(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "treatment_approved_indications",
        joinColumns = @JoinColumn(name = "treatment_id", nullable = false))
    @Column(name = "approved_indications", length = 500)
    private Set<String> approvedIndications = new HashSet<String>(0);

    private Integer priority;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "evidence_id")
    @JsonIgnore
    private Evidence evidence;

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
    @JsonIgnore
    public List<Drug> getDrugs() {
        if (this.treatmentDrugs == null) {
            return null;
        } else {
            List<TreatmentDrug> treatmentDrugs = new ArrayList<>(this.getTreatmentDrugs());
            Collections.sort(treatmentDrugs, new Comparator<TreatmentDrug>() {
                public int compare(TreatmentDrug td1, TreatmentDrug td2) {
                    return td1.getPriority() - td2.getPriority();
                }
            });
            List<Drug> drugs = new ArrayList<>();
            for (TreatmentDrug treatmentDrug : treatmentDrugs) {
                drugs.add(treatmentDrug.getDrug());
            }
            return drugs;
        }
    }

    public void setDrugs(List<Drug> drugs) {
        if (drugs == null) {
            this.treatmentDrugs = null;
        } else {
            Set<TreatmentDrug> treatmentDrugs = new HashSet<>();
            for (int i = 0; i < drugs.size(); i++) {
                TreatmentDrug treatmentDrug = new TreatmentDrug();
                treatmentDrug.setTreatment(this);
                treatmentDrug.setPriority(i + 1);
                treatmentDrug.setDrug(drugs.get(i));
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

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @JsonIgnore
    public String getName() {
        return TreatmentUtils.getTreatmentName(Collections.singleton(this));
    }
}


