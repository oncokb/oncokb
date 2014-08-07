/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.EvidenceBlobDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.EvidenceBlob;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 *
 * @author jgao
 */
public class EvidenceBlobDaoImpl
            extends GenericDaoImpl<EvidenceBlob, Integer>
            implements EvidenceBlobDao {
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration) {
        return findByNamedQuery("findEvidenceBlobsByAlteration", alteration.getAlterationId());
    }

    @Override
    public List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene) {
        return findByNamedQuery("findEvidenceBlobsByGene", gene.getEntrezGeneId());
    }
    
}
