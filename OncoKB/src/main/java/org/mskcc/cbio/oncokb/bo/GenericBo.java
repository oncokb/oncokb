/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.List;

/**
 * 
 * @author jgao
 * @param <T> 
 */
public interface GenericBo<T> {
    
    void saveOrUpdate(T t);
    
    List<T> findAll();
}
