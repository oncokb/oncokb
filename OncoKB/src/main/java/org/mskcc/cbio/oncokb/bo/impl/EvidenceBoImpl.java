/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.*;
import java.util.List;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.Evidence;

/**
 *
 * @author jgao
 */
public class EvidenceBoImpl  extends GenericBoImpl<Evidence, EvidenceDao> implements EvidenceBo {
    /**
     * Find Evidences by alteration ID
     * @param alterationId
     * @return 
     */
    public List<Evidence> findEvidencesByAlteration(int alterationId) {
        return getDao().findEvidencesByAlteration(alterationId);
    }
    
    /**
     * Find Evidences by Entrez Gene ID
     * @param entrezGeneId
     * @return 
     */
    public List<Evidence> findEvidencesByGene(int entrezGeneId) {
        return getDao().findEvidencesByGene(entrezGeneId);
    }
}
