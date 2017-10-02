package org.mskcc.cbio.oncokb.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Hongxin Zhang on 9/29/17.
 */
@Entity
@Table(name = "evidence_treatment")
@AssociationOverrides({
    @AssociationOverride(name = "evidenceTreatmentId.evidence",
        joinColumns = @JoinColumn(name = "evidence_id")),
    @AssociationOverride(name = "evidenceTreatmentId.treatment",
        joinColumns = @JoinColumn(name = "treatment_id"))
})
public class EvidenceTreatment implements Serializable {

    @EmbeddedId
    private EvidenceTreatmentId evidenceTreatmentId = new EvidenceTreatmentId();

    public EvidenceTreatmentId getEvidenceTreatmentId() {
        return evidenceTreatmentId;
    }

    public void setEvidenceTreatmentId(EvidenceTreatmentId evidenceTreatmentId) {
        this.evidenceTreatmentId = evidenceTreatmentId;
    }

    private Integer priority;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Transient
    public Evidence getEvidence() {
        return this.evidenceTreatmentId.getEvidence();
    }

    public void setEvidence(Evidence evidence) {
        this.evidenceTreatmentId.setEvidence(evidence);
    }

    @Transient
    public Treatment getTreatment() {
        return this.evidenceTreatmentId.getTreatment();
    }

    public void setTreatment(Treatment treatment) {
        this.evidenceTreatmentId.setTreatment(treatment);
    }
}
