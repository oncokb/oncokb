/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public class EvidenceBoImpl  extends GenericBoImpl<Evidence, EvidenceDao> implements EvidenceBo {
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations) {
        List<Evidence> list = new ArrayList<Evidence>();
        for (Alteration alteration : alterations) {
            list.addAll(getDao().findEvidencesByAlteration(alteration));
        }
        return list;
    }
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, EvidenceType evidenceType) {
        List<Evidence> list = new ArrayList<Evidence>();
        for (Alteration alteration : alterations) {
            list.addAll(getDao().findEvidencesByAlteration(alteration, evidenceType));
        }
        return list;
    }
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, EvidenceType evidenceType, TumorType tumorType) {
        List<Evidence> list = new ArrayList<Evidence>();
        for (Alteration alteration : alterations) {
            list.addAll(getDao().findEvidencesByAlteration(alteration, evidenceType, tumorType));
        }
        return list;
    }
    
    @Override
    public List<Evidence> findEvidencesByGene(Gene gene) {
        return getDao().findEvidencesByGene(gene);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Gene gene, EvidenceType evidenceType) {
        return getDao().findEvidencesByGene(gene, evidenceType);
    }
}
