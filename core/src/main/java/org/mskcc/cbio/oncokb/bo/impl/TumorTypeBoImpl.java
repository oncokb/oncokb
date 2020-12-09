package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.model.TumorType;


public class TumorTypeBoImpl extends GenericBoImpl<TumorType, TumorTypeDao> implements TumorTypeBo {
    @Override
    public TumorType findTumorTypeByCode(String code) {
        return getDao().findTumorTypeByCode(code);
    }
}
