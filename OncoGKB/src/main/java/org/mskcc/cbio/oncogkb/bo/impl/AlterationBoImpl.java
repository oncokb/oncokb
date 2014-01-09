/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.bo.impl;

import java.util.List;
import org.mskcc.cbio.oncogkb.bo.AlterationBo;
import org.mskcc.cbio.oncogkb.dao.AlterationDao;
import org.mskcc.cbio.oncogkb.model.Alteration;

/**
 *
 * @author jgao
 */
public class AlterationBoImpl implements AlterationBo {

    AlterationDao alterationDao;

    public void setAlterationDao(AlterationDao alterationDao) {
        this.alterationDao = alterationDao;
    }

    public List<Alteration> getAlterations(long entrezGeneId) {
        return alterationDao.getAlterations(entrezGeneId);
    }

    public void saveOrUpdate(Alteration alteration) {
        alterationDao.saveOrUpdate(alteration);
    }
    
}
