/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Adapted from http://wordgraphs.com/post/604/Generic-DAO-design-pattern-with-Java---Hibernate
 * & https://community.jboss.org/wiki/GenericDataAccessObjects#jive_content_id_Preparing_DAOs_with_factories
 * @author jgao
 * @param <T>
 * @param <ID>
 */
public interface GenericDao<T, ID extends Serializable> {

    /**
     *
     * @param id
     * @return
     */
    T findById(ID id);

    /**
     *
     * @param queryString
     * @return
     */
    List<T> find(String queryString);

    /**
     *
     * @param queryString
     * @param values
     * @return
     */
    List<T> find(String queryString, Object... values);

    /**
     *
     * @param param
     * @param value
     * @return
     */
    List<T> findByParamValue(String param, Object value);

    /**
     *
     * @param params
     * @param values values must be the same number as params
     * @return
     */
    List<T> findByParamValues(String[] params, Object[] values);

    /**
     *
     * @param queryName
     * @return
     */
    List<T> findByNamedQuery(String queryName);

    /**
     *
     * @param queryName
     * @param value
     * @return
     */
    List<T> findByNamedQuery(String queryName, Object value);

    /**
     *
     * @param queryName
     * @param values
     * @return
     */
    List<T> findByNamedQuery(String queryName, Object... values);

    /**
     *
     * @param queryName
     * @param params
     * @param values
     * @return
     */
    List<T> findByNamedQueryAndNamedParam(String queryName, String[] params, List[] values);

    /**
     *
     * @param <C>
     * @param queryName
     * @param values
     * @return
     */
    <C> List<C> findByNamedQueryOfAnyType(String queryName, Object... values);

    /**
     *
     * @param t
     */
    void save(T t);

    /**
     *
     * @param t
     */
    void update(T t);

    /**
     *
     * @param t
     */
    void saveOrUpdate(T t);

    /**
     *
     * @return
     */
    List<T> findAll();

    /**
     *
     * @return
     */
    int countAll();

    /**
     *
     * @param t
     */
    void delete(T t);

    void deleteAll(List<T> ts);

    /**
     *
     * @param cacheQueries
     */
    void setCacheQueries(boolean cacheQueries);
}
