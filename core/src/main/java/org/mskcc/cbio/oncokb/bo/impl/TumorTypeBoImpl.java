/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public class TumorTypeBoImpl extends GenericBoImpl<TumorType, TumorTypeDao> implements TumorTypeBo {
    
    @Override
    public TumorType findTumorTypeById(String tumorTypeId) {
        return getDao().findTumorTypeById(tumorTypeId);
    }   

    @Override
    public TumorType findTumorTypeByName(String tumorTypeName) {
        return getDao().findTumorTypeByName(tumorTypeName);
    }

    @Override
    public List<TumorType> findTumorTypesWithEvidencesForAlteration(Alteration alteration) {
        return getDao().findTumorTypesWithEvidencesForAlteration(alteration);
    }
}
