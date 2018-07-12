/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;

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

    private Alteration findAlteration(String alteration, Set<Alteration> fullAlterations) {
        if (alteration == null) {
            return null;
        }
        // Implement the data access logic
        for (Alteration alt : fullAlterations) {
            if (alt.getAlteration() != null && alt.getAlteration().equalsIgnoreCase(alteration)) {
                return alt;
            }
        }
        for (Alteration alt : fullAlterations) {
            if (alt.getAlteration() != null && alt.getName().equalsIgnoreCase(alteration)) {
                return alt;
            }
        }
        return null;
    }

    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, String alteration) {
        if (CacheUtils.isEnabled()) {
            return findAlteration(alteration, CacheUtils.getAlterations(gene.getEntrezGeneId()));
        } else {
            return getDao().findAlteration(gene, alterationType, alteration);
        }
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPosition(Gene gene, VariantConsequence consequence, int start, int end, Collection<Alteration> alterations) {
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
                Collection<Alteration> queryResult;
                if (CacheUtils.isEnabled()) {
                    queryResult = CacheUtils.findMutationsByConsequenceAndPosition(gene, consequence, start, end);
                } else {
                    queryResult = getDao().findMutationsByConsequenceAndPosition(gene, consequence, start, end);
                }
                if (queryResult != null) {
                    result.addAll(queryResult);
                }
            }
        }

        return new ArrayList<>(result);
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPositionOnSamePosition(Gene gene, VariantConsequence consequence, int start, int end, Collection<Alteration> alterations) {
        Set<Alteration> result = new HashSet<>();

        if (alterations != null && alterations.size() > 0) {
            for (Alteration alteration : alterations) {
                if (alteration.getGene().equals(gene) && alteration.getConsequence() != null
                    && alteration.getConsequence().equals(consequence)
                    && alteration.getProteinStart() != null
                    && alteration.getProteinEnd() != null
                    && alteration.getProteinStart().equals(alteration.getProteinEnd())
                    && alteration.getProteinStart() >= start
                    && alteration.getProteinStart() <= end) {
                    result.add(alteration);
                }
            }
        } else {
            Collection<Alteration> queryResult;
            if (CacheUtils.isEnabled()) {
                queryResult = CacheUtils.findMutationsByConsequenceAndPositionOnSamePosition(gene, consequence, start, end);
            } else {
                queryResult = getDao().findMutationsByConsequenceAndPositionOnSamePosition(gene, consequence, start, end);
            }
            if (queryResult != null) {
                result.addAll(queryResult);
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Find all relevant alterations. The order is important. The list should be generated based on priority.
     *
     * @param alteration
     * @return
     */
    @Override
    public LinkedHashSet<Alteration> findRelevantAlterations(Alteration alteration, boolean includeAlternativeAllele) {
        return findRelevantAlterationsSub(alteration, AlterationUtils.getAllAlterations(alteration.getGene()), includeAlternativeAllele);
    }

    /**
     * Find all relevant alterations. The order is important. The list should be generated based on priority.
     *
     * @param alteration
     * @param fullAlterations
     * @return
     */
    @Override
    public LinkedHashSet<Alteration> findRelevantAlterations(Alteration alteration, Set<Alteration> fullAlterations, boolean includeAlternativeAllele) {
        if(fullAlterations == null) {
            return new LinkedHashSet<>();
        }
        return findRelevantAlterationsSub(alteration, fullAlterations, includeAlternativeAllele);
    }

    @Override
    public void deleteMutationsWithoutEvidenceAssociatedByGene(Gene gene) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        Set<Alteration> relatedAlts = new HashSet<>();
        List<Alteration> noMappingAlts = new ArrayList<>();
        for(Evidence evidence : evidenceBo.findEvidencesByGeneFromDB(Collections.singleton(gene))) {
            relatedAlts.addAll(evidence.getAlterations());
        }
        for(Alteration alteration : findAlterationsByGene(Collections.singleton(gene))){
            if(!relatedAlts.contains(alteration)) {
                noMappingAlts.add(alteration);
            }
        }
        deleteAll(noMappingAlts);
    }

    /**
     * Find all relevant alterations. The order is important. The list should be generated based on priority.
     *
     * @param alteration
     * @param fullAlterations
     * @return
     */
    private LinkedHashSet<Alteration> findRelevantAlterationsSub(Alteration alteration, Set<Alteration> fullAlterations, boolean includeAlternativeAllele) {
        LinkedHashSet<Alteration> alterations = new LinkedHashSet<>();
        Boolean addTruncatingMutations = false;
        Boolean addDeletion = false;

        AlterationType daoAlterationType = alteration.getAlterationType();

        // DAO only support MUTATION and intragenic fusion
        if (daoAlterationType != null && !(org.apache.commons.lang3.StringUtils.containsIgnoreCase(alteration.getAlteration(), "intragenic") && daoAlterationType.equals(AlterationType.FUSION))) {
            daoAlterationType = AlterationType.MUTATION;
        }

        // Alteration should always has consequence attached.
        if (alteration.getConsequence() == null) {
            AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());
        }

        if (alteration.getConsequence().getTerm().equals("synonymous_variant")) {
            return alterations;
        }

        // Find exact match
        Alteration matchedAlt = findAlteration(alteration.getAlteration(), fullAlterations);
        if (matchedAlt != null) {
            alterations.add(matchedAlt);
        }

        if (addEGFRCTD(alteration)) {
            Alteration alt = findAlteration("CTD", fullAlterations);
            if (alt != null) {
                alterations.add(alt);
            }
        }

        if (alteration.getGene().getHugoSymbol().equals("EGFR") && alteration.getAlteration().equals("CTD")) {
            Alteration alt = findAlteration("CTD", fullAlterations);
            if (alt != null && !alterations.contains(alt)) {
                alterations.add(alt);
            }
        }

        // Find fusion variant
        //If alteration contains 'fusion' or alterationType is fusion
        if ((alteration.getAlteration() != null && alteration.getAlteration().toLowerCase().contains("fusion"))
            || (alteration.getAlterationType() != null && alteration.getAlterationType().equals(AlterationType.FUSION))
            || (alteration.getAlterationType() != null && alteration.getAlterationType().equals(AlterationType.STRUCTURAL_VARIANT)
            && alteration.getConsequence() != null && alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm("fusion")))) {
            // TODO: match fusion partner

            //the alteration 'fusions' should be injected into alteration list
            Alteration alt = findAlteration("fusions", fullAlterations);
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
        if (includeAlternativeAllele && alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant"))) {
            alterations.addAll(AlterationUtils.getAlleleAlterations(alteration, fullAlterations));
            List<Alteration> includeRangeAlts = findMutationsByConsequenceAndPosition(alteration.getGene(), alteration.getConsequence(), alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations);

            // For missense mutation, also include positioned
            VariantConsequence variantConsequence = new VariantConsequence();
            variantConsequence.setTerm("NA");
            List<Alteration> positionVariants = findMutationsByConsequenceAndPositionOnSamePosition(alteration.getGene(), variantConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations);

            includeRangeAlts.addAll(positionVariants);

            for (Alteration alt : includeRangeAlts) {
                if (!alterations.contains(alt)) {
                    alterations.add(alt);
                }
            }
        } else {
            alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), alteration.getConsequence(), alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));
        }


        if (alteration.getConsequence().getIsGenerallyTruncating()) {
            addTruncatingMutations = true;
        }

        // Match all variants with `any` as consequence. Currently, only format start_end mut is supported.
        VariantConsequence anyConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm("any");
        alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), anyConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));

        // Remove all range mutations as relevant for truncating mutations in oncogenes
        oncogeneTruncMuts(alteration, alterations);

        // Match Truncating Mutations section to Deletion if no Deletion section specifically curated
        if (alteration.getAlteration().toLowerCase().matches("deletion")) {
            addDeletion = true;
            addTruncatingMutations = true;
        }

        if (addDeletion) {
            Alteration deletion = findAlteration( "Deletion", fullAlterations);
            if (deletion != null) {
                alterations.add(deletion);

                // If there is Deletion annotated already, do not associate Truncating Mutations
                addTruncatingMutations = false;
            }
        }

        if (addTruncatingMutations) {
            VariantConsequence truncatingVariantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm("feature_truncation");
            alterations.addAll(findMutationsByConsequenceAndPosition(alteration.getGene(), truncatingVariantConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations));
        }

        if (addOncogenicMutations(alteration, alterations)) {
            Alteration oncogenicMutations = findAlteration( "oncogenic mutations", fullAlterations);
            if (oncogenicMutations != null) {
                alterations.add(oncogenicMutations);
            }
        }

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
            Alteration alt = findAlteration( effect + " mutations", fullAlterations);
            if (alt != null) {
                alterations.add(alt);
            }
        }

        // Remove exon 17 for KIT D816
        if (isKIT816(alteration)) {
            Iterator<Alteration> iter = alterations.iterator();
            while (iter.hasNext()) {
                Alteration alt = iter.next();
                if (alt.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm("any"))
                    && alt.getProteinStart() != null && alt.getProteinStart().equals(788)
                    && alt.getProteinEnd() != null && alt.getProteinEnd().equals(828)) {
                    iter.remove();
                }
            }
        }
        return alterations;
    }

    @Override
    public void save(Alteration alteration) {
        super.save(alteration);
        if (CacheUtils.isEnabled()) {
            CacheUtils.forceUpdateGeneAlterations(alteration.getGene().getEntrezGeneId());
        }
    }


    private boolean addEGFRCTD(Alteration exactAlt) {
        boolean add = false;
        if (exactAlt != null && exactAlt.getGene() != null
            && exactAlt.getGene().equals(GeneUtils.getGeneByHugoSymbol("EGFR"))
            && !StringUtils.isNullOrEmpty(exactAlt.getAlteration())
            && exactAlt.getAlteration().trim().matches("^vIV(a|b|c)?$")) {
            add = true;
        }
        return add;
    }

    private boolean addOncogenicMutations(Alteration exactAlt, Set<Alteration> relevantAlts) {
        boolean add = false;
        if (!isKitSpecialVariants(exactAlt)) {
            if (!exactAlt.getAlteration().trim().equalsIgnoreCase("amplification")) {
                Set<Oncogenicity> oncogenicities = AlterationUtils.getCuratedOncogenicity(exactAlt);
                boolean has = AlterationUtils.hasImportantCuratedOncogenicity(oncogenicities);
                if (has) {
                    Boolean isOncogenic = AlterationUtils.hasOncogenic(oncogenicities);

                    if (isOncogenic != null && isOncogenic) {
                        add = true;
                    }
                } else {
                    // When we look at the oncogenicity, the VUS relevant variants should be excluded.
                    for (Alteration alt : AlterationUtils.excludeVUS(new ArrayList<>(relevantAlts))) {
                        Boolean isOncogenic = AlterationUtils.isOncogenicAlteration(alt);

                        if (isOncogenic != null && isOncogenic && !isKitSpecialVariants(alt)) {
                            add = true;
                            break;
                        }
                    }
                }
            }
        }
        return add;
    }

    private boolean isKitSpecialVariants(Alteration alteration) {
        boolean isSpecial = false;
        if (alteration != null && alteration.getGene().getEntrezGeneId() == 3815) {
            String[] speicalVariants = {"V654A", "T670I"};
            VariantConsequence consequence = VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant");
            for (int i = 0; i < speicalVariants.length; i++) {
                if (alteration.getGene() != null && alteration.getGene().getEntrezGeneId() == 3815
                    && alteration.getAlteration() != null && alteration.getAlteration().equals(speicalVariants[i])
                    && alteration.getConsequence().equals(consequence)) {
                    isSpecial = true;
                    break;
                }
            }
            if (!isSpecial) {
                isSpecial = isInExon17(alteration);
            }
        }
        return isSpecial;
    }

    private boolean isVariantByLocation(Alteration alteration, int entrezGeneId, int proteinStart, int proteinEnd, VariantConsequence variantConsequence) {
        if (alteration.getGene() != null && alteration.getGene().getEntrezGeneId() == entrezGeneId
            && alteration.getProteinStart() != null && alteration.getProteinStart().equals(proteinStart)
            && alteration.getProteinEnd() != null && alteration.getProteinEnd().equals(proteinEnd)
            && alteration.getConsequence().equals(variantConsequence)) {
            return true;
        }
        return false;
    }

    private boolean isKIT816(Alteration alteration) {
        return isVariantByLocation(alteration, 3815, 816, 816, VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant"));
    }

    private boolean isInExon17(Alteration alteration) {
        if (alteration.getProteinStart() != null && alteration.getProteinStart() <= 828
            && alteration.getProteinEnd() != null && alteration.getProteinEnd() >= 788) {
            return true;
        }
        return false;
    }

    private void oncogeneTruncMuts(Alteration alteration, LinkedHashSet<Alteration> relevantAlts) {
        if (alteration.getGene().getOncogene() != null && alteration.getGene().getTSG() != null && alteration.getGene().getOncogene() && !alteration.getGene().getTSG() && alteration.getConsequence().getIsGenerallyTruncating()) {
            Iterator<Alteration> iterator = relevantAlts.iterator();
            while (iterator.hasNext()) {
                Alteration relevantAlt = iterator.next();
                if (!relevantAlt.getConsequence().getIsGenerallyTruncating() && !relevantAlt.getProteinEnd().equals(relevantAlt.getProteinStart()) && !relevantAlt.getProteinStart().equals(-1)) {
                    iterator.remove();
                }
            }
        }
    }
}
