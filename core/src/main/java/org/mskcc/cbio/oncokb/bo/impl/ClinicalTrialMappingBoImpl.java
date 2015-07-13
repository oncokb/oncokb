/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.ClinicalTrialMappingBo;
import org.mskcc.cbio.oncokb.dao.ClinicalTrialMappingDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.ClinicalTrialMapping;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jgao
 */
public class ClinicalTrialMappingBoImpl extends GenericBoImpl<ClinicalTrialMapping, ClinicalTrialMappingDao> implements ClinicalTrialMappingBo {
    
    @Override
    public ClinicalTrialMapping findMappingById(String mappingId) {
        return getDao().findMappingById(mappingId);
    }

    @Override
    public Set<ClinicalTrialMapping> findMappingByAlterationTumorType(Collection<Alteration> alterations, Collection<TumorType> tumorTypes) {
        Set<ClinicalTrialMapping> mappings = new HashSet<ClinicalTrialMapping>();
        for (TumorType tumorType : tumorTypes) {
            for (Alteration alteration : alterations) {
                for (ClinicalTrialMapping mapping : getDao().findMappingByAlterationTumorType(alteration, tumorType)) {
                    mappings.add(mapping);
                }
            }
        }
        return mappings;
    }
}
