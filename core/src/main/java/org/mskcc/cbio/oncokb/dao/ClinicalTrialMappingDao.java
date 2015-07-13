/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.ClinicalTrialMapping;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.List;

/**
 *
 * @author jgao
 */
public interface ClinicalTrialMappingDao extends GenericDao<ClinicalTrialMapping, String> {

    ClinicalTrialMapping findMappingById(String mappingId);

    /**
     *
     * @param alteration
     * @param tumorType
     * @return
     */
    List<ClinicalTrialMapping> findMappingByAlterationTumorType(Alteration alteration, TumorType tumorType);
}
