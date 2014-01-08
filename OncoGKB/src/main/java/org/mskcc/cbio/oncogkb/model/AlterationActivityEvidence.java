/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.model;

import org.mskcc.cbio.oncogkb.model.TumorType;

/**
 *
 * @author jgao
 */
public interface AlterationActivityEvidence {

    Alteration getAlteration();

    Integer getAlterationActivityEvidenceId();

    String getDescriptionOfKnownEffect();

    String getGenomicContext();

    String getKnownEffect();

    String getPmids();

    TumorType getTumorType();

    void setAlteration(Alteration alteration);

    void setAlterationActivityEvidenceId(Integer alterationActivityEvidenceId);

    void setDescriptionOfKnownEffect(String descriptionOfKnownEffect);

    void setGenomicContext(String genomicContext);

    void setKnownEffect(String knownEffect);

    void setPmids(String pmids);

    void setTumorType(TumorType tumorType);
    
}
