/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 *
 * @author jgao
 */
public interface EvidenceDao extends GenericDao<Evidence, Integer> {
    /**
     * Find AlterationActivityEvidences by alterations
     * @param alteration
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Alteration alteration);
    
    /**
     * Find AlterationActivityEvidences by Entrez Gene ID
     * @param entrezGeneId
     * @return 
     */
    List<Evidence> findEvidencesByGene(Gene gene);
}
