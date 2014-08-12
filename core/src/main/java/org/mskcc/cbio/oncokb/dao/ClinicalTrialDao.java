package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.ClinicalTrial;



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
}
