/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.util;

import org.hibernate.Session;

/**
 *
 * @author jgao
 */
public final class DbQueryHelper {
    
    Session session = null;

    public DbQueryHelper() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
    }
}
