package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.*;
/**
 *
 * @author Hongxin Zhang
 */
public interface GenesetDao extends GenericDao<Geneset, Integer> {
    Geneset findByUuid(String uuid);
}
