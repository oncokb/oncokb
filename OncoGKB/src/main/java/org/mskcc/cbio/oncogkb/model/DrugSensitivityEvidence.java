/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.model;

/**
 *
 * @author jgao
 */
public interface DrugSensitivityEvidence {

    Alteration getAlteration();

    String getContext();

    String getDescriptionOfKnownEffect();

    Drug getDrug();

    Integer getDrugSensitivityEvidenceId();

    String getKnownEffect();

    String getPmids();

    TumorType getTumorType();

    void setAlteration(Alteration alteration);

    void setContext(String context);

    void setDescriptionOfKnownEffect(String descriptionOfKnownEffect);

    void setDrug(Drug drug);

    void setDrugSensitivityEvidenceId(Integer drugSensitivityEvidenceId);

    void setKnownEffect(String knownEffect);

    void setPmids(String pmids);

    void setTumorType(TumorType tumorType);
    
}
