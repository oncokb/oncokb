/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.EvidenceBlob;
import org.mskcc.cbio.oncokb.model.Gene;

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
    List<EvidenceBlob> findEvidenceBlobsByAlteration(Collection<Alteration> alterationIds);
    
    /**
     * Find Evidences by Entrez Gene ID
     * @param genes
     * @return 
     */
    List<EvidenceBlob> findEvidenceBlobsByGene(Collection<Gene> genes);
}
