/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

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
     * @param alterationId
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alterationId);
    
    /**
     * Find Evidences by alteration ID and evidence type
     * @param alterationId
     * @param evidencetype
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alterationId, EvidenceType evidenceType);
    
    /**
     * 
     * @param alteration
     * @param evidenceType
     * @param tumorType
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration, EvidenceType evidenceType, TumorType tumorType);
    
    /**
     * Find Evidences by Entrez Gene ID
     * @param genes
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene);
}
