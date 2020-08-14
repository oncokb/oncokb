/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.genomenexus.TranscriptConsequence;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mskcc.cbio.oncokb.Constants.MISSENSE_VARIANT;

/**
 * @author jgao
 */
public final class AlterationUtils {
    private static List<String> oncogenicList = Arrays.asList(new String[]{
        "", Oncogenicity.INCONCLUSIVE.getOncogenic(), Oncogenicity.LIKELY_NEUTRAL.getOncogenic(),
        Oncogenicity.LIKELY.getOncogenic(), Oncogenicity.YES.getOncogenic()});

    private static AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();

    private final static String fusionRegex = "((\\w*)-(\\w*))\\s+(?i)fusion";

    private AlterationUtils() {
        throw new AssertionError();
    }

    public static Set<Alteration> findOverlapAlteration(Set<Alteration> alterations, Gene gene, VariantConsequence consequence, int start, int end) {
        Set<Alteration> overlaps = new HashSet<>();
        for (Alteration alteration : alterations) {
            if (alteration.getGene().equals(gene) && alteration.getConsequence() != null && alteration.getConsequence().equals(consequence)) {
                //For alteration without specific position, do not do intersection
                if (start <= AlterationPositionBoundary.START.getValue() || end >= AlterationPositionBoundary.END.getValue()) {
                    if (start >= alteration.getProteinStart()
                        && end <= alteration.getProteinEnd()) {
                        overlaps.add(alteration);
                    }
                } else if (end >= alteration.getProteinStart()
                    && start <= alteration.getProteinEnd()) {
                    //For variant, as long as they are overlapped to each, return the alteration
                    overlaps.add(alteration);
                }
            }
        }
        return overlaps;
    }

    public static void annotateAlteration(Alteration alteration, String proteinChange) {
        String consequence = "NA";
        String ref = null;
        String var = null;
        Integer start = AlterationPositionBoundary.START.getValue();
        Integer end = AlterationPositionBoundary.END.getValue();

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

        Pattern p = Pattern.compile("^([A-Z\\*]+)([0-9]+)([A-Z\\*\\?]*)$");
        Matcher m = p.matcher(proteinChange);
        if (m.matches()) {
            ref = m.group(1);
            start = Integer.valueOf(m.group(2));
            end = start;
            var = m.group(3);

            Integer refL = ref.length();
            Integer varL = var.length();

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
                end = start + refL - 1;
                if (refL > 1 || varL > 1) {
                    // Handle in-frame insertion/deletion event. Exp: IK744K
                    if (refL > varL) {
                        consequence = "inframe_deletion";
                    } else if (refL < varL) {
                        consequence = "inframe_insertion";
                    } else {
                        consequence = MISSENSE_VARIANT;
                    }
                } else if (refL == 1 && varL == 1) {
                    consequence = MISSENSE_VARIANT;
                } else {
                    consequence = "NA";
                }
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
                        consequence = MISSENSE_VARIANT;
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
                                consequence = MISSENSE_VARIANT;
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

        if (alteration.getProteinStart() == null || (start != null && start != AlterationPositionBoundary.START.getValue())) {
            alteration.setProteinStart(start);
        }

        if (alteration.getProteinEnd() == null || (end != null && end != AlterationPositionBoundary.END.getValue())) {
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

        if (com.mysql.jdbc.StringUtils.isNullOrEmpty(alteration.getName()) && alteration.getAlteration() != null) {
            // Change the positional name
            if (isPositionedAlteration(alteration)) {
                alteration.setName(alteration.getAlteration() + " Missense Mutations");
            } else {
                alteration.setName(alteration.getAlteration());
            }
        }
    }

    public static Boolean isFusion(String variant) {
        Boolean flag = false;
        if (variant != null && Pattern.matches(fusionRegex, variant)) {
            flag = true;
        }
        return flag;
    }

    public static Alteration getRevertFusions(Alteration alteration, Set<Alteration> fullAlterations) {
        if (fullAlterations == null) {
            return getRevertFusions(alteration);
        } else {
            String revertFusionAltStr = getRevertFusionName(alteration);
            Optional<Alteration> match = fullAlterations.stream().filter(alteration1 -> alteration1.getGene().equals(alteration.getGene()) && alteration1.getAlteration().equalsIgnoreCase(revertFusionAltStr)).findFirst();
            if (match.isPresent()) {
                return match.get();
            } else {
                return null;
            }
        }
    }

    public static Alteration getRevertFusions(Alteration alteration) {
        return alterationBo.findAlteration(alteration.getGene(),
            alteration.getAlterationType(), getRevertFusionName(alteration));
    }

    private static String getRevertFusionName(Alteration alteration) {
        String revertFusionAltStr = null;
        if (alteration != null && alteration.getAlteration() != null
            && isFusion(alteration.getAlteration())) {
            Pattern pattern = Pattern.compile(fusionRegex);
            Matcher matcher = pattern.matcher(alteration.getAlteration());
            if (matcher.matches() && matcher.groupCount() == 3) {
                // Revert fusion
                String geneA = matcher.group(2);
                String geneB = matcher.group(3);
                revertFusionAltStr = geneB + "-" + geneA + " fusion";
            }
        }
        return revertFusionAltStr;
    }

    public static String trimAlterationName(String alteration) {
        if (alteration != null) {
            if (alteration.startsWith("p.")) {
                alteration = alteration.substring(2);
            }
        }
        return alteration;
    }

    public static Alteration getAlteration(String hugoSymbol, String alteration, AlterationType alterationType,
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
            if (alterationType != null) {
                type = alterationType;
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

    public static Alteration getAlterationFromGenomeNexus(GNVariantAnnotationType type, String query) {
        Alteration alteration = null;
        if (query != null && !query.trim().isEmpty()) {
            TranscriptConsequence transcriptConsequence = GenomeNexusUtils.getTranscriptConsequence(type, query);
            if (transcriptConsequence != null) {
                String hugoSymbol = transcriptConsequence.getGeneSymbol();
                Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
                if (gene != null) {
                    alteration = new Alteration();
                    alteration.setGene(gene);
                    alteration.setAlterationType(null);
                    if (transcriptConsequence.getHgvspShort() != null) {
                        alteration.setAlteration(transcriptConsequence.getHgvspShort());
                    }
                    if (transcriptConsequence.getProteinStart() != null) {
                        alteration.setProteinStart(Integer.parseInt(transcriptConsequence.getProteinStart()));
                    }
                    if (transcriptConsequence.getProteinEnd() != null) {
                        alteration.setProteinEnd(Integer.parseInt(transcriptConsequence.getProteinEnd()));
                    }
                    if (transcriptConsequence.getConsequence() != null) {
                        alteration.setConsequence(VariantConsequenceUtils.findVariantConsequenceByTerm(transcriptConsequence.getConsequence()));
                    }
                    return alteration;
                }
            }
        }
        return alteration;
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

    public static Set<Alteration> getVUS(Alteration alteration){
        Set<Alteration> result = new HashSet<>();
        Gene gene = alteration.getGene();
        if (CacheUtils.isEnabled()) {
            result = CacheUtils.getVUS(gene.getEntrezGeneId());
        } else {
            result = AlterationUtils.findVUSFromEvidences(EvidenceUtils.getEvidenceByGenes(Collections.singleton(gene)).get(gene));
        }
        return result;
    }
    public static List<Alteration> excludeVUS(List<Alteration> alterations) {
        List<Alteration> result = new ArrayList<>();

        for (Alteration alteration : alterations) {
            Set<Alteration> VUS = AlterationUtils.getVUS(alteration);
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
                for (String inferredAlt : getInferredMutations()) {
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

    public static List<Alteration> excludePositionedAlterations(List<Alteration> alterations) {
        List<Alteration> result = new ArrayList<>();
        for (Alteration alteration : alterations) {
            if (!isPositionedAlteration(alteration)) {
                result.add(alteration);
            }
        }
        return result;
    }

    public static Boolean isInferredAlterations(String alteration) {
        Boolean isInferredAlt = false;
        if (alteration != null) {
            for (String alt : getInferredMutations()) {
                if (alteration.equalsIgnoreCase(alt)) {
                    isInferredAlt = true;
                    break;
                }
            }
        }
        return isInferredAlt;
    }

    public static Boolean isLikelyInferredAlterations(String alteration) {
        Boolean isLikelyInferredAlt = false;
        if (alteration != null) {
            String lowerCaseAlteration = alteration.trim().toLowerCase();
            if (lowerCaseAlteration.startsWith("likely")) {
                alteration = alteration.replaceAll("(?i)likely", "").trim();
                for (String alt : getInferredMutations()) {
                    if (alteration.equalsIgnoreCase(alt)) {
                        isLikelyInferredAlt = true;
                        break;
                    }
                }
            }
        }
        return isLikelyInferredAlt;
    }

    public static Set<Alteration> getEvidencesAlterations(Set<Evidence> evidences) {
        Set<Alteration> alterations = new HashSet<>();
        if (evidences == null) {
            return alterations;
        }
        for (Evidence evidence : evidences) {
            if (evidence.getAlterations() != null) {
                alterations.addAll(evidence.getAlterations());
            }
        }
        return alterations;
    }

    public static Set<Alteration> getAlterationsByKnownEffectInGene(Gene gene, String knownEffect, Boolean includeLikely) {
        Set<Alteration> alterations = new HashSet<>();
        if (includeLikely == null) {
            includeLikely = false;
        }
        if (gene != null && knownEffect != null) {
            Set<Evidence> evidences = EvidenceUtils.getEvidenceByGenes(Collections.singleton(gene)).get(gene);
            for (Evidence evidence : evidences) {
                if (knownEffect.equalsIgnoreCase(evidence.getKnownEffect())) {
                    alterations.addAll(evidence.getAlterations());
                }
                if (includeLikely) {
                    String likely = "likely " + knownEffect;
                    if (likely.equalsIgnoreCase(evidence.getKnownEffect())) {
                        alterations.addAll(evidence.getAlterations());
                    }
                }
            }
        }
        return alterations;
    }

    public static String getInferredAlterationsKnownEffect(String inferredAlt) {
        String knownEffect = null;
        if (inferredAlt != null) {
            knownEffect = inferredAlt.replaceAll("(?i)\\s+mutations", "");
        }
        return knownEffect;
    }

    private static List<Alteration> getAlterations(Gene gene, String alteration, AlterationType alterationType, String consequence, Integer proteinStart, Integer proteinEnd, Set<Alteration> fullAlterations) {
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
                alt.setAlterationType(alterationType == null ? AlterationType.MUTATION : alterationType);
                alt.setGene(gene);
                alt.setProteinStart(proteinStart);
                alt.setProteinEnd(proteinEnd);

                AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                LinkedHashSet<Alteration> alts = alterationBo.findRelevantAlterations(alt, fullAlterations, true);
                if (!alts.isEmpty()) {
                    alterations.addAll(alts);
                }
            } else {
                Alteration alt = new Alteration();
                alt.setAlteration(alteration);
                alt.setAlterationType(alterationType == null ? AlterationType.MUTATION : alterationType);
                alt.setGene(gene);
                alt.setProteinStart(proteinStart);
                alt.setProteinEnd(proteinEnd);

                AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                LinkedHashSet<Alteration> alts = alterationBo.findRelevantAlterations(alt, fullAlterations, true);
                if (!alts.isEmpty()) {
                    alterations.addAll(alts);
                }
            }
        }

        if (isFusion(alteration)) {
            Alteration alt = new Alteration();
            alt.setAlteration(alteration);
            alt.setAlterationType(alterationType == null ? AlterationType.MUTATION : alterationType);
            alt.setGene(gene);

            AlterationUtils.annotateAlteration(alt, alt.getAlteration());
            Alteration revertFusion = getRevertFusions(alt, fullAlterations);
            if (revertFusion != null) {
                LinkedHashSet<Alteration> alts = alterationBo.findRelevantAlterations(revertFusion, fullAlterations, true);
                if (alts != null) {
                    alterations.addAll(alts);
                }
            }
        }
        return alterations;
    }

    public static List<Alteration> getAlleleAlterations(Alteration alteration) {
        return getAlleleAlterationsSub(alteration, getAllAlterations(alteration.getGene()));
    }

    public static List<Alteration> getAlleleAlterations(Alteration alteration, Set<Alteration> fullAlterations) {
        return getAlleleAlterationsSub(alteration, fullAlterations);
    }

    // Only for missense alteration
    public static List<Alteration> getPositionedAlterations(Alteration alteration) {
        return getPositionedAlterations(alteration, getAllAlterations(alteration.getGene()));
    }

    // Only for missense alteration
    public static List<Alteration> getPositionedAlterations(Alteration alteration, Set<Alteration> fullAlterations) {
        if (alteration.getGene().getHugoSymbol().equals("ABL1") && alteration.getAlteration().equals("T315I")) {
            return new ArrayList<>();
        }

        if (alteration.getConsequence() != null && alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm(MISSENSE_VARIANT))
            && alteration.getProteinStart().intValue() != AlterationPositionBoundary.START.getValue() && alteration.getProteinStart().intValue() != AlterationPositionBoundary.END.getValue()) {
            VariantConsequence variantConsequence = new VariantConsequence();
            variantConsequence.setTerm("NA");
            return ApplicationContextSingleton.getAlterationBo().findMutationsByConsequenceAndPositionOnSamePosition(alteration.getGene(), variantConsequence, alteration.getProteinStart(), alteration.getProteinEnd(), fullAlterations);
        }
        return new ArrayList<>();
    }

    public static List<Alteration> getUniqueAlterations(List<Alteration> alterations) {
        return new ArrayList<>(new LinkedHashSet<>(alterations));
    }

    private static List<Alteration> getAlleleAlterationsSub(Alteration alteration, Set<Alteration> fullAlterations) {
        if (alteration == null || alteration.getConsequence() == null ||
            !alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm(MISSENSE_VARIANT))) {
            return new ArrayList<>();
        }

        if (alteration.getGene().getHugoSymbol().equals("ABL1") && alteration.getAlteration().equals("T315I")) {
            return new ArrayList<>();
        }

        List<Alteration> missenseVariants = alterationBo.findMutationsByConsequenceAndPosition(
            alteration.getGene(), VariantConsequenceUtils.findVariantConsequenceByTerm(MISSENSE_VARIANT), alteration.getProteinStart(),
            alteration.getProteinEnd(), fullAlterations);

        List<Alteration> alleles = new ArrayList<>();
        for (Alteration alt : missenseVariants) {
            if (alt.getProteinStart() != null && alt.getProteinEnd() != null && alt.getProteinStart().equals(alt.getProteinEnd()) && !alt.equals(alteration)) {
                alleles.add(alt);
            }
        }

        // Special case for PDGFRA: don't match D842V as alternative allele to other alleles
        if (alteration.getGene() != null && alteration.getGene().getEntrezGeneId() == 5156 && !alteration.getAlteration().equals("D842V")) {
            Alteration d842v = AlterationUtils.findAlteration(alteration.getGene(), "D842V");
            alleles.remove(d842v);
        }

        sortAlternativeAlleles(alleles);
        return alleles;
    }

    public static void removeAlternativeAllele(Alteration alteration, List<Alteration> relevantAlterations) {
        // the alternative alleles do not only include the different variant allele, but also include the delins but it's essentially the same thing.
        // For instance, S768_V769delinsIL. This is equivalent to S768I + V769L, S768I should be listed relevant and not be excluded.
        if (alteration != null && alteration.getConsequence() != null && alteration.getConsequence().getTerm().equals(MISSENSE_VARIANT)) {
            // check for positional variant when the consequence is forced to be missense variant
            boolean isMissensePositionalVariant = StringUtils.isEmpty(alteration.getVariantResidues()) && alteration.getProteinStart() != null && alteration.getProteinEnd() != null && alteration.getProteinStart().equals(alteration.getProteinEnd());
            List<Alteration> alternativeAlleles = alterationBo.findMutationsByConsequenceAndPosition(alteration.getGene(), alteration.getConsequence(), alteration.getProteinStart(), alteration.getProteinEnd(), new HashSet<>(relevantAlterations));
            for (Alteration allele : alternativeAlleles) {
                // remove all alleles if the alteration variant residue is empty
                if (isMissensePositionalVariant && !StringUtils.isEmpty(allele.getVariantResidues())) {
                    relevantAlterations.remove(allele);
                    return;
                }
                if (allele.getConsequence() != null && allele.getConsequence().getTerm().equals(MISSENSE_VARIANT)) {
                    if (alteration.getProteinStart().equals(alteration.getProteinEnd()) && !StringUtils.isEmpty(alteration.getVariantResidues())) {
                        if (allele.getProteinStart().equals(allele.getProteinEnd())) {
                            if (!alteration.getVariantResidues().equalsIgnoreCase(allele.getVariantResidues())) {
                                relevantAlterations.remove(allele);
                            }
                        } else {
                            String alleleVariant = getMissenseVariantAllele(allele, alteration.getProteinStart());
                            if (alleleVariant != null && !alteration.getVariantResidues().equalsIgnoreCase(alleleVariant)) {
                                relevantAlterations.remove(allele);
                            }
                        }
                    } else {
                        boolean isRelevant = false;
                        for (int start = alteration.getProteinStart().intValue(); start <= alteration.getProteinEnd().intValue(); start++) {
                            String alterationAllele = getMissenseVariantAllele(alteration, start);
                            if (alterationAllele != null) {
                                if (allele.getProteinStart().equals(allele.getProteinEnd())) {
                                    if (alterationAllele.equalsIgnoreCase(allele.getVariantResidues())) {
                                        isRelevant = true;
                                        break;
                                    }
                                } else {
                                    String alleleVariant = getMissenseVariantAllele(allele, start);
                                    if (alleleVariant == null || alterationAllele.equalsIgnoreCase(alleleVariant)) {
                                        isRelevant = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!isRelevant) {
                            relevantAlterations.remove(allele);
                        }
                    }
                }
            }
        }
    }

    private static String getMissenseVariantAllele(Alteration alteration, int position) {
        Pattern pattern = Pattern.compile(".*delins([\\w]+)");
        Matcher matcher = pattern.matcher(alteration.getAlteration());
        if (matcher.find()) {
            String variantAlleles = matcher.group(1);
            int index = position - alteration.getProteinStart();
            if (index >= 0 && index < variantAlleles.length()) {
                return variantAlleles.substring(index, index + 1);
            } else {
                return null;
            }
        } else if (alteration.getVariantResidues() != null && alteration.getVariantResidues().length() > 0) {
            return alteration.getVariantResidues().substring(0, 1);
        }
        return null;
    }
    public static List<Alteration> lookupVariant(String query, Boolean exactMatch, Set<Alteration> alterations) {
        List<Alteration> alterationList = new ArrayList<>();
        // Only support columns(alteration/name) blur search.
        query = query.toLowerCase().trim();
        if (exactMatch == null)
            exactMatch = false;
        if (com.mysql.jdbc.StringUtils.isNullOrEmpty(query))
            return alterationList;
        query = query.trim().toLowerCase();
        for (Alteration alteration : alterations) {
            if (isMatch(exactMatch, query, alteration.getAlteration())) {
                alterationList.add(alteration);
                continue;
            }

            if (isMatch(exactMatch, query, alteration.getName())) {
                alterationList.add(alteration);
                continue;
            }
        }
        return alterationList;
    }

    private static Boolean isMatch(Boolean exactMatch, String query, String string) {
        if (string != null) {
            if (exactMatch) {
                if (StringUtils.containsIgnoreCase(string, query)) {
                    return true;
                }
            } else {
                if (StringUtils.containsIgnoreCase(string, query)) {
                    return true;
                }
            }
        }
        return false;
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

    public static void sortAlterationsByTheRange(List<Alteration> alterations, final int proteinStart, final int proteinEnd) {
        Collections.sort(alterations, new Comparator<Alteration>() {
            @Override
            public int compare(Alteration a1, Alteration a2) {
                if (a1.getProteinStart() == null || a1.getProteinEnd() == null) {
                    if (a2.getProteinStart() == null || a2.getProteinEnd() == null) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
                if (a2.getProteinStart() == null || a2.getProteinEnd() == null) {
                    return -1;
                }

                int overlap1 = Math.min(a1.getProteinEnd(), proteinEnd) - Math.max(a1.getProteinStart(), proteinStart);
                int overlap2 = Math.min(a2.getProteinEnd(), proteinEnd) - Math.max(a2.getProteinStart(), proteinStart);

                if (overlap1 == overlap2) {
                    int diff = a1.getProteinEnd() - a1.getProteinStart() - (a2.getProteinEnd() - a2.getProteinStart());
                    return diff == 0 ? a1.getAlteration().compareTo(a2.getAlteration()) : diff;
                } else {
                    return overlap2 - overlap1;
                }
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
            Boolean isOncogenicAlt = isOncogenicAlteration(allele);
            if (isOncogenicAlt != null && isOncogenicAlt) {
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

        return getAlterations(
            gene, alteration.getAlteration(), alteration.getAlterationType(), term,
            proteinStart, proteinEnd,
            getAllAlterations(gene));
    }

    public static List<Alteration> removeAlterationsFromList(List<Alteration> list, List<Alteration> alterationsToBeRemoved) {
        List<Alteration> cleanedList = new ArrayList<>();
        for (Alteration alt : list) {
            if (!alterationsToBeRemoved.contains(alt)) {
                cleanedList.add(alt);
            }
        }
        return cleanedList;
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
        if (gene == null) {
            return null;
        }
        return alterationBo.findAlteration(gene, AlterationType.MUTATION, alteration);
    }

    /**
     *
     * @param alteration Annotated alteration
     * @return A list of alterations we consider the same
     */
    public static LinkedHashSet<Alteration> findMatchedAlterations(Alteration alteration) {
        LinkedHashSet<Alteration> matches = new LinkedHashSet<>();
        Alteration matchedAlteration = findAlteration(alteration.getGene(), alteration.getAlteration());
        if (matchedAlteration != null) {
            matches.add(matchedAlteration);
        }
        if (alteration.getConsequence() != null
            && alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm(MISSENSE_VARIANT))
            && !alteration.getProteinStart().equals(AlterationPositionBoundary.START)
            && !alteration.getProteinEnd().equals(AlterationPositionBoundary.END)
            && !alteration.getProteinStart().equals(alteration.getProteinEnd())
        ) {
            Pattern p = Pattern.compile(".*delins([A-Z]+)");
            Matcher m = p.matcher(alteration.getAlteration());
            if (m.matches()) {
                Set<Alteration> allAlterations = getAllAlterations(alteration.getGene());
                String insertedAAs = m.group(1);
                for (int i = 0; i < insertedAAs.length(); i++) {
                    char varAA = insertedAAs.charAt(i);
                    int proteinStart = alteration.getProteinStart() + i;
                    List<Alteration> alterations = alterationBo.findMutationsByConsequenceAndPosition(alteration.getGene(), alteration.getConsequence(), proteinStart, proteinStart, allAlterations);
                    for (Alteration alt : alterations) {
                        if (alt.getVariantResidues() != null && alt.getVariantResidues().charAt(0) == varAA) {
                            matches.add(alt);
                        }
                    }
                }
            }
        }
        return matches;
    }

    public static Boolean isOncogenicAlteration(Alteration alteration) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> oncogenicEvs = evidenceBo.findEvidencesByAlteration(Collections.singleton(alteration), Collections.singleton(EvidenceType.ONCOGENIC));
        Boolean isOncogenic = null;
        for (Evidence evidence : oncogenicEvs) {
            Oncogenicity oncogenicity = Oncogenicity.getByEvidence(evidence);
            if (oncogenicity != null
                && (oncogenicity.equals(Oncogenicity.YES) || oncogenicity.equals(Oncogenicity.LIKELY))) {
                isOncogenic = true;
                break;
            } else if (oncogenicity != null && oncogenicity.equals(Oncogenicity.LIKELY_NEUTRAL)) {
                isOncogenic = false;
            }
            if (isOncogenic != null) {
                break;
            }
        }

        // If there is no oncogenicity specified by the system and it is hotspot, then this alteration should be oncogenic.
        if (isOncogenic == null && HotspotUtils.isHotspot(alteration)) {
            isOncogenic = true;
        }
        return isOncogenic;
    }

    public static Boolean hasImportantCuratedOncogenicity(Set<Oncogenicity> oncogenicities) {
        Set<Oncogenicity> curatedOncogenicities = new HashSet<>();
        curatedOncogenicities.add(Oncogenicity.YES);
        curatedOncogenicities.add(Oncogenicity.LIKELY);
        curatedOncogenicities.add(Oncogenicity.LIKELY_NEUTRAL);
        curatedOncogenicities.add(Oncogenicity.INCONCLUSIVE);
        return !Collections.disjoint(curatedOncogenicities, oncogenicities);
    }

    public static Boolean hasOncogenic(Set<Oncogenicity> oncogenicities) {
        Set<Oncogenicity> curatedOncogenicities = new HashSet<>();
        curatedOncogenicities.add(Oncogenicity.YES);
        curatedOncogenicities.add(Oncogenicity.LIKELY);
        return !Collections.disjoint(curatedOncogenicities, oncogenicities);
    }

    public static Set<Oncogenicity> getCuratedOncogenicity(Alteration alteration) {
        Set<Oncogenicity> curatedOncogenicities = new HashSet<>();

        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> oncogenicEvs = evidenceBo.findEvidencesByAlteration(Collections.singleton(alteration), Collections.singleton(EvidenceType.ONCOGENIC));

        for (Evidence evidence : oncogenicEvs) {
            curatedOncogenicities.add(Oncogenicity.getByEvidence(evidence));
        }
        return curatedOncogenicities;
    }

    public static Set<Alteration> getOncogenicMutations(Alteration alteration) {
        Set<Alteration> oncogenicMutations = new HashSet<>();
        Alteration alt = findAlteration(alteration.getGene(), "oncogenic mutations");
        if (alt != null) {
            oncogenicMutations.add(alt);
        }
        return oncogenicMutations;
    }

    public static Set<String> getGeneralVariants() {
        Set<String> variants = new HashSet<>();
        variants.addAll(getInferredMutations());
        variants.addAll(getStructuralAlterations());
        variants.addAll(getSpecialVariant());
        return variants;
    }

    public static Set<String> getInferredMutations() {
        Set<String> variants = new HashSet<>();
        for (InferredMutation inferredMutation : InferredMutation.values()) {
            variants.add(inferredMutation.getVariant());
        }
        return variants;
    }

    public static Set<String> getStructuralAlterations() {
        Set<String> variants = new HashSet<>();
        for (StructuralAlteration structuralAlteration : StructuralAlteration.values()) {
            variants.add(structuralAlteration.getVariant());
        }
        return variants;
    }

    public static boolean isPositionedAlteration(Alteration alteration) {
        boolean isPositionVariant = false;
        if (alteration != null
            && alteration.getProteinStart() != null
            && alteration.getProteinEnd() != null
            && alteration.getProteinStart().equals(alteration.getProteinEnd())
            && alteration.getRefResidues() != null && alteration.getRefResidues().length() == 1
            && alteration.getVariantResidues() == null
            && alteration.getConsequence() != null
            && alteration.getConsequence().getTerm().equals("NA")
            )
            isPositionVariant = true;
        return isPositionVariant;
    }

    private static Set<String> getSpecialVariant() {
        Set<String> variants = new HashSet<>();
        for (SpecialVariant variant : SpecialVariant.values()) {
            variants.add(variant.getVariant());
        }
        return variants;
    }


    public static boolean isGeneralAlterations(String variant) {
        boolean is = false;
        Set<String> generalAlterations = getGeneralVariants();
        for (String generalAlteration : generalAlterations) {
            if (generalAlteration.toLowerCase().equals(variant.toLowerCase())) {
                is = true;
                break;
            }
        }
        return is;
    }

    public static Boolean isGeneralAlterations(String mutationStr, Boolean exactMatch) {
        exactMatch = exactMatch || false;
        if (exactMatch) {
            return MainUtils.containsCaseInsensitive(mutationStr, getGeneralVariants());
        } else if (stringContainsItemFromSet(mutationStr, getGeneralVariants())
            && itemFromSetAtEndString(mutationStr, getGeneralVariants())) {
            return true;
        }
        return false;
    }

    public static String toString(Collection<Alteration> relevantAlterations) {
        List<String> names = new ArrayList<>();
        for (Alteration alteration : relevantAlterations) {
            names.add(alteration.getAlteration());
        }
        return MainUtils.listToString(names, ", ");
    }

    private static boolean stringContainsItemFromSet(String inputString, Set<String> items) {
        for (String item : items) {
            if (StringUtils.containsIgnoreCase(inputString, item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean itemFromSetAtEndString(String inputString, Set<String> items) {
        for (String item : items) {
            if (StringUtils.endsWithIgnoreCase(inputString, item)) {
                return true;
            }
        }
        return false;
    }
}
