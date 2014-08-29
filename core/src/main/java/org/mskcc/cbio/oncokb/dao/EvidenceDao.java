/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.TumorType;

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
     * Find AlterationActivityEvidences by alterations
     * @param alteration
     * @param evidenceType
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Alteration alteration, EvidenceType evidenceType);
    
    /**
     * 
     * @param alteration
     * @param evidenceType
     * @param tumorType
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Alteration alteration, EvidenceType evidenceType, TumorType tumorType);
    
    /**
     * Find AlterationActivityEvidences by Entrez Gene ID
     * @param gene
     * @return 
     */
    List<Evidence> findEvidencesByGene(Gene gene);
    
    /**
     * 
     * @param gene
     * @param evidenceType
     * @return 
     */
    List<Evidence> findEvidencesByGene(Gene gene, EvidenceType evidenceType);
}
