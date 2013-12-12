/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.dao;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.mskcc.cbio.oncogkb.model.Gene;

/**
 *
 * @author jgao
 */
public final class DbQueryHelper {
    
    private DbQueryHelper() {}
    
    public static Gene getGeneByHugoSymbol(String symbol) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query q = session.createQuery ("from Gene as gene where gene.hugoSymbol=?");
        q.setString(0, symbol);
        List list = q.list();
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return Gene.class.cast(list.get(0));
    }
    
    public static Gene getGeneByEntrezGeneId(int entrezGeneId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query q = session.createQuery ("from Gene as gene where gene.entrezGeneId=?");
        q.setInteger(0, entrezGeneId);
        List list = q.list();
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return Gene.class.cast(list.get(0));
    }
}
