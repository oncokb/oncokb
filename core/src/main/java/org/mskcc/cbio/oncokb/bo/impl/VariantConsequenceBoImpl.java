

package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.VariantConsequenceBo;
import org.mskcc.cbio.oncokb.dao.VariantConsequenceDao;
import org.mskcc.cbio.oncokb.model.VariantConsequence;

/**
 *
 * @author jgao
 */
public class VariantConsequenceBoImpl extends GenericBoImpl<VariantConsequence, VariantConsequenceDao> implements VariantConsequenceBo {

    @Override
    public VariantConsequence findVariantConsequenceByTerm(String term) {
        return getDao().findVariantConsequenceByTerm(term);
    }
}
