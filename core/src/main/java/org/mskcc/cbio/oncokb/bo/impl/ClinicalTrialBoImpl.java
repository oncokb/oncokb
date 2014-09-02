

package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.dao.ClinicalTrialDao;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public class ClinicalTrialBoImpl extends GenericBoImpl<ClinicalTrial, ClinicalTrialDao> implements ClinicalTrialBo {

    @Override
    public ClinicalTrial findClinicalTrialByNctId(String nctId) {
        return getDao().findClinicalTrialByNctId(nctId);
    }

    @Override
    public List<ClinicalTrial> findClinicalTrialByTumorTypeAndDrug(Collection<TumorType> tumorTypes, Collection<Drug> drugs) {
        Set<ClinicalTrial> trials = new LinkedHashSet<ClinicalTrial>();
        for (TumorType tumorType : tumorTypes) {
            for (Drug drug : drugs) {
                trials.addAll(getDao().findClinicalTrialByTumorTypeAndDrug(tumorType, drug));
            }
        }
        return new ArrayList<ClinicalTrial>(trials);
    }
}
