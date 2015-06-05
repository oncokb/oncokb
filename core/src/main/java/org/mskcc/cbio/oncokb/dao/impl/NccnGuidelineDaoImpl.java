

package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.NccnGuidelineDao;
import org.mskcc.cbio.oncokb.model.NccnGuideline;

/**
 *
 * @author jgao
 */
public class NccnGuidelineDaoImpl
            extends GenericDaoImpl<NccnGuideline, Integer>
            implements NccnGuidelineDao  {
    
    public NccnGuideline findNccnGuideline(String disease, String version, String pages) {
        List<NccnGuideline> list = findByNamedQuery("findNccnGuideline", disease, version, pages);
        return list.isEmpty() ? null : list.get(0);
    }
}
