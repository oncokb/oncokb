/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.dao.GenericDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Adapted from http://wordgraphs.com/post/604/Generic-DAO-design-pattern-with-Java---Hibernate
 * & https://community.jboss.org/wiki/GenericDataAccessObjects#jive_content_id_Preparing_DAOs_with_factories
 * @author jgao
 * @param <T>
 * @param <ID>
 */
public abstract class GenericDaoImpl<T, ID extends Serializable> extends HibernateDaoSupport implements GenericDao<T, ID> {

    private Class<T> type;

    protected Class<T> getType()
    {
        return this.type;
    }

    protected String getClassName()
    {
        return type.getName();
    }

    @SuppressWarnings("unchecked")
    public GenericDaoImpl()
    {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        type = (Class)pt.getActualTypeArguments()[0];
    }

    @Override
    public T findById(ID id) {
        return getHibernateTemplate().get(type, id);
    }

    @Override
    public List<T> find(String queryString) {
        return (List<T>) getHibernateTemplate().find(queryString);
    }

    @Override
    public List<T> find(String queryString, Object... values) {
        return (List<T>) getHibernateTemplate().find(queryString, values);
    }

    public List<T> findByParamValue(String param, Object value) {
        String queryString = "from " + getClassName() + " where " + param + "=?";
        return find(queryString, value);
    }

    @Override
    public List<T> findByNamedQuery(String queryName) {
        return (List<T>) getHibernateTemplate().findByNamedQuery(queryName);
    }

    @Override
    public List<T> findByNamedQuery(String queryName, Object value) {
        return (List<T>) getHibernateTemplate().findByNamedQuery(queryName, value);
    }

    @Override
    public List<T> findByNamedQuery(String queryName, Object... values) {
        return (List<T>) getHibernateTemplate().findByNamedQuery(queryName, values);
    }

    @Override
    public List<T> findByNamedQueryAndNamedParam(String queryName, String[] params, List[] values) {
        return (List<T>) getHibernateTemplate().findByNamedQueryAndNamedParam(queryName, params, values);
    }

    @Override
    public <C> List<C> findByNamedQueryOfAnyType(String queryName, Object... values) {
        return (List<C>)getHibernateTemplate().findByNamedQuery(queryName, values);
    }

    @Override
    public void save(T t)
    {
        if (t!=null) {
            getHibernateTemplate().save(t);
        }
    }

    @Override
    public void update(T t) {
        if (t!=null) {
            getHibernateTemplate().update(t);
        }
    }

    @Override
    public void saveOrUpdate(T t) {
        if (t!=null) {
            getHibernateTemplate().saveOrUpdate(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findAll()
    {

        return (List<T>)getHibernateTemplate().find("FROM " + getClassName());
    }

    @Override
    public int countAll()
    {
        return DataAccessUtils.intResult(find("select count(*) from " + getClassName()));
    }

    @Override
    public void delete(T obj)
    {
        if(obj != null) {
            getHibernateTemplate().delete(obj);
        }
    }

    @Override
    public void deleteAll(List<T> objs) {
        if(objs != null) {
            getHibernateTemplate().deleteAll(objs);
        }
    }

    @Override
    public void setCacheQueries(boolean cacheQueries) {
        getHibernateTemplate().setCacheQueries(cacheQueries);
    }
}
