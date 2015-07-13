/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.ClinicalTrialMapping;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author jgao
 */
public interface ClinicalTrialMappingBo extends GenericBo<ClinicalTrialMapping> {
    ClinicalTrialMapping findMappingById(String tumorTypeId);
    
    Set<ClinicalTrialMapping> findMappingByAlterationTumorType(Collection<Alteration> alterations, Collection<TumorType> tumorTypes);
}
