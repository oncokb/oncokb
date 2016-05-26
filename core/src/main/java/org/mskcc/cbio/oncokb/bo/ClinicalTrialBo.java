
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;

import org.mskcc.cbio.oncokb.model.*;

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
    ClinicalTrial findClinicalTrialByNctId(String nciId);
}
