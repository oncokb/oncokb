

package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.InfoBo;
import org.mskcc.cbio.oncokb.dao.InfoDao;
import org.mskcc.cbio.oncokb.model.Info;

/**
 * @author Hongxin Zhang
 */
public class InfoBoImpl extends GenericBoImpl<Info, InfoDao> implements InfoBo {

    @Override
    public Info get() {
        return getDao().get();
    }

    @Override
    public void update(Info info) {
        getDao().update(info);
    }
}
