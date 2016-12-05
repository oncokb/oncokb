/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jgao
 */
public final class AlterationUtils {
    private static List<String> oncogenicList = Arrays.asList(new String[]{"", "-1", "0", "2", "1"});
    private static AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
    private final static String[] generalAlts = {"activating mutations", "activating mutation", "inactivating mutations", "inactivating mutation", "all mutations", "all mutation", "wildtype", "wildtypes"};
    private final static String[] singularGeneralAlt = {"activating mutation", "inactivating mutation", "all mutations"};
    private final static Set<String> generalAlterations = new HashSet<>(Arrays.asList(generalAlts));
    private final static Set<String> singularGeneralAlterations = new HashSet<>(Arrays.asList(singularGeneralAlt));
    private final static String fusionRegex = "((\\w*)-(\\w*))\\s+(?i)fusion";

    private AlterationUtils() {
        throw new AssertionError();
    }

    public static void annotateAlteration(Alteration alteration, String proteinChange) {
        String consequence = "NA";
        String ref = null;
        String var = null;
        Integer start = -1;
        Integer end = 100000;

        if (alteration == null) {
            return;
        }

        if (proteinChange == null) {
            proteinChange = "";
        }

        if (proteinChange.startsWith("p.")) {
            proteinChange = proteinChange.substring(2);
        }

        if (proteinChange.indexOf("[") != -1) {
            proteinChange = proteinChange.substring(0, proteinChange.indexOf("["));
        }

        proteinChange = proteinChange.trim();

        Pattern p = Pattern.compile("([A-Z\\*])([0-9]+)([A-Z\\*]?)");
        Matcher m = p.matcher(proteinChange);
        if (m.matches()) {
            ref = m.group(1);
            start = Integer.valueOf(m.group(2));
            end = start;
            var = m.group(3);

            if (ref.equals(var)) {
                consequence = "synonymous_variant";
            } else if (ref.equals("*")) {
                consequence = "stop_lost";
            } else if (var.equals("*")) {
                consequence = "stop_gained";
            } else if (start == 1) {
                consequence = "initiator_codon_variant";
            } else {
                consequence = "missense_variant";
            }
        } else {
            p = Pattern.compile("[A-Z]?([0-9]+)(_[A-Z]?([0-9]+))?delins([A-Z]+)");
            m = p.matcher(proteinChange);
            if (m.matches()) {
                start = Integer.valueOf(m.group(1));
                if (m.group(3) != null) {
                    end = Integer.valueOf(m.group(3));
                } else {
                    end = start;
                }
                Integer deletion = end - start + 1;
                Integer insertion = m.group(4).length();

                if (insertion - deletion > 0) {
                    consequence = "inframe_insertion";
                } else if (insertion - deletion == 0) {
                    consequence = "missense_variant";
                } else {
                    consequence = "inframe_deletion";
                }
            } else {
                p = Pattern.compile("[A-Z]?([0-9]+)_[A-Z]?([0-9]+)(.+)");
                m = p.matcher(proteinChange);
                if (m.matches()) {
                    start = Integer.valueOf(m.group(1));
                    end = Integer.valueOf(m.group(2));
                    String v = m.group(3);
                    switch (v) {
                        case "mis":
                            consequence = "missense_variant";
                            break;
                        case "ins":
                            consequence = "inframe_insertion";
                            break;
                        case "del":
                            consequence = "inframe_deletion";
                            break;
                        case "fs":
                            consequence = "frameshift_variant";
                            break;
                        case "trunc":
                            consequence = "feature_truncation";
                            break;
                        case "mut":
                            consequence = "any";
                    }
                } else {
                    p = Pattern.compile("([A-Z\\*])([0-9]+)[A-Z]?fs.*");
                    m = p.matcher(proteinChange);
                    if (m.matches()) {
                        ref = m.group(1);
                        start = Integer.valueOf(m.group(2));
                        end = start;

                        consequence = "frameshift_variant";
                    } else {
                        p = Pattern.compile("([A-Z]+)?([0-9]+)((ins)|(del))");
                        m = p.matcher(proteinChange);
                        if (m.matches()) {
                            ref = m.group(1);
                            start = Integer.valueOf(m.group(2));
                            end = start;
                            String v = m.group(3);
                            switch (v) {
                                case "ins":
                                    consequence = "inframe_insertion";
                                    break;
                                case "del":
                                    consequence = "inframe_deletion";
                                    break;
                            }
                        } else {
                            p = Pattern.compile("_splice");
                            m = p.matcher(proteinChange);
                            if (m.find()) {
                                consequence = "splice_region_variant";
                            }
                        }
                    }
                }
            }
        }

        // truncating
        if (proteinChange.toLowerCase().matches("truncating mutations?")) {
            consequence = "feature_truncation";
        }

        VariantConsequence variantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm(consequence);

        if (variantConsequence == null) {
            variantConsequence = new VariantConsequence(consequence, null, false);
        }

        if (alteration.getRefResidues() == null && ref != null && !ref.isEmpty()) {
            alteration.setRefResidues(ref);
        }

        if (alteration.getVariantResidues() == null && var != null && !var.isEmpty()) {
            alteration.setVariantResidues(var);
        }

        if (alteration.getProteinStart() == null && start != null) {
            alteration.setProteinStart(start);
        }

        if (alteration.getProteinEnd() == null && end != null) {
            alteration.setProteinEnd(end);
        }

        if (alteration.getConsequence() == null && variantConsequence != null) {
            alteration.setConsequence(variantConsequence);
        }

        if (alteration.getAlteration() != null) {
            if (isSingularGeneralAlteration(alteration.getAlteration())) {
                alteration.setAlteration(alteration.getAlteration() + "s");
            }
        }
    }

    public static Boolean isSingularGeneralAlteration(String alteration) {
        if (alteration == null) {
            return false;
        }
        for (String name : singularGeneralAlterations) {
            if (name.equalsIgnoreCase(alteration)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean isFusion(String variant) {
        Boolean flag = false;
        if (variant != null && Pattern.matches(fusionRegex, variant)) {
            flag = true;
        }
        return flag;
    }

    public static Alteration getRevertFusions(Alteration alteration) {
        Alteration revertFusionAlt = null;
        if (alteration != null && alteration.getAlteration() != null
            && isFusion(alteration.getAlteration())) {
            Pattern pattern = Pattern.compile(fusionRegex);
            Matcher matcher = pattern.matcher(alteration.getAlteration());
            if (matcher.matches() && matcher.groupCount() == 3) {
                // Revert fusion
                String geneA = matcher.group(2);
                String geneB = matcher.group(3);
                String revertFusion = geneB + "-" + geneA + " fusion";

                revertFusionAlt = alterationBo.findAlteration(alteration.getGene(),
                    alteration.getAlterationType(), revertFusion);
            }
        }
        return revertFusionAlt;
    }

    public static String trimAlterationName(String alteration) {
        if (alteration != null) {
            if (alteration.startsWith("p.")) {
                alteration = alteration.substring(2);
            }
        }
        return alteration;
    }

    public static Alteration getAlteration(String hugoSymbol, String alteration, String alterationType,
                                           String consequence, Integer proteinStart, Integer proteinEnd) {
        Alteration alt = new Alteration();

        if (alteration != null) {
            alteration = AlterationUtils.trimAlterationName(alteration);
            alt.setAlteration(alteration);
        }

        Gene gene = null;
        if (hugoSymbol != null) {
            gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        }
        alt.setGene(gene);

        AlterationType type = AlterationType.MUTATION;
        if (alterationType != null) {
            AlterationType t = AlterationType.valueOf(alterationType.toUpperCase());
            if (t != null) {
                type = t;
            }
        }
        alt.setAlterationType(type);

        VariantConsequence variantConsequence = null;
        if (consequence != null) {
            variantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm(consequence);

            if (variantConsequence == null) {
                variantConsequence = new VariantConsequence();
                variantConsequence.setTerm(consequence);
            }
        }
        alt.setConsequence(variantConsequence);

        if (proteinEnd == null) {
            proteinEnd = proteinStart;
        }
        alt.setProteinStart(proteinStart);
        alt.setProteinEnd(proteinEnd);

        AlterationUtils.annotateAlteration(alt, alt.getAlteration());
        return alt;
    }

    public static String getOncogenic(List<Alteration> alterations) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.ONCOGENIC));
        return findHighestOncogenic(evidences);
    }

    private static String findHighestOncogenic(List<Evidence> evidences) {
        String oncogenic = "";
        for (Evidence evidence : evidences) {
            oncogenic = oncogenicList.indexOf(evidence.getKnownEffect()) < oncogenicList.indexOf(oncogenic) ? oncogenic : evidence.getKnownEffect();
        }
        return oncogenic;
    }

    public static Set<Alteration> getAllAlterations(Gene gene) {
        if (CacheUtils.isEnabled()) {
            if (!CacheUtils.containAlterations(gene.getEntrezGeneId())) {
                CacheUtils.setAlterations(gene.getEntrezGeneId(),
                    new HashSet<>(alterationBo.findAlterationsByGene(Collections.singleton(gene))));
            }
            return CacheUtils.getAlterations(gene.getEntrezGeneId());
        } else {
            return new HashSet<>(alterationBo.findAlterationsByGene(Collections.singleton(gene)));
        }
    }

    public static Set<Alteration> getAllAlterations() {
        Set<Gene> genes = GeneUtils.getAllGenes();
        Set<Alteration> alterations = new HashSet<>();
        for (Gene gene : genes) {
            alterations.addAll(getAllAlterations(gene));
        }
        return alterations;
    }

    public static Set<Alteration> findVUSFromEvidences(Set<Evidence> evidences) {
        Set<Alteration> alterations = new HashSet<>();

        for (Evidence evidence : evidences) {
            if (evidence.getEvidenceType().equals(EvidenceType.VUS)) {
                alterations.addAll(evidence.getAlterations());
            }
        }

        return alterations;
    }

    public static List<Alteration> excludeVUS(List<Alteration> alterations) {
        List<Alteration> result = new ArrayList<>();
        Set<Alteration> VUS = new HashSet<>();
        Set<Gene> allGenes = CacheUtils.getAllGenes();
        for (Gene gene : allGenes) {
            Set<Alteration> alts = CacheUtils.getVUS(gene.getEntrezGeneId());
            if (alts != null) {
                VUS.addAll(alts);
            }
        }

        for (Alteration alteration : alterations) {
            if (!VUS.contains(alteration)) {
                result.add(alteration);
            }
        }

        return result;
    }

    public static List<Alteration> excludeVUS(Gene gene, List<Alteration> alterations) {
        List<Alteration> result = new ArrayList<>();
        Set<Alteration> VUS = CacheUtils.getVUS(gene.getEntrezGeneId());

        if (VUS == null) {
            VUS = new HashSet<>();
        }

        for (Alteration alteration : alterations) {
            if (!VUS.contains(alteration)) {
                result.add(alteration);
            }
        }

        return result;
    }

    public static List<Alteration> excludeGeneralAlterations(List<Alteration> alterations) {
        List<Alteration> result = new ArrayList<>();
        for (Alteration alteration : alterations) {
            String name = alteration.getAlteration().toLowerCase();
            if (name != null && !generalAlterations.contains(name)) {
                result.add(alteration);
            }
        }
        return result;
    }

    public static List<Alteration> getAlterations(Gene gene, String alteration, String consequence, Integer proteinStart, Integer proteinEnd, Set<Alteration> fullAlterations) {
        List<Alteration> alterations = new ArrayList<>();
        VariantConsequence variantConsequence = null;

        if (gene != null && alteration != null) {
            if (consequence != null) {
                Alteration alt = new Alteration();
                alt.setAlteration(alteration);
                variantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm(consequence);
                if (variantConsequence == null) {
                    variantConsequence = new VariantConsequence(consequence, null, false);
                }
                alt.setConsequence(variantConsequence);
                alt.setAlterationType(AlterationType.MUTATION);
                alt.setGene(gene);
                alt.setProteinStart(proteinStart);
                alt.setProteinEnd(proteinEnd);

                AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                List<Alteration> alts = alterationBo.findRelevantAlterations(alt, new ArrayList<>(fullAlterations));
                if (!alts.isEmpty()) {
                    alterations.addAll(alts);
                }
            } else {
                Alteration alt = new Alteration();
                alt.setAlteration(alteration);
                alt.setAlterationType(AlterationType.MUTATION);
                alt.setGene(gene);
                alt.setProteinStart(proteinStart);
                alt.setProteinEnd(proteinEnd);

                AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                List<Alteration> alts = alterationBo.findRelevantAlterations(alt, new ArrayList<>(fullAlterations));
                if (!alts.isEmpty()) {
                    alterations.addAll(alts);
                }
            }
        }

        if (isFusion(alteration)) {
            Alteration alt = new Alteration();
            alt.setAlteration(alteration);
            alt.setAlterationType(AlterationType.MUTATION);
            alt.setGene(gene);

            AlterationUtils.annotateAlteration(alt, alt.getAlteration());
            Alteration revertFusion = getRevertFusions(alt);
            if (revertFusion != null) {
                List<Alteration> alts = alterationBo.findRelevantAlterations(revertFusion, new ArrayList<>(fullAlterations));
                if (alts != null) {
                    alterations.addAll(alts);
                }
            }
        }
        return alterations;
    }

    public static Set<Alteration> getAlleleAlterations(Alteration alteration) {
        List<Alteration> alterations = new ArrayList<>();

        if (CacheUtils.isEnabled()) {
            alterations = new ArrayList<>(CacheUtils.getAlterations(alteration.getGene().getEntrezGeneId()));
        } else {
            alterations = alterationBo.findAlterationsByGene(Collections.singleton(alteration.getGene()));
        }

        Set<Alteration> alleles = new HashSet<>(alterationBo.findMutationsByConsequenceAndPosition(
            alteration.getGene(), alteration.getConsequence(), alteration.getProteinStart(),
            alteration.getProteinEnd(), alterations));

        for (Alteration allele : alleles) {
            if (isOncogenicAlteration(allele)) {
                alleles.addAll(getOncogenicMutations(allele));
                break;
            }
        }
        // Remove alteration itself
        alleles.remove(alteration);
        return filterAllelesBasedOnLocation(new HashSet<>(alleles), alteration.getProteinStart());
    }

    public static List<Alteration> getRelevantAlterations(Alteration alteration) {
        if (alteration == null || alteration.getGene() == null) {
            return new ArrayList<>();
        }
        Gene gene = alteration.getGene();
        VariantConsequence consequence = alteration.getConsequence();
        String term = consequence == null ? null : consequence.getTerm();
        Integer proteinStart = alteration.getProteinStart();
        Integer proteinEnd = alteration.getProteinEnd();

        String id = gene.getHugoSymbol() + alteration.getAlteration() + (consequence == null ? "" : consequence);

        if (CacheUtils.isEnabled()) {
            if (!CacheUtils.containRelevantAlterations(gene.getEntrezGeneId(), id)) {
                CacheUtils.setRelevantAlterations(
                    gene.getEntrezGeneId(), id,
                    getAlterations(
                        gene, alteration.getAlteration(), term,
                        proteinStart, proteinEnd,
                        getAllAlterations(gene)));
            }

            return CacheUtils.getRelevantAlterations(gene.getEntrezGeneId(), id);
        } else {
            return getAlterations(
                gene, alteration.getAlteration(), term,
                proteinStart, proteinEnd,
                getAllAlterations(gene));
        }
    }

    public static Boolean hasAlleleAlterations(Alteration alteration) {
        Set<Alteration> setAlleles = AlterationUtils.getAlleleAlterations(alteration);
        List<Alteration> alleles = setAlleles == null ? new ArrayList<Alteration>() : new ArrayList<>(setAlleles);
        
        alleles = AlterationUtils.excludeVUS(alleles);
        if (alleles.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static Alteration findAlteration(Gene gene, String alteration) {
        if (CacheUtils.isEnabled()) {
            Set<Alteration> alterations = CacheUtils.getAlterations(gene.getEntrezGeneId());
            for (Alteration al : alterations) {
                if (al.getAlteration().equalsIgnoreCase(alteration)) {
                    return al;
                }
            }
            return null;
        } else {
            return alterationBo.findAlteration(gene, AlterationType.MUTATION, alteration);
        }
    }

    public static Set<Alteration> filterAllelesBasedOnLocation(Set<Alteration> alterations, Integer location) {
        Set<Alteration> result = new HashSet<>();

        for (Alteration alteration : alterations) {
            if (alteration.getProteinStart() != null
                && alteration.getProteinEnd() != null
                && alteration.getProteinStart().equals(alteration.getProteinEnd())
                && alteration.getProteinStart().equals(location)) {

                result.add(alteration);
            } else if (alteration.getAlteration() != null
                && alteration.getAlteration().toLowerCase().contains("activating")) {
                result.add(alteration);
            }
        }
        return result;
    }

    public static Boolean isOncogenicAlteration(Alteration alteration) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> mutationEffectEvs = evidenceBo.findEvidencesByAlteration(Collections.singleton(alteration), Collections.singleton(EvidenceType.MUTATION_EFFECT));
        boolean activating = false, inactivating = false;
        for (Evidence evidence : mutationEffectEvs) {
            String effect = evidence.getKnownEffect();
            if (effect != null) {
                effect = effect.toLowerCase();
                if (effect.contains("inactivating") || effect.contains("loss-of-function")) {
                    inactivating = true;
                } else if (effect.contains("activating") || effect.contains("gain-of-function")
                    || effect.contains("switch-of-function")) {
                    activating = true;
                }
            }
        }

        return inactivating || activating;
    }
    
    public static Set<Alteration> getOncogenicMutations(Alteration alteration) {
        Set<Alteration> oncogenicMutations = new HashSet<>();
        Alteration alt = findAlteration(alteration.getGene(), "inactivating mutations");
        if (alt != null) {
            oncogenicMutations.add(alt);
        }

        alt = findAlteration(alteration.getGene(), "activating mutations");
        if (alt != null) {
            oncogenicMutations.add(alt);
        }
        return oncogenicMutations;
    }
}
