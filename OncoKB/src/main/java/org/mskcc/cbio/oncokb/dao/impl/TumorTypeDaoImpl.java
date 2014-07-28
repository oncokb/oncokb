/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public class TumorTypeDaoImpl extends GenericDaoImpl<TumorType, String> implements TumorTypeDao {
    
    @Override
    public TumorType findTumorTypeById(String tumorTypeId) {
        return findById(tumorTypeId);
    }

    @Override
    public TumorType findTumorTypeByName(String tumorTypeName) {
        List<TumorType> list = findByNamedQuery("findTumorTypeByName", tumorTypeName);
        return list.isEmpty() ? null : list.get(0);
    }
}
