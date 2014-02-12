/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public class TumorTypeBoImpl extends GenericBoImpl<TumorType, TumorTypeDao> implements TumorTypeBo {
    
    @Override
    public List<TumorType> findTumorTypesById(Collection<String> tumorTypeIds) {
        List<TumorType> tumorTypes = new ArrayList<TumorType>();
        for (String tumorTypeId : tumorTypeIds) {
            TumorType tumorType = getDao().findTumorTypeById(tumorTypeId);
            if (tumorType != null) {
                tumorTypes.add(tumorType);
            }
        }
        return tumorTypes;
    }   

}
