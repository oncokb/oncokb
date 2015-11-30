/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.*;

import java.util.Collection;
import java.util.List;

/**
 * @author Hongxin
 */
public interface StatusBo extends GenericBo<Status> {
    /**
     * Find Status by alteration ID
     *
     * @param alterations
     * @return
     */
    List<Status> findStatusByAlteration(Collection<Alteration> alterations);


    List<Status> findStatusByAlterationAndStatusType(Collection<Alteration> alterations, Collection<StatusType> statusTypes);
}
