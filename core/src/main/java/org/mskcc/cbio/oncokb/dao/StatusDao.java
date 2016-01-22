/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.*;

import java.util.List;

/**
 * @author Hongxin
 */
public interface StatusDao extends GenericDao<Status, Integer> {
    /**
     * Find status by alterations
     *
     * @param alteration
     * @return
     */
    List<Status> findStatusByAlteration(Alteration alteration);


    /**
     * Find status by alterations and statusType
     *
     * @param alteration
     * @param statusType
     * @return
     */
    List<Status> findStatusByAlterationAndStatusType(Alteration alteration, StatusType statusType);
}
