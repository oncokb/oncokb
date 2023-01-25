package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.InfoDao;
import org.mskcc.cbio.oncokb.model.Info;

import java.util.List;

/**
 * @author Hongxin Zhang
 */
public class InfoDaoImpl
    extends GenericDaoImpl<Info, Integer>
    implements InfoDao {
    @Override
    public Info get() {
        List<Info> infoList = findAll();
        // the info table have and should only have one row
        return infoList.size() > 0 ? infoList.iterator().next() : null;
    }

    @Override
    public void update(Info info) {
        super.update(info);
    }
}
