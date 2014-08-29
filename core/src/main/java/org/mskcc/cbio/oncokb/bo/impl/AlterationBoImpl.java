/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.VariantConsequenceBo;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.VariantConsequence;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

/**
 *
 * @author jgao
 */
public class AlterationBoImpl extends GenericBoImpl<Alteration, AlterationDao> implements AlterationBo {

    @Override
    public List<Alteration> findAlterationsByGene(Collection<Gene> genes) {
        List<Alteration> alterations = new ArrayList<Alteration>();
        for (Gene gene : genes) {
            alterations.addAll(getDao().findAlterationsByGene(gene));
        }
        return alterations;
    }
    
    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, String alteration) {
        return getDao().findAlteration(gene, alterationType, alteration);
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPosition(Gene gene, VariantConsequence consequence, int start, int end) {
        return getDao().findMutationsByConsequenceAndPosition(gene, consequence, start, end);
    }

    @Override
    public List<Alteration> findRelevantAlterations(Alteration alteration) {
        List<Alteration> alterations = new ArrayList<Alteration>();
        Alteration matchedAlt = findAlteration(alteration.getGene(), alteration.getAlterationType(), alteration.getAlteration());
        if (matchedAlt!=null) {
            alterations.add(matchedAlt);
        }
        if (alteration.getConsequence()!=null) {
            if (matchedAlt==null // only when there is no specific match
                    && alteration.getProteinStart()!=null) { // only whe 
                alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), alteration.getConsequence(), alteration.getProteinStart(), alteration.getProteinEnd()));
            }

            if (alteration.getConsequence().getIsGenerallyTruncating()) {
                VariantConsequence truncatingVariantConsequence = ApplicationContextSingleton.getVariantConsequenceBo().findVariantConsequenceByTerm("feature_truncation");
                alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), truncatingVariantConsequence, alteration.getProteinStart(), alteration.getProteinEnd()));
            }
        }
            
        //TODO: add activating or inactivating alterations
        
        return alterations;
    }
}
