/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public interface TumorTypeDao extends GenericDao<TumorType, String> {

    TumorType findTumorTypeById(String tumorTypeId);
    TumorType findTumorTypeByName(String tumorTypeName);
    
    /**
     * 
     * @param alteration
     * @return 
     */
    List<TumorType> findTumorTypesWithEvidencesForAlteration(Alteration alteration);
}
