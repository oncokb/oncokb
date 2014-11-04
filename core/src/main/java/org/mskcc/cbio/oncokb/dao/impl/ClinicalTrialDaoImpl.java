

package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.ClinicalTrialDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.TumorType;

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

    @Override
    public List<ClinicalTrial> findClinicalTrialByTumorTypeAndDrug(TumorType tumorType, Drug drug) {
        return findByNamedQuery("findClinicalTrialByTumorTypeAndDrug", tumorType.getTumorTypeId(), drug.getDrugId());
    }

    @Override
    public List<ClinicalTrial> findClinicalTrialByTumorType(TumorType tumorType) {
        return findByNamedQuery("findClinicalTrialByTumorType", tumorType.getTumorTypeId());
    }

    @Override
    public List<ClinicalTrial> findClinicalTrialByTumorTypeAndAlteration(TumorType tumorType, Alteration alteration) {
        return findByNamedQuery("findClinicalTrialByTumorTypeAndAlteration", tumorType.getTumorTypeId(), alteration.getAlterationId());
    }
}
