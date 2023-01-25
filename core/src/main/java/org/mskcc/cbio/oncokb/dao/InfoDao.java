package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.Info;


/**
 *
 * @author Hongxin Zhang
 */
public interface InfoDao extends GenericDao<Info, Integer> {
    Info get();

    void update(Info info);
}
