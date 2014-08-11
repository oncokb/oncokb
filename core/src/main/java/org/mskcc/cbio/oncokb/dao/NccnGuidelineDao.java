package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.NccnGuideline;



/**
 *
 * @author jgao
 */
public interface NccnGuidelineDao extends GenericDao<NccnGuideline, Integer> {
    /**
     * 
     * @param disease
     * @param version
     * @param pages
     * @return 
     */
    NccnGuideline findNccnGuideline(String disease, String version, String pages);
}
