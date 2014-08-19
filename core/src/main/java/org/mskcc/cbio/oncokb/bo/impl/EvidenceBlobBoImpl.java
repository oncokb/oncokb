/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.*;
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
public class EvidenceBlobBoImpl  extends GenericBoImpl<EvidenceBlob, EvidenceBlobDao> implements EvidenceBlobBo {
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration) {
        return getDao().findEvidenceBlobsByAlteration(alteration);
    }
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alterationId, EvidenceType evidenceType) {
        return getDao().findEvidenceBlobsByAlteration(alterationId, evidenceType);
    }
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Alteration alteration, EvidenceType evidenceType, TumorType tumorType) {
        return getDao().findEvidenceBlobsByAlteration(alteration, evidenceType, tumorType);
    }
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene) {
        return getDao().findEvidenceBlobsByGene(gene);
    }
}
