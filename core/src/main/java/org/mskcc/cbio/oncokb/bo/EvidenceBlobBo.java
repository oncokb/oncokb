/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
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
public interface EvidenceBlobBo extends GenericBo<EvidenceBlob> {
    /**
     * Find Evidences by alteration ID
     * @param alterations
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Collection<Alteration> alterations);
    
    /**
     * Find Evidences by alteration ID and evidence type
     * @param alterations
     * @param evidenceType
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Collection<Alteration> alterations, EvidenceType evidenceType);
    
    /**
     * 
     * @param alterations
     * @param evidenceType
     * @param tumorType
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Collection<Alteration> alterations, EvidenceType evidenceType, TumorType tumorType);
    
    /**
     * Find Evidences by Entrez Gene ID
     * @param genes
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
