package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Hongxin Zhang on 8/23/17.
 */
public class FusionUtils {
    public static List<Gene> getGenes(String query) {
        List<Gene> genes = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(query)) {
            List<String> geneStrsList = Arrays.asList(query.split("-"));

            for (String geneStr : geneStrsList) {
                Gene tmpGene = GeneUtils.getGeneByHugoSymbol(geneStr);
                if (tmpGene != null && !genes.contains(tmpGene)) {
                    genes.add(tmpGene);
                }
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
