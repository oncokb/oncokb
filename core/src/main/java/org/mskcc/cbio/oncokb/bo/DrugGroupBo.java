package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.DrugGroup;

import java.util.List;

public interface DrugGroupBo extends GenericBo<DrugGroup> {
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
