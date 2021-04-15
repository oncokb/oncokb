package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Hongxin Zhang on 8/23/17.
 */
public class FusionUtils {
    public static List<String> getGenesStrs(String query) {
        Set<String> geneStrsList = new LinkedHashSet<>();
        if (!StringUtils.isNullOrEmpty(query)) {
            List<String> geneFragments = Arrays.asList(query.split("-"));
            if (geneFragments.size() > 2) {
                String rightHandGene = org.apache.commons.lang3.StringUtils.join(geneFragments.subList(1, geneFragments.size()), "-");
                if (GeneUtils.getGeneByHugoSymbol(rightHandGene) != null) {
                    geneStrsList.add(rightHandGene);
                    geneStrsList.add(geneFragments.get(0));
                }
                String leftHandGene = org.apache.commons.lang3.StringUtils.join(geneFragments.subList(0, geneFragments.size() - 1), "-");
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

    public static List<Gene> getGenes(String query) {
        List<Gene> genes = new ArrayList<>();
        for (String geneStr : getGenesStrs(query)) {
            Gene tmpGene = GeneUtils.getGeneByHugoSymbol(geneStr);
            if (tmpGene != null && !genes.contains(tmpGene)) {
                genes.add(tmpGene);
            }
        }
        return genes;
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

        for (String hugoA : geneANames) {
            for (String hugoB : geneBNames) {
                fusionName = getFusionName(hugoA, hugoB);
                matchedAlteration = AlterationUtils.findAlteration(geneA, ReferenceGenome.GRCh37, fusionName);
                if (matchedAlteration != null) {
                    return fusionName;
                }

                fusionName = getFusionName(hugoA, hugoB);
                matchedAlteration = AlterationUtils.findAlteration(geneB, ReferenceGenome.GRCh37, fusionName);
                if (matchedAlteration != null) {
                    return fusionName;
                }

                fusionName = getFusionName(hugoB, hugoA);
                matchedAlteration = AlterationUtils.findAlteration(geneA, ReferenceGenome.GRCh37, fusionName);
                if (matchedAlteration != null) {
                    return fusionName;
                }
                fusionName = getFusionName(hugoB, hugoA);
                matchedAlteration = AlterationUtils.findAlteration(geneB, ReferenceGenome.GRCh37, fusionName);
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

    private static String getFusionName(String hugoA, String hugoB) {
        return hugoA + "-" + hugoB + " Fusion";
    }
}
