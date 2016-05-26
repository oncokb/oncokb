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
}
