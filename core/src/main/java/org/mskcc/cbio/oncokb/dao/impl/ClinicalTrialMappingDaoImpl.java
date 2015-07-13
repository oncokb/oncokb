/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.ClinicalTrialMappingDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.ClinicalTrialMapping;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.List;

/**
 *
 * @author jgao
 */
public class ClinicalTrialMappingDaoImpl extends GenericDaoImpl<ClinicalTrialMapping, String> implements ClinicalTrialMappingDao {
    
    @Override
    public ClinicalTrialMapping findMappingById(String mappingId) {
        return findById(mappingId);
    }

    @Override
    public List<ClinicalTrialMapping> findMappingByAlterationTumorType(Alteration alteration, TumorType tumorType) {
        return findByNamedQuery("findMappingByAlterationTumorType", alteration, tumorType);
    }
}
