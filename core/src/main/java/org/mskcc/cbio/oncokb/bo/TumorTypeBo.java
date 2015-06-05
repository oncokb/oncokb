/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.Set;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public interface TumorTypeBo extends GenericBo<TumorType> {
    TumorType findTumorTypeById(String tumorTypeId);
    TumorType findTumorTypeByName(String tumorTypeName);
    
    /**
     * 
     * @param alteration
     * @return 
     */
    Set<TumorType> findTumorTypesWithEvidencesForAlterations(Collection<Alteration> alterations);
}
