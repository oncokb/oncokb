package org.mskcc.cbio.oncokb.model;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Created by Hongxin Zhang on 9/29/17.
 */

@Embeddable
public class EvidenceTreatmentId implements Serializable {

    @ManyToOne
    private Evidence evidence;

    @ManyToOne
    private Treatment treatment;

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }
}
