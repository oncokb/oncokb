package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.dao.impl.GenericDaoImpl;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.List;

/**
 * Created by Hongxin Zhang on 12/7/20.
 */
public class TumorTypeDaoImpl
    extends GenericDaoImpl<TumorType, Integer> implements TumorTypeDao {

    @Override
    public TumorType findTumorTypeByCode(String code) {
        List<TumorType> matchResult = findByNamedQuery("findTumorTypeByCode", code);
        if (matchResult == null || matchResult.isEmpty()) {
            return null;
        } else {
            return matchResult.iterator().next();
        }
    }
}
