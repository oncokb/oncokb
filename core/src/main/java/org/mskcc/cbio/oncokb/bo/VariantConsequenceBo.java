
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.VariantConsequence;

/**
 *
 * @author jgao
 */
public interface VariantConsequenceBo extends GenericBo<VariantConsequence> {
    
    /**
     * 
     * @param term
     * @return 
     */
    VariantConsequence findVariantConsequenceByTerm(String term);
}
