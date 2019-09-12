package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.GenesetDao;
import org.mskcc.cbio.oncokb.model.*;

import java.util.List;


/**
 * @author Hongxin Zhang
 */
public class GenesetDaoImpl
    extends GenericDaoImpl<Geneset, Integer>
    implements GenesetDao {
    @Override
    public Geneset findByUuid(String uuid) {
        List<Geneset> matches = findByNamedQuery("findGenesetByUUID", uuid);
        return matches.isEmpty() ? null : matches.iterator().next();
    }
}
