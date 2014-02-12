/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;

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
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterationIds);
    
    /**
     * Find Evidences by Entrez Gene ID
     * @param genes
     * @return 
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes);
}
