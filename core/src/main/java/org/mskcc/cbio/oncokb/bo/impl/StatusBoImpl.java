/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.StatusBo;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.dao.StatusDao;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;

/**
 * @author Hongxin
 */
public class StatusBoImpl extends GenericBoImpl<Status, StatusDao> implements StatusBo {
    @Override
    public List<Status> findStatusByAlteration(Collection<Alteration> alterations) {
        Set<Status> set = new LinkedHashSet<>();
        for (Alteration alteration : alterations) {
            set.addAll(getDao().findStatusByAlteration(alteration));
        }
        return new ArrayList<Status>(set);
    }

    @Override
    public List<Status> findStatusByAlterationAndStatusType(Collection<Alteration> alterations, Collection<StatusType> statusTypes) {
        Set<Status> set = new LinkedHashSet<>();
        for (Alteration alteration : alterations) {
            for (StatusType statusType : statusTypes) {
                set.addAll(getDao().findStatusByAlterationAndStatusType(alteration, statusType));
            }
        }
        return new ArrayList<Status>(set);
    }
}
