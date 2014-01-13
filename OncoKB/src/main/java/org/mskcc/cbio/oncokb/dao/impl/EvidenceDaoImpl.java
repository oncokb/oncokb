/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.Evidence;

/**
 *
 * @author jgao
 */
public class EvidenceDaoImpl
            extends GenericDaoImpl<Evidence, Integer>
            implements EvidenceDao {

    public List<Evidence> findEvidencesByAlterationId(int alterationId) {
        return findByNamedQuery("findEvidencesByAlterationId", alterationId);
    }

    public List<Evidence> findEvidencesByGene(int entrezGeneId) {
        return findByNamedQuery("findEvidencesByGene", entrezGeneId);
    }
    
}
