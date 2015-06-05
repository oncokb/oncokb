package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.VariantConsequence;



/**
 *
 * @author jgao
 */
public interface VariantConsequenceDao extends GenericDao<VariantConsequence, Integer> {
    /**
     * 
     * @param term
     * @return 
     */
    VariantConsequence findVariantConsequenceByTerm(String term);
}
