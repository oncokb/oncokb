package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.DrugGroup;

import java.util.List;


public interface DrugGroupDao extends GenericDao<DrugGroup, Integer> {
    /**
     * @param groupName
     * @return
     */
    DrugGroup findGroupByName(String groupName);

    /**
     * @param drug
     * @return
     */
    List<DrugGroup> findGroupsByDrug(Drug drug);
}
