
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.ClinicalTrial;

/**
 *
 * @author jgao
 */
public interface ClinicalTrialBo extends GenericBo<ClinicalTrial> {
    
    /**
     * 
     * @param nciId
     * @return 
     */
    ClinicalTrial findClinicalTrialByPmid(String nciId);
}
