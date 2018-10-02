package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.mskcc.cbio.oncokb.model.CancerGene;
import org.mskcc.cbio.oncokb.model.Gene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jiaojiao on 6/9/17.
 */
public class CancerGeneUtils {
    private static final String CANCER_GENE_FILE_PATH = "/data/cancer-gene.txt";
    private static List<CancerGene> CancerGeneList;

    public static List<CancerGene> getCancerGeneList() {
        if (CancerGeneList == null) {
            CancerGeneList = new ArrayList<>();
            try {
                List<String> lines = FileUtils.readTrimedLinesStream(
                    CancerGeneUtils.class.getResourceAsStream(CANCER_GENE_FILE_PATH));
                Iterator itr = lines.iterator();

                while (itr.hasNext()) {
                    String line = itr.next().toString().trim();
                    // skip comments
                    if (line.startsWith("#")) {
                        continue;
                    }

                    String[] items = line.split("\t");
                    if (items.length != 9) continue;

                    CancerGene cancerGene = new CancerGene();
                    cancerGene.setHugoSymbol(items[0]);
                    cancerGene.setEntrezGeneId(items[1]);
                    Gene gene = GeneUtils.getGeneByEntrezId(Integer.parseInt(items[1]));
                    cancerGene.setOncokbAnnotated(gene == null ? false : true);
                    int occurence = NumberUtils.isNumber(items[2].trim()) ? Integer.parseInt(items[2].trim()) : 0;
                    if (cancerGene.getOncokbAnnotated()) {
                        occurence++;
                    }
                    cancerGene.setOccurrenceCount(occurence);
                    cancerGene.setmSKImpact(items[3].trim().equals("1"));
                    cancerGene.setmSKHeme(items[4].trim().equals("1"));
                    cancerGene.setFoundation(items[5].trim().equals("1"));
                    cancerGene.setFoundationHeme(items[6].trim().equals("1"));
                    cancerGene.setVogelstein(items[7].trim().equals("1"));
                    cancerGene.setSangerCGC(items[8].trim().equals("1"));
                    CancerGeneList.add(cancerGene);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return CancerGeneList;
    }
}
