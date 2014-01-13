/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Evidence;

/**
 *
 * @author jgao
 */
public interface EvidenceBo extends GenericBo<Evidence> {
    /**
     * Find Evidences by alteration ID
     * @param alterationId
     * @return 
     */
    List<Evidence> findEvidencesByAlterationId(int alterationId);
    
    /**
     * Find Evidences by Entrez Gene ID
     * @param entrezGeneId
     * @return 
     */
    List<Evidence> findEvidencesByGene(int entrezGeneId);
}
