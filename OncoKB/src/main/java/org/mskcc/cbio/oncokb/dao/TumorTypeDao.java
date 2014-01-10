/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public interface TumorTypeDao extends GenericDao<TumorType, String> {

    TumorType findTumorTypeById(String tumorTypeId);
}
