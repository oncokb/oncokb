
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.NccnGuideline;

/**
 *
 * @author jgao
 */
public interface NccnGuidelineBo extends GenericBo<NccnGuideline> {
    
    NccnGuideline findNccnGuideline(String disease, String version, String pages);
}
