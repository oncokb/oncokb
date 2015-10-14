/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.VariantConsequence;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

/**
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
    public List<Alteration> findMutationsByConsequenceAndPosition(Gene gene, VariantConsequence consequence, int start, int end, List<Alteration> alterations) {
        if (alterations != null && alterations.size() > 0) {
            List<Alteration> filter = new ArrayList<>();

            for (Alteration alteration : alterations) {
                if (gene != null && consequence != null
                        && alteration.getGene().equals(gene) && alteration.getConsequence() != null && alteration.getConsequence().equals(consequence) && alteration.getProteinStart() <= start && alteration.getProteinEnd() >= end) {
                    filter.add(alteration);
                }
            }
            return filter;
        } else {
            return getDao().findMutationsByConsequenceAndPosition(gene, consequence, start, end);
        }
    }

    @Override
    public List<Alteration> findRelevantAlterations(Alteration alteration, List<Alteration> fullAlterations) {
        List<Alteration> alterations = new ArrayList<Alteration>();
        Alteration matchedAlt = findAlteration(alteration.getGene(), alteration.getAlterationType(), alteration.getAlteration());
        if (matchedAlt != null) {
            alterations.add(matchedAlt);
        }
        if (alteration.getConsequence() != null) {
            // we need to develop better way to match mutation
            if (alteration.getProteinStart() != null) {
                List<Alteration> alts = findMutationsByConsequenceAndPosition(alteration.getGene(), alteration.getConsequence(), alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations);
                if (!alteration.getConsequence().getTerm().equals("missense_variant")) {
                    alterations.addAll(alts);
                } else {
                    for (Alteration alt : alts) {
                        if (!alt.getAlteration().matches("[A-Z][0-9]+[A-Z]")) {
                            alterations.add(alt);
                        }
                    }
                }
            }

            if (alteration.getConsequence().getIsGenerallyTruncating()) {
                VariantConsequence truncatingVariantConsequence = ApplicationContextSingleton.getVariantConsequenceBo().findVariantConsequenceByTerm("feature_truncation");
                alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), truncatingVariantConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));
            }
        }

        VariantConsequence anyConsequence = ApplicationContextSingleton.getVariantConsequenceBo().findVariantConsequenceByTerm("any");
        alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), anyConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));

        //TODO: add activating or inactivating alterations
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> mutationEffectEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT));
        boolean activating = false, inactivating = false;
        for (Evidence evidence : mutationEffectEvs) {
            String effect = evidence.getKnownEffect();
            if (effect != null) {
                effect = effect.toLowerCase();
                if (effect.contains("inactivating")) {
                    inactivating = true;
                } else if (effect.contains("activating")) {
                    activating = true;
                }
            }
        }

        //If alteration contains 'fusion', the alteration 'fusions' should be injected into alteration list
        if (alteration.getAlteration().toLowerCase().contains("fusion")) {
            Alteration alt = findAlteration(alteration.getGene(), alteration.getAlterationType(), "fusions");
            if (alt != null) {
                alterations.add(alt);
            }
        }

        if (inactivating) {
            Alteration alt = findAlteration(alteration.getGene(), alteration.getAlterationType(), "inactivating mutations");
            if (alt != null) {
                alterations.add(alt);
            }
        }

        if (activating) {
            Alteration alt = findAlteration(alteration.getGene(), alteration.getAlterationType(), "activating mutations");
            if (alt != null) {
                alterations.add(alt);
            }
        }

        return alterations;
    }
}
