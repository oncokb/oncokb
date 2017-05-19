/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.VariantConsequenceUtils;

import java.util.*;

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
        Set<Alteration> result = new HashSet<>();

        // Don't search for NA cases
        if (gene != null && consequence != null && !consequence.getTerm().equals("NA")) {
            if (alterations != null && alterations.size() > 0) {
                for (Alteration alteration : alterations) {
                    if (alteration.getGene().equals(gene) && alteration.getConsequence() != null && alteration.getConsequence().equals(consequence)) {

                        //For missense variant, as long as they are overlapped to each, return the alteration
                        if (consequence.equals(VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant"))) {
                            if (end >= alteration.getProteinStart()
                                && start <= alteration.getProteinEnd()) {
                                result.add(alteration);
                            }
                        } else if (alteration.getProteinStart() <= start && alteration.getProteinEnd() >= end) {
                            result.add(alteration);
                        }
                    }
                }
            } else {
                List<Alteration> queryResult = getDao().findMutationsByConsequenceAndPosition(gene, consequence, start, end);
                if (queryResult != null) {
                    result.addAll(queryResult);
                }
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Find all relevant alterations. The order is important. The list should be generated based on priority.
     *
     * @param alteration
     * @param fullAlterations
     * @return
     */
    @Override
    public LinkedHashSet<Alteration> findRelevantAlterations(Alteration alteration, List<Alteration> fullAlterations) {
        LinkedHashSet<Alteration> alterations = new LinkedHashSet<>();
        Boolean addTruncatingMutations = false;
        Boolean addDeletion = false;
        Boolean addOncogenicMutations = false;

        // Alteration should always has consequence attached.
        if (alteration.getConsequence() == null) {
            AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());
        }

        if (alteration.getConsequence().getTerm().equals("synonymous_variant")) {
            return alterations;
        }

        // Find exact match
        Alteration matchedAlt = findAlteration(alteration.getGene(), alteration.getAlterationType(), alteration.getAlteration());
        if (matchedAlt != null) {
            alterations.add(matchedAlt);
        }

        // Find fusion variant
        //If alteration contains 'fusion' or alterationType is fusion
        if ((alteration.getAlteration() != null && alteration.getAlteration().toLowerCase().contains("fusion"))
            || (alteration.getAlterationType() != null && alteration.getAlterationType().equals(AlterationType.FUSION))) {
            // TODO: match fusion partner

            //the alteration 'fusions' should be injected into alteration list
            Alteration alt = findAlteration(alteration.getGene(), alteration.getAlterationType(), "fusions");
            if (alt != null) {
                alterations.add(alt);
            } else {
                // If no fusions curated, check the Truncating Mutations.
                addTruncatingMutations = true;
            }
        }

        // Map Truncating Mutations to Translocation and Inversion
        // These two are all structural variants, need a better way to model them.
//            if (alteration.getAlteration().toLowerCase().equals("translocation")
//                || alteration.getAlteration().toLowerCase().equals("inversion")) {
//                // If no fusions curated, check the Truncating Mutations.
//                if (truncatingMutation != null && !alterations.contains(truncatingMutation)) {
//                    alterations.add(truncatingMutation);
//                }
//            }

        // Map intragenic to Deletion or Truncating Mutation
        // If no Deletion curated, attach Truncating Mutations
//            if (alteration.getAlteration().toLowerCase().contains("intragenic") ||
//                alteration.getAlteration().toLowerCase().equals("deletion")) {
//                if (deletion != null) {
//                    if (!alterations.contains(deletion)) {
//                        alterations.add(deletion);
//                    }
//                } else if (truncatingMutation != null && !alterations.contains(truncatingMutation)) {
//                    alterations.add(truncatingMutation);
//                }
//            }

        //Find Alternative Alleles for missense variant
        if (alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant"))) {
            alterations.addAll(AlterationUtils.getAlleleAlterations(alteration));
        } else {
            alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), alteration.getConsequence(), alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));
        }


        if (alteration.getConsequence().getIsGenerallyTruncating()) {
            VariantConsequence truncatingVariantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm("feature_truncation");
            alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), truncatingVariantConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));
        }

        // Match all variants with `any` as consequence. Currently, only format start_endmut is supported.
        VariantConsequence anyConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm("any");
        alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), anyConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));


        // Looking for general biological effect variants. Gain-of-function mutations, Loss-of-function mutations etc.
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> mutationEffectEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT));
        Set<String> effects = new HashSet<>();

        for (Evidence evidence : mutationEffectEvs) {
            String effect = evidence.getKnownEffect();
            if (effect != null) {
                effect = effect.toLowerCase();
                effect = effect.replaceAll("likely", "");
                effect = effect.replaceAll(" ", "");

                effects.add(effect);
            }
        }

        for (String effect : effects) {
            Alteration alt = findAlteration(alteration.getGene(), alteration.getAlterationType(), effect + " mutations");
            if (alt != null) {
                alterations.add(alt);
            }
        }

        // Looking for oncogenic mutations
        if (!alteration.getAlteration().trim().equalsIgnoreCase("amplification")) {
            for (Alteration alt : alterations) {
                if (AlterationUtils.isOncogenicAlteration(alt)) {
                    addOncogenicMutations = true;
                    break;
                }
            }
        }

        if (addTruncatingMutations) {
            Alteration truncatingMutations = findAlteration(alteration.getGene(), alteration.getAlterationType(), "Truncating Mutations");
            if (truncatingMutations != null) {
                alterations.add(truncatingMutations);
            }
        }
        if (addDeletion) {
            Alteration deletion = findAlteration(alteration.getGene(), alteration.getAlterationType(), "Deletion");
            if (deletion != null) {
                alterations.add(deletion);
            }
        }
        if (addOncogenicMutations) {
            Alteration oncogenicMutations = findAlteration(alteration.getGene(), alteration.getAlterationType(), "oncogenic mutations");
            if (oncogenicMutations != null) {
                alterations.add(oncogenicMutations);
            }
        }

        // Match Truncating Mutations section to Deletion if no Deletion section specifically curated
        Alteration truncAlt = AlterationUtils.findAlteration(alteration.getGene(), "Truncating Mutations");
        if (truncAlt != null && !alterations.contains(truncAlt)) {
            Alteration deletion = AlterationUtils.findAlteration(alteration.getGene(), "Deletion");
            if (deletion == null && alteration.getAlteration().toLowerCase().matches("deletion")) {
                alterations.add(truncAlt);
            }
        }
        return alterations;
    }
}
