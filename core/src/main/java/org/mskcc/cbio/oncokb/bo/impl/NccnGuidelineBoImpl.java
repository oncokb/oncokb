

package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.NccnGuidelineBo;
import org.mskcc.cbio.oncokb.dao.NccnGuidelineDao;
import org.mskcc.cbio.oncokb.model.NccnGuideline;

/**
 *
 * @author jgao
 */
public class NccnGuidelineBoImpl extends GenericBoImpl<NccnGuideline, NccnGuidelineDao> implements NccnGuidelineBo {

    @Override
    public NccnGuideline findNccnGuideline(String disease, String version, String pages) {
        return getDao().findNccnGuideline(disease, version, pages);
    }
}
