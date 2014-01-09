/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author jgao
 */
public class AlterationDaoImpl extends HibernateDaoSupport implements AlterationDao {

    public List<Alteration> getAlterations(long entrezGeneId) {
        return getHibernateTemplate().find("from AlterationImpl where entrez_gene_id=?", entrezGeneId);
    }

    public void saveOrUpdate(Alteration alteration) {
        getHibernateTemplate().saveOrUpdate(alteration);
    }
    
}
