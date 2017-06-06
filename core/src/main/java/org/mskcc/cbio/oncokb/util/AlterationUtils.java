/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
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
    private static List<String> oncogenicList = Arrays.asList(new String[]{
        "", Oncogenicity.INCONCLUSIVE.getOncogenic(), Oncogenicity.LIKELY_NEUTRAL.getOncogenic(),
        Oncogenicity.LIKELY.getOncogenic(), Oncogenicity.YES.getOncogenic()});

    private static AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();

    private final static String[] inferredAlts = {"Oncogenic Mutations", "Gain-of-function Mutations",
        "Loss-of-function Mutations", "Switch-of-function Mutations"};
    private final static List<String> inferredAlterations = Arrays.asList(inferredAlts);

    private final static String[] structureAlts = {"Truncating Mutations", "Fusions", "Amplification", "Deletion"};
    private final static List<String> structureAlterations = Arrays.asList(structureAlts);

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

        Pattern p = Pattern.compile("([A-Z\\*])([0-9]+)([A-Z\\*\\?]?)");
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
                consequence = "start_lost";
            } else if (var.equals("?")) {
                consequence = "any";
            } else {
                consequence = "missense_variant";
            }
        } else {
            p = Pattern.compile("[A-Z]?([0-9]+)(_[A-Z]?([0-9]+))?(delins|ins)([A-Z]+)");
            m = p.matcher(proteinChange);
            if (m.matches()) {
                start = Integer.valueOf(m.group(1));
                if (m.group(3) != null) {
                    end = Integer.valueOf(m.group(3));
                } else {
                    end = start;
                }
                String type = m.group(4);
                if (type.equals("ins")) {
                    consequence = "inframe_insertion";
                } else {
                    Integer deletion = end - start + 1;
                    Integer insertion = m.group(5).length();

                    if (insertion - deletion > 0) {
                        consequence = "inframe_insertion";
                    } else if (insertion - deletion == 0) {
                        consequence = "missense_variant";
                    } else {
                        consequence = "inframe_deletion";
                    }
                }
            } else {
                p = Pattern.compile("[A-Z]?([0-9]+)(_[A-Z]?([0-9]+))?(_)?splice");
                m = p.matcher(proteinChange);
                if (m.matches()) {
                    start = Integer.valueOf(m.group(1));
                    if (m.group(3) != null) {
                        end = Integer.valueOf(m.group(3));
                    } else {
                        end = start;
                    }
                    consequence = "splice_region_variant";
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
                            case "dup":
                                consequence = "inframe_insertion";
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
                            p = Pattern.compile("([A-Z]+)?([0-9]+)((ins)|(del)|(dup))");
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
                                    case "dup":
                                        consequence = "inframe_insertion";
                                        break;
                                    case "del":
                                        consequence = "inframe_deletion";
                                        break;
                                }
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
        } else if (alteration.getConsequence() != null && variantConsequence != null &&
            !alteration.getConsequence().equals(variantConsequence) &&
            alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm("any"))) {
            // For the query which already contains consequence but different with OncoKB algorithm,
            // we should keep query consequence unless it is `any`
            alteration.setConsequence(variantConsequence);
        }

        // Annotate alteration based on consequence and special rules
        if (alteration.getAlteration() == null || alteration.getAlteration().isEmpty()) {
            alteration.setAlteration(proteinChange);
        }
        if (alteration.getAlteration().isEmpty()) {
            if (variantConsequence != null) {
                if (variantConsequence.getTerm().equals("splice_region_variant")) {
                    alteration.setAlteration("splice mutation");
                }
            }
        } else {
            if (alteration.getAlteration().toLowerCase().matches("gain")) {
                alteration.setAlteration("Amplification");
            } else if (alteration.getAlteration().toLowerCase().matches("loss")) {
                alteration.setAlteration("Deletion");
            }
        }

        if (alteration.getName() == null && alteration.getAlteration() != null) {
            alteration.setName(alteration.getAlteration());
        }
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
                CacheUtils.setAlterations(gene);
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

    public static Alteration getTruncatingMutations(Gene gene) {
        return findAlteration(gene, "Truncating Mutations");
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
        Set<Gene> allGenes = new HashSet<>();
        if (CacheUtils.isEnabled()) {
            allGenes = CacheUtils.getAllGenes();
        } else {
            allGenes = new HashSet<>(ApplicationContextSingleton.getGeneBo().findAll());
        }
        for (Gene gene : allGenes) {
            Set<Alteration> alts = new HashSet<>();
            if (CacheUtils.isEnabled()) {
                alts = CacheUtils.getVUS(gene.getEntrezGeneId());
            } else {
                alts = AlterationUtils.findVUSFromEvidences(EvidenceUtils.getEvidenceByGenes(Collections.singleton(gene)).get(gene));
            }
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

    public static List<Alteration> excludeInferredAlterations(List<Alteration> alterations) {
        List<Alteration> result = new ArrayList<>();
        for (Alteration alteration : alterations) {
            String name = alteration.getAlteration();
            if (name != null) {
                Boolean contain = false;
                for (String inferredAlt : inferredAlterations) {
                    if (inferredAlt.equalsIgnoreCase(name)) {
                        contain = true;
                    }
                }
                if (!contain) {
                    result.add(alteration);
                }
            }
        }
        return result;
    }

    private static List<Alteration> getAlterations(Gene gene, String alteration, String consequence, Integer proteinStart, Integer proteinEnd, Set<Alteration> fullAlterations) {
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

                LinkedHashSet<Alteration> alts = alterationBo.findRelevantAlterations(alt, new ArrayList<>(fullAlterations));
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

                LinkedHashSet<Alteration> alts = alterationBo.findRelevantAlterations(alt, new ArrayList<>(fullAlterations));
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
                LinkedHashSet<Alteration> alts = alterationBo.findRelevantAlterations(revertFusion, new ArrayList<>(fullAlterations));
                if (alts != null) {
                    alterations.addAll(alts);
                }
            }
        }
        return alterations;
    }

    public static List<Alteration> getAlleleAlterations(Alteration alteration) {
        List<Alteration> alterations = new ArrayList<>();

        if (alteration == null || alteration.getConsequence() == null ||
            !alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant"))) {
            return alterations;
        }
        if (CacheUtils.isEnabled()) {
            alterations = new ArrayList<>(CacheUtils.getAlterations(alteration.getGene().getEntrezGeneId()));
        } else {
            alterations = alterationBo.findAlterationsByGene(Collections.singleton(alteration.getGene()));
        }

        List<Alteration> alleles = alterationBo.findMutationsByConsequenceAndPosition(
            alteration.getGene(), VariantConsequenceUtils.findVariantConsequenceByTerm("missense_variant"), alteration.getProteinStart(),
            alteration.getProteinEnd(), alterations);

        // Remove alteration itself
        alleles.remove(alteration);
        sortAlternativeAlleles(alleles);
        return alleles;
    }

    public static List<Alteration> lookupVarinat(String query, Boolean exactMatch, Set<Alteration> alterations) {
        List<Alteration> alterationList = new ArrayList<>();
        // Only support variant blur search for now.
        query = query.toLowerCase().trim();
        if (exactMatch == null)
            exactMatch = false;
        if (com.mysql.jdbc.StringUtils.isNullOrEmpty(query))
            return alterationList;
        query = query.trim().toLowerCase();
        for (Alteration alteration : alterations) {
            if (alteration.getAlteration() != null) {
                if (exactMatch) {
                    if (alteration.getAlteration().toLowerCase().equals(query)) {
                        alterationList.add(alteration);
                        continue;
                    }
                } else {
                    if (alteration.getAlteration().toLowerCase().contains(query)) {
                        alterationList.add(alteration);
                        continue;
                    }
                }
            }
            if (alteration.getName() != null) {
                if (exactMatch) {
                    if (alteration.getName().toLowerCase().equals(query)) {
                        alterationList.add(alteration);
                        continue;
                    }
                } else {
                    if (alteration.getName().toLowerCase().contains(query)) {
                        alterationList.add(alteration);
                        continue;
                    }
                }
            }
        }
        return alterationList;
    }

    // Sort the alternative alleles alphabetically
    private static void sortAlternativeAlleles(List<Alteration> alternativeAlleles) {
        Collections.sort(alternativeAlleles, new Comparator<Alteration>() {
            @Override
            public int compare(Alteration a1, Alteration a2) {
                return a1.getAlteration().compareTo(a2.getAlteration());
            }
        });
    }

    public static List<Alteration> getAlleleAndRelevantAlterations(Alteration alteration) {
        List<Alteration> alleles = getAlleleAlterations(alteration);
        Alteration oncogenicAllele = AlterationUtils.findOncogenicAllele(alleles);

        if (oncogenicAllele != null) {
            alleles.addAll(AlterationUtils.getOncogenicMutations(oncogenicAllele));
        }
        return alleles;
    }

    public static Alteration findOncogenicAllele(List<Alteration> alleles) {
        for (Alteration allele : alleles) {
            if (isOncogenicAlteration(allele)) {
                return allele;
            }
        }
        return null;
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

        String id = alteration.getUniqueId();

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
        List<Alteration> alleles = AlterationUtils.getAlleleAlterations(alteration);

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

    public static Boolean isOncogenicAlteration(Alteration alteration) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> oncogenicEvs = evidenceBo.findEvidencesByAlteration(Collections.singleton(alteration), Collections.singleton(EvidenceType.ONCOGENIC));
        boolean isOncogenic = false;
        for (Evidence evidence : oncogenicEvs) {
            Oncogenicity oncogenicity = Oncogenicity.getByEvidence(evidence);
            if (oncogenicity != null
                && (oncogenicity.equals(Oncogenicity.YES) || oncogenicity.equals(Oncogenicity.LIKELY))) {
                isOncogenic = true;
                break;
            }
        }
        return isOncogenic;
    }

    public static Set<Alteration> getOncogenicMutations(Alteration alteration) {
        Set<Alteration> oncogenicMutations = new HashSet<>();
        Alteration alt = findAlteration(alteration.getGene(), "oncogenic mutations");
        if (alt != null) {
            oncogenicMutations.add(alt);
        }
        return oncogenicMutations;
    }

    public static List<String> getGeneralAlterations() {
        List<String> suggestedAlterations = new ArrayList<>();
        suggestedAlterations.addAll(inferredAlterations);
        suggestedAlterations.addAll(structureAlterations);
        return suggestedAlterations;
    }

    public static Boolean isGeneralAlterations(String mutationStr, Boolean exactMatch) {
        exactMatch = exactMatch || false;
        if (exactMatch) {
            return MainUtils.containsCaseInsensitive(mutationStr, AlterationUtils.getGeneralAlterations());
        } else if (stringContainsItemFromList(mutationStr, getGeneralAlterations())
            && itemFromListAtEndString(mutationStr, getGeneralAlterations())) {
            return true;
        }
        return false;
    }

    private static boolean stringContainsItemFromList(String inputString, List<String> items) {
        for (String item : items) {
            if (StringUtils.containsIgnoreCase(inputString, item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean itemFromListAtEndString(String inputString, List<String> items) {
        for (String item : items) {
            if (StringUtils.endsWithIgnoreCase(inputString, item)) {
                return true;
            }
        }
        return false;
    }
}
