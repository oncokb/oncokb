/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.EvidenceBlob;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.TumorType;

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
     * Find AlterationActivityEvidences by alterations
     * @param alteration
     * @param evidenceType
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration, EvidenceType evidenceType);
    
    /**
     * 
     * @param alteration
     * @param evidenceType
     * @param tumorType
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration, EvidenceType evidenceType, TumorType tumorType);
    
    /**
     * Find AlterationActivityEvidences by Entrez Gene ID
     * @param gene
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene);
    
    /**
     * 
     * @param gene
     * @param evidenceType
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene, EvidenceType evidenceType);
}
