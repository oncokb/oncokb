package org.mskcc.cbio.oncokb.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Hongxin Zhang on 10/2/17.
 */
@Entity
@Table(name = "treatment_drug")
@AssociationOverrides({
    @AssociationOverride(name = "treatmentDrugId.treatment",
        joinColumns = @JoinColumn(name = "treatment_id")),
    @AssociationOverride(name = "treatmentDrugId.drug",
        joinColumns = @JoinColumn(name = "drug_id"))
})
public class TreatmentDrug implements Serializable {

    @EmbeddedId
    private TreatmentDrugId treatmentDrugId = new TreatmentDrugId();

    private Integer priority;

    public TreatmentDrugId getTreatmentDrugId() {
        return treatmentDrugId;
    }

    public void setTreatmentDrugId(TreatmentDrugId treatmentDrugId) {
        this.treatmentDrugId = treatmentDrugId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Transient
    public Treatment getTreatment() {
        return this.treatmentDrugId.getTreatment();
    }

    public void setTreatment(Treatment treatment) {
        this.treatmentDrugId.setTreatment(treatment);
    }

    @Transient
    public Drug getDrug() {
        return this.treatmentDrugId.getDrug();
    }

    public void setDrug(Drug drug) {
        this.treatmentDrugId.setDrug(drug);
    }
}
