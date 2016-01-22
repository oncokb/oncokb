/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.dao.StatusDao;
import org.mskcc.cbio.oncokb.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hongxin
 */
public class StatusDaoImpl
        extends GenericDaoImpl<Status, Integer>
        implements StatusDao {
    @Override
    public List<Status> findStatusByAlteration(Alteration alteration) {
        return findByNamedQuery("findStatusByAlteration", alteration.getAlterationId());
    }

    @Override
    public List<Status> findStatusByAlterationAndStatusType(Alteration alteration, StatusType statusType) {
        return findByNamedQuery("findStatusByAlterationAndStatusType", alteration.getAlterationId(), statusType);
    }
}
