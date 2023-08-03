package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FusionUtils {
    public final static String FUSION_SEPARATOR = "::";
    public final static String FUSION_ALTERNATIVE_SEPARATOR = "-";
    private final static String FUSION_REGEX = "\\s*(\\w*)" + FUSION_SEPARATOR + "(\\w*)\\s*(?i)(fusion)?\\s*";
    private final static String FUSION_ALT_REGEX = "\\s*((\\w*)" + FUSION_ALTERNATIVE_SEPARATOR + "(\\w*))\\s+(?i)fusion\\s*";

    public static List<String> getGenesStrs(String query) {
        Set<String> geneStrsList = new LinkedHashSet<>();
        if (!StringUtils.isNullOrEmpty(query)) {
            String fusionSeparator = query.contains(FUSION_SEPARATOR) ? FUSION_SEPARATOR : FUSION_ALTERNATIVE_SEPARATOR;
            List<String> geneFragments = Arrays.asList(query.split(fusionSeparator));
            if (geneFragments.size() > 2) {
                String rightHandGene = org.apache.commons.lang3.StringUtils.join(geneFragments.subList(1, geneFragments.size()), fusionSeparator);
                if (GeneUtils.getGeneByHugoSymbol(rightHandGene) != null) {
                    geneStrsList.add(rightHandGene);
                    geneStrsList.add(geneFragments.get(0));
                }
                String leftHandGene = org.apache.commons.lang3.StringUtils.join(geneFragments.subList(0, geneFragments.size() - 1), fusionSeparator);
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
        matchedAlteration = AlterationUtils.findAlteration(geneA, ReferenceGenome.GRCh37, fusionName);
        if (matchedAlteration == null) {
            matchedAlteration = AlterationUtils.findAlteration(geneB, ReferenceGenome.GRCh37, fusionName);
        }
        return matchedAlteration;
    }

    private static String getFusionName(String hugoA, String hugoB) {
        return hugoA + FUSION_SEPARATOR + hugoB;
    }

    // This is used to find fusion in the alteration table
    private static String getFusionAlterationName(String hugoA, String hugoB) {
        return hugoA + FUSION_ALTERNATIVE_SEPARATOR + hugoB + " Fusion";
    }

    public static Boolean isFusion(String variant) {
        if (!StringUtils.isNullOrEmpty(variant)) {
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
            // Revert fusion
            String geneA = matcher.group(1);
            String geneB = matcher.group(2);
            revertFusionAltStr = getFusionName(geneB, geneA);
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
