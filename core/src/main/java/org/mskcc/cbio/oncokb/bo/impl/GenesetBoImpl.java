package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.GenesetBo;
import org.mskcc.cbio.oncokb.dao.GenesetDao;
import org.mskcc.cbio.oncokb.model.Geneset;

import java.util.List;

/**
 * Created by Hongxin Zhang on 2019-08-08.
 */
public class GenesetBoImpl extends GenericBoImpl<Geneset, GenesetDao> implements GenesetBo {
    @Override
    public Geneset findGenesetByUuid(String uuid) {
        return getDao().findByUuid(uuid);
    }

    @Override
    public Geneset findGenesetByName(String name) {
        List<Geneset> genesetList = getDao().findByNamedQuery("findGenesetByName", name);
        return genesetList.size() == 0 ? null : genesetList.iterator().next();
    }
}
