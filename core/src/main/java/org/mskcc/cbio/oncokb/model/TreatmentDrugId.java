package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.CacheUtils;

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
    @JsonUnwrapped
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
        Drug persistence = null;
        if (CacheUtils.isEnabled()) {
            if (CacheUtils.getAllDrugs().contains(drug)) {
                persistence = drug;
            }
        } else {
            persistence = ApplicationContextSingleton.getDrugBo().guessUnambiguousDrug(drug.getDrugName());
        }
        if (persistence == null) {
            ApplicationContextSingleton.getDrugBo().save(drug);
            if (CacheUtils.isEnabled()) {
                CacheUtils.addDrug(drug);
            }
            persistence = drug;
        }
        this.drug = persistence;
    }
}
