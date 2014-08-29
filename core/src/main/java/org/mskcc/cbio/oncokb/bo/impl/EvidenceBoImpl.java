/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Alteration alteration : alterations) {
            set.addAll(getDao().findEvidencesByAlteration(alteration));
        }
        return new ArrayList<Evidence>(set);
    }
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, EvidenceType evidenceType) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Alteration alteration : alterations) {
            set.addAll(getDao().findEvidencesByAlteration(alteration, evidenceType));
        }
        return new ArrayList<Evidence>(set);
    }
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, EvidenceType evidenceType, TumorType tumorType) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Alteration alteration : alterations) {
            set.addAll(getDao().findEvidencesByAlteration(alteration, evidenceType, tumorType));
        }
        return new ArrayList<Evidence>(set);
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
