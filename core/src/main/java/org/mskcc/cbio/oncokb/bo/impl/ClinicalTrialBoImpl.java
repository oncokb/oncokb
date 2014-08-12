

package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.dao.ClinicalTrialDao;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;

/**
 *
 * @author jgao
 */
public class ClinicalTrialBoImpl extends GenericBoImpl<ClinicalTrial, ClinicalTrialDao> implements ClinicalTrialBo {

    @Override
    public ClinicalTrial findClinicalTrialByPmid(String nctId) {
        return getDao().findClinicalTrialByNctId(nctId);
    }
}
