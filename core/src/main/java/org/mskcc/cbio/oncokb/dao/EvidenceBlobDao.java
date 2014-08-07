/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.EvidenceBlob;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 *
 * @author jgao
 */
public interface EvidenceBlobDao extends GenericDao<EvidenceBlob, Integer> {
    /**
     * Find AlterationActivityEvidences by alterations
     * @param alteration
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration);
    
    /**
     * Find AlterationActivityEvidences by Entrez Gene ID
     * @param entrezGeneId
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene);
}
