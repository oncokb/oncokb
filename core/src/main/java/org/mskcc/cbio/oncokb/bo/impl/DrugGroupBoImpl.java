package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.DrugGroupBo;
import org.mskcc.cbio.oncokb.dao.DrugGroupDao;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.DrugGroup;

import java.util.List;

/**
 * Created by Hongxin Zhang on 10/5/18.
 */
public class DrugGroupBoImpl extends GenericBoImpl<DrugGroup, DrugGroupDao> implements DrugGroupBo {
    @Override
    public DrugGroup findGroupByName(String groupName) {
        return getDao().findGroupByName(groupName);
    }

    @Override
    public List<DrugGroup> findGroupsByDrug(Drug drug) {
        return getDao().findGroupsByDrug(drug);
    }
}
