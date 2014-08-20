

package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.VariantConsequenceDao;
import org.mskcc.cbio.oncokb.model.VariantConsequence;

/**
 *
 * @author jgao
 */
public class VariantConsequenceDaoImpl
            extends GenericDaoImpl<VariantConsequence, Integer>
            implements VariantConsequenceDao  {
    
    @Override
    public VariantConsequence findVariantConsequenceByTerm(String term) {
        List<VariantConsequence> list = findByNamedQuery("findVariantConsequenceByTerm", term);
        return list.isEmpty() ? null : list.get(0);
    }
}
