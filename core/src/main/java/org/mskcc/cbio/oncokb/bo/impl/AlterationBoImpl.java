/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.VariantConsequence;

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
    public List<Alteration> findRelevantAlterations(Gene gene, AlterationType alterationType, String alteration, VariantConsequence consequence, Integer start, Integer end) {
        List<Alteration> alterations = new ArrayList<Alteration>();
        Alteration matchedAlt = findAlteration(gene, alterationType, alteration);
        if (matchedAlt!=null) {
            alterations.add(matchedAlt);
        }
        
        if (end==null) {
            end = start;
        }
        if (consequence!=null) {
            if (start!=null) {
                alterations.addAll(findMutationsByConsequenceAndPosition(gene, consequence, start, end));
            }

            if (consequence.getIsGenerallyTruncating()) {
                Alteration truncatingMutation = findAlteration(gene, AlterationType.MUTATION, "truncating mutations");
                if (truncatingMutation!=null) {
                    if (truncatingMutation.getProteinStart()==null || (truncatingMutation.getProteinStart()<=start && truncatingMutation.getProteinStart()>=start)) {
                        alterations.add(truncatingMutation);
                    }
                }
            }
        }
            
        //TODO: add activating or inactivating alterations
        
        return alterations;
    }
}
