package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Created by Hongxin Zhang on 10/2/17.
 */

@Embeddable
public class TreatmentDrugId implements Serializable {

    @ManyToOne
    @JsonIgnore
    private Treatment treatment;

    @ManyToOne
    private Drug drug;

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }
}
