/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
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
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Collection<Alteration> alterations) {
        List<EvidenceBlob> list = new ArrayList<EvidenceBlob>();
        for (Alteration alteration : alterations) {
            list.addAll(getDao().findEvidenceBlobsByAlteration(alteration));
        }
        return list;
    }
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Collection<Alteration> alterations, EvidenceType evidenceType) {
        List<EvidenceBlob> list = new ArrayList<EvidenceBlob>();
        for (Alteration alteration : alterations) {
            list.addAll(getDao().findEvidenceBlobsByAlteration(alteration, evidenceType));
        }
        return list;
    }
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Collection<Alteration> alterations, EvidenceType evidenceType, TumorType tumorType) {
        List<EvidenceBlob> list = new ArrayList<EvidenceBlob>();
        for (Alteration alteration : alterations) {
            list.addAll(getDao().findEvidenceBlobsByAlteration(alteration, evidenceType, tumorType));
        }
        return list;
    }
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByGene(Gene gene) {
        return getDao().findEvidenceBlobsByGene(gene);
    }
}
