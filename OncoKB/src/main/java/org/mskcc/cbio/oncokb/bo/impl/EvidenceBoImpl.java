/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.mskcc.cbio.oncokb.bo.*;
import java.util.List;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 *
 * @author jgao
 */
public class EvidenceBoImpl  extends GenericBoImpl<Evidence, EvidenceDao> implements EvidenceBo {
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations) {
        List<Evidence> evidences = new ArrayList<Evidence>();
        for (Alteration alteration : alterations) {
            evidences.addAll(getDao().findEvidencesByAlteration(alteration));
        }
        return evidences;
    }
    
    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes) {
        List<Evidence> evidences = new ArrayList<Evidence>();
        for (Gene gene : genes) {
            evidences.addAll(getDao().findEvidencesByGene(gene));
        }
        return evidences;
    }
}
