/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.EvidenceBlobDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.EvidenceBlob;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.TumorType;

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
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration, EvidenceType evidenceType) {
        return findByNamedQuery("findEvidenceBlobsByAlterationAndEvidenceType", alteration.getAlterationId(), evidenceType);
    }
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration, EvidenceType evidenceType, TumorType tumorType) {
        return findByNamedQuery("findEvidenceBlobsByAlterationAndEvidenceTypeAndTumorType", alteration.getAlterationId(), evidenceType, tumorType);
    }


    @Override
    public List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene) {
        return findByNamedQuery("findEvidenceBlobsByGene", gene);
    }

    @Override
    public List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene, EvidenceType evidenceType) {
        return findByNamedQuery("findEvidenceBlobsByGeneAndEvidenceType", gene, evidenceType);
    }
}
