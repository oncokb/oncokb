package org.mskcc.cbio.oncokb.dao;

import java.util.List;

import org.mskcc.cbio.oncokb.model.*;


/**
 *
 * @author jgao
 */
public interface ClinicalTrialDao extends GenericDao<ClinicalTrial, Integer> {
    /**
     * 
     * @param nctId
     * @return 
     */
    ClinicalTrial findClinicalTrialByNctId(String nctId);
    
    /**
     * 
     * @param tumorType
     * @return 
     */
    List<ClinicalTrial> findClinicalTrialByTumorType(TumorType tumorType);
    
    /**
     * 
     * @param tumorType
     * @param drug
     * @return 
     */
    List<ClinicalTrial> findClinicalTrialByTumorTypeAndDrug(TumorType tumorType, Drug drug);
    
    /**
     * 
     * @param tumorType
     * @param alteration
     * @return
    */
    List<ClinicalTrial> findClinicalTrialByTumorTypeAndAlteration(TumorType tumorType, Alteration alteration);
}
