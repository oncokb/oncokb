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
import org.mskcc.cbio.oncokb.model.Gene;

/**
 *
 * @author jgao
 */
public class EvidenceBlobBoImpl  extends GenericBoImpl<EvidenceBlob, EvidenceBlobDao> implements EvidenceBlobBo {
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByAlteration(Collection<Alteration> alterations) {
        List<EvidenceBlob> evidences = new ArrayList<EvidenceBlob>();
        for (Alteration alteration : alterations) {
            evidences.addAll(getDao().findEvidenceBlobsByAlteration(alteration));
        }
        return evidences;
    }
    
    @Override
    public List<EvidenceBlob> findEvidenceBlobsByGene(Collection<Gene> genes) {
        List<EvidenceBlob> evidences = new ArrayList<EvidenceBlob>();
        for (Gene gene : genes) {
            evidences.addAll(getDao().findEvidenceBlobsByGene(gene));
        }
        return evidences;
    }
}
