/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public interface TumorTypeBo extends GenericBo<TumorType> {

    TumorType findTumorTypeById(String tumorTypeId);
    
}
