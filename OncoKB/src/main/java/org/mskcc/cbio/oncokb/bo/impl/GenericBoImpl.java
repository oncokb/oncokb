/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.GenericBo;
import org.mskcc.cbio.oncokb.dao.GenericDao;

/**
 *
 * @author jgao
 */
public class GenericBoImpl<T, DAO extends GenericDao> implements GenericBo<T> {

    private DAO dao;

    public void setDao(DAO dao) {
        this.dao = dao;
    }
    
    protected DAO getDao() {
        return dao;
    }
    
    public void saveOrUpdate(T t) {
        dao.saveOrUpdate(t);
    }
    
}
