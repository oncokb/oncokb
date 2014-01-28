/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Evidence;

/**
 *
 * @author jgao
 */
public interface EvidenceDao extends GenericDao<Evidence, Integer> {
    /**
     * Find AlterationActivityEvidences by alteration ID
     * @param alterationId
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(int alterationId);
    
    /**
     * Find AlterationActivityEvidences by Entrez Gene ID
     * @param entrezGeneId
     * @return 
     */
    List<Evidence> findEvidencesByGene(int entrezGeneId);
}
