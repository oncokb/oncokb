

package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.ClinicalTrialDao;
import org.mskcc.cbio.oncokb.model.*;

/**
 *
 * @author jgao
 */
public class ClinicalTrialDaoImpl
            extends GenericDaoImpl<ClinicalTrial, Integer>
            implements ClinicalTrialDao  {
    public ClinicalTrial findClinicalTrialByNctId(String nciId) {
        List<ClinicalTrial> list = findByNamedQuery("findClinicalTrialByNctId", nciId);
        return list.isEmpty() ? null : list.get(0);
    }
}
