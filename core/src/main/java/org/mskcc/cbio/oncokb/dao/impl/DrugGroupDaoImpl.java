package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.DrugGroupDao;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.DrugGroup;

import java.util.List;

/**
 * Created by Hongxin Zhang on 10/5/18.
 */
public class DrugGroupDaoImpl extends GenericDaoImpl<DrugGroup, Integer> implements DrugGroupDao {
    @Override
    public DrugGroup findGroupByName(String groupName) {
        List<DrugGroup> list = findByNamedQuery("findGroupByName", groupName);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<DrugGroup> findGroupsByDrug(Drug drug) {
        return findByNamedQuery("findGroupsByDrug", drug);
    }
}
