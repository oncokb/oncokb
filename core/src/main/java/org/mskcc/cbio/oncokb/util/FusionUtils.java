package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.model.Gene;

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
}
