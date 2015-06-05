

package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.dao.ClinicalTrialDao;
import org.mskcc.cbio.oncokb.model.Alteration;
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
    public List<ClinicalTrial> findClinicalTrialByTumorTypeAndDrug(Collection<TumorType> tumorTypes, Collection<Drug> drugs, boolean openTrialsOnly) {
        Set<ClinicalTrial> trials = new LinkedHashSet<ClinicalTrial>();
        for (TumorType tumorType : tumorTypes) {
            for (Drug drug : drugs) {
                if (!openTrialsOnly) {
                    trials.addAll(getDao().findClinicalTrialByTumorTypeAndDrug(tumorType, drug));
                } else {
                    for (ClinicalTrial trial : getDao().findClinicalTrialByTumorTypeAndDrug(tumorType, drug)) {
                        if (trial.isOpen()) {
                            trials.add(trial);
                        }
                    }
                }
            }
        }
        return new ArrayList<ClinicalTrial>(trials);
    }

    @Override
    public List<ClinicalTrial> findClinicalTrialByTumorType(Collection<TumorType> tumorTypes, boolean openTrialsOnly) {
        Set<ClinicalTrial> trials = new LinkedHashSet<ClinicalTrial>();
        for (TumorType tumorType : tumorTypes) {
            if (!openTrialsOnly) {
                trials.addAll(getDao().findClinicalTrialByTumorType(tumorType));
            } else {
                for (ClinicalTrial trial : getDao().findClinicalTrialByTumorType(tumorType)) {
                    if (trial.isOpen()) {
                        trials.add(trial);
                    }
                }
            }
        }
        return new ArrayList<ClinicalTrial>(trials);    
    }

    @Override
    public List<ClinicalTrial> findClinicalTrialByTumorTypeAndAlteration(Collection<TumorType> tumorTypes, Collection<Alteration> alterations, boolean openTrialsOnly) {
        Set<ClinicalTrial> trials = new LinkedHashSet<ClinicalTrial>();
        for (TumorType tumorType : tumorTypes) {
            for (Alteration alteration : alterations) {
                if (!openTrialsOnly) {
                    trials.addAll(getDao().findClinicalTrialByTumorTypeAndAlteration(tumorType, alteration));
                } else {
                    for (ClinicalTrial trial : getDao().findClinicalTrialByTumorTypeAndAlteration(tumorType, alteration)) {
                        if (trial.isOpen()) {
                            trials.add(trial);
                        }
                    }
                }
            }
        }
        return new ArrayList<ClinicalTrial>(trials);
    }
}
