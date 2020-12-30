package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.CacheUtils;

import java.util.List;


public class TumorTypeBoImpl extends GenericBoImpl<TumorType, TumorTypeDao> implements TumorTypeBo {
    @Override
    public TumorType findTumorTypeByCode(String code) {
        return CacheUtils.findTumorTypeByCode(code);
    }

    @Override
    public List<TumorType> findAll() {
        return CacheUtils.getAllCancerTypes();
    }
}
