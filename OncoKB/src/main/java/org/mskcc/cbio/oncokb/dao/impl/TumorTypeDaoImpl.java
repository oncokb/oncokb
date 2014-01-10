/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public class TumorTypeDaoImpl extends GenericDaoImpl<TumorType, String> implements TumorTypeDao {
    
    @Override
    public TumorType getTumorTypeById(String tumorTypeId) {
        return findById(tumorTypeId);
    }
}
