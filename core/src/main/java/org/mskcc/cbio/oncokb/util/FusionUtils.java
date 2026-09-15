package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.FusionSeparatorStatus;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FusionUtils {
    public final static String FUSION_SEPARATOR = "::";
    public final static String FUSION_LEGACY_SEPARATOR = "-";
    private final static String FUSION_REGEX = "\\s*([\\w-]*)" + FUSION_SEPARATOR + "([\\w-]*)\\s*(?i)(fusion)?\\s*";
    private final static String FUSION_ALT_REGEX = "\\s*((\\w*)" + FUSION_LEGACY_SEPARATOR + "(\\w*))\\s+(?i)fusion\\s*";

    // Groups: 1 gene partner, 2 fusion keyword.
    private final static Pattern FUSION_KEYWORD_PATTERN =
        Pattern.compile("\\s*(.*?)(?:\\s+(?i)(fusions?))?\\s*", Pattern.DOTALL);

    public static List<String> getGenesStrs(String query) {
        Set<String> geneStrsList = new LinkedHashSet<>();
        if (!StringUtils.isEmpty(query)) {
            String fusionSeparator = query.contains(FUSION_SEPARATOR) ? FUSION_SEPARATOR : FUSION_LEGACY_SEPARATOR;
            List<String> geneFragments = Arrays.asList(query.split(fusionSeparator));
            if (geneFragments.size() > 2) {
                String rightHandGene = StringUtils.join(geneFragments.subList(1, geneFragments.size()), fusionSeparator);
                if (GeneUtils.getGeneByHugoSymbol(rightHandGene) != null) {
                    geneStrsList.add(rightHandGene);
                    geneStrsList.add(geneFragments.get(0));
                }
                String leftHandGene = StringUtils.join(geneFragments.subList(0, geneFragments.size() - 1), fusionSeparator);
                if (GeneUtils.getGeneByHugoSymbol(leftHandGene) != null) {
                    geneStrsList.add(leftHandGene);
                    geneStrsList.add(geneFragments.get(geneFragments.size() - 1));
                }

                if (geneStrsList.size() == 0) {
                    if (GeneUtils.getGeneByHugoSymbol(geneFragments.get(0)) != null) {
                        geneStrsList.add(geneFragments.get(0));
                        geneStrsList.add(rightHandGene);
                    }
                    if (GeneUtils.getGeneByHugoSymbol(geneFragments.get(geneFragments.size() - 1)) != null) {
                        geneStrsList.add(geneFragments.get(geneFragments.size() - 1));
                        geneStrsList.add(leftHandGene);
                    }
                }
            } else if (geneFragments.size() == 2) {
                if (GeneUtils.getGeneByHugoSymbol(query) != null) {
                    geneStrsList.add(query);
                } else {
                    geneStrsList.addAll(geneFragments);
                }
            } else {
                geneStrsList.addAll(geneFragments);
            }
        }
        return new ArrayList<>(geneStrsList);
    }

    public static String getFusionName(Gene geneA, Gene geneB) {
        if (geneA == null || geneB == null) {
            return "";
        }
        List<String> geneANames = new ArrayList<>();
        List<String> geneBNames = new ArrayList<>();
        geneANames.add(geneA.getHugoSymbol());
        geneANames.addAll(geneA.getGeneAliases());
        geneBNames.add(geneB.getHugoSymbol());
        geneBNames.addAll(geneB.getGeneAliases());

        Alteration matchedAlteration = null;
        String fusionName = null;
        String fusionAltName = null;

        for (String hugoA : geneANames) {
            for (String hugoB : geneBNames) {
                fusionName = getFusionName(hugoA, hugoB);
                fusionAltName = getFusionAlterationName(hugoA, hugoB);
                matchedAlteration = findAltByFusionName(fusionAltName, geneA, geneB);
                if (matchedAlteration != null) {
                    return fusionName;
                }
                matchedAlteration = findAltByFusionName(fusionName, geneA, geneB);
                if (matchedAlteration != null) {
                    return fusionName;
                }

                fusionName = getFusionName(hugoB, hugoA);
                fusionAltName = getFusionAlterationName(hugoB, hugoA);
                matchedAlteration = findAltByFusionName(fusionAltName, geneA, geneB);
                if (matchedAlteration != null) {
                    return fusionName;
                }
                matchedAlteration = findAltByFusionName(fusionName, geneA, geneB);
                if (matchedAlteration != null) {
                    return fusionName;
                }
            }
        }
        if (matchedAlteration == null) {
            fusionName = getFusionName(geneA.getHugoSymbol(), geneB.getHugoSymbol());
        }
        return fusionName;
    }

    private static Alteration findAltByFusionName(String fusionName, Gene geneA, Gene geneB) {
        Alteration matchedAlteration = null;
        matchedAlteration = AlterationUtils.findAlteration(geneA, ReferenceGenome.GRCh37, fusionName, false);
        if (matchedAlteration == null) {
            matchedAlteration = AlterationUtils.findAlteration(geneB, ReferenceGenome.GRCh37, fusionName, false);
        }
        return matchedAlteration;
    }

    private static String getFusionName(String hugoA, String hugoB) {
        return hugoA + FUSION_SEPARATOR + hugoB;
    }

    private static String getFusionAlterationName(String hugoA, String hugoB) {
        return hugoA + FUSION_SEPARATOR + hugoB + " Fusion";
    }

    /**
     * Rewrites a queried fusion name onto the HGVS {@code ::} separator, which is what OncoKB curates
     * fusions under. A name written with a single legacy hyphen is rewritten. HUGO symbols may themselves
     * contain hyphens (H1-4, HLA-A), so a name written with more than one hyphen is rewritten only when
     * exactly one hyphen splits it into two known gene symbols (H1-4-BRAF Fusion); otherwise it is left
     * alone and reported as {@link FusionSeparatorStatus#AMBIGUOUS}.
     *
     * <p>Only names carrying the trailing "Fusion"/"Fusions" keyword are considered, so an ordinary
     * protein change that happens to contain a hyphen is never touched. A gene part that is itself a
     * HUGO symbol (H1-4 Fusion, COX10-AS1 Fusion) is left alone as well, whether or not OncoKB curates
     * that gene.
     */
    public static FusionNameNormalization normalizeSeparator(String alteration) {
        if (StringUtils.isEmpty(alteration)) {
            return new FusionNameNormalization(alteration, FusionSeparatorStatus.NOT_APPLICABLE);
        }
        Matcher matcher = FUSION_KEYWORD_PATTERN.matcher(alteration);
        if (!matcher.matches() || matcher.group(2) == null) {
            return new FusionNameNormalization(alteration, FusionSeparatorStatus.NOT_APPLICABLE);
        }
        String genePart = matcher.group(1);
        String keyword = matcher.group(2);
        if (genePart.contains(FUSION_SEPARATOR)) {
            return new FusionNameNormalization(alteration, FusionSeparatorStatus.HGVS);
        }
        if (GeneUtils.isKnownGeneSymbol(genePart)) {
            // Prevent a gene that is not in oncokb core to be split into two genes.
            // For example COX10-AS1 should not be split into COX10::AS1.
            return new FusionNameNormalization(alteration, FusionSeparatorStatus.NOT_APPLICABLE);
        }
        int hyphens = StringUtils.countMatches(genePart, FUSION_LEGACY_SEPARATOR);
        if (hyphens == 0) {
            return new FusionNameNormalization(alteration, FusionSeparatorStatus.NOT_APPLICABLE);
        }
        if (hyphens > 1) {
            String[] partners = findOnlyGenePartnerSplit(genePart);
            if (partners == null) {
                return new FusionNameNormalization(alteration, FusionSeparatorStatus.AMBIGUOUS);
            }
            return new FusionNameNormalization(
                getFusionName(partners[0], partners[1]) + " " + keyword,
                FusionSeparatorStatus.NORMALIZED);
        }
        return new FusionNameNormalization(
            genePart.replace(FUSION_LEGACY_SEPARATOR, FUSION_SEPARATOR) + " " + keyword,
            FusionSeparatorStatus.NORMALIZED);
    }

    /**
     * Tries every hyphen in the gene part as the separator and returns the partners when exactly one
     * split leaves a known gene symbol on both sides. Returns null when no split or more than 1.
     */
    private static String[] findOnlyGenePartnerSplit(String genePart) {
        String[] match = null;
        int index = genePart.indexOf(FUSION_LEGACY_SEPARATOR);
        while (index != -1) {
            String geneA = genePart.substring(0, index);
            String geneB = genePart.substring(index + FUSION_LEGACY_SEPARATOR.length());
            if (GeneUtils.isKnownGeneSymbol(geneA) && GeneUtils.isKnownGeneSymbol(geneB)) {
                if (match != null) {
                    return null;
                }
                match = new String[]{geneA, geneB};
            }
            index = genePart.indexOf(FUSION_LEGACY_SEPARATOR, index + 1);
        }
        return match;
    }

    /** The outcome of {@link #normalizeSeparator(String)}: the name to annotate and what was done to it. */
    public static final class FusionNameNormalization {
        private final String name;
        private final FusionSeparatorStatus status;

        FusionNameNormalization(String name, FusionSeparatorStatus status) {
            this.name = name;
            this.status = status;
        }

        /** The fusion name to annotate: rewritten when normalized, the queried name otherwise. */
        public String getName() {
            return name;
        }

        public FusionSeparatorStatus getStatus() {
            return status;
        }

        public boolean isNormalized() {
            return FusionSeparatorStatus.NORMALIZED.equals(status);
        }

        public boolean isAmbiguous() {
            return FusionSeparatorStatus.AMBIGUOUS.equals(status);
        }
    }

    public static Boolean isFusion(String variant) {
        if (!StringUtils.isEmpty(variant)) {
            if (variant.toLowerCase().equals("fusion") || variant.toLowerCase().equals("fusions")) {
                return true;
            }
            if ((Pattern.matches(FUSION_REGEX, variant) || Pattern.matches(FUSION_ALT_REGEX, variant))) {
                return true;
            }
        }
        return false;
    }

    public static String getRevertFusionName(String fusionName) {
        String revertFusionAltStr = "";
        Pattern pattern = Pattern.compile(FUSION_REGEX);
        Matcher matcher = pattern.matcher(fusionName);
        if (matcher.matches() && matcher.groupCount() == 3) {
            // Revert fusion. The reverted name is looked up in the alteration table as-is, so a name
            // that carried the "Fusion" keyword keeps it, spelled the way the table spells it.
            String geneA = matcher.group(1);
            String geneB = matcher.group(2);
            revertFusionAltStr = matcher.group(3) == null
                ? getFusionName(geneB, geneA)
                : getFusionAlterationName(geneB, geneA);
        } else {
            pattern = Pattern.compile(FUSION_ALT_REGEX);
            matcher = pattern.matcher(fusionName);
            if (matcher.matches() && matcher.groupCount() == 3) {
                // Revert fusion
                String geneA = matcher.group(2);
                String geneB = matcher.group(3);
                revertFusionAltStr = getFusionAlterationName(geneB, geneA);
            }
        }
        return revertFusionAltStr;
    }
}
