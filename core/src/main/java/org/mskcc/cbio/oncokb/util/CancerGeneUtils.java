package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.mskcc.cbio.oncokb.model.CancerGene;
import org.mskcc.cbio.oncokb.model.Gene;

import java.io.IOException;
import java.util.*;

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
                Set<String> allHugoSymbolsFromFile = new HashSet<>();

                while (itr.hasNext()) {
                    String line = itr.next().toString().trim();
                    // skip comments
                    if (line.startsWith("#")) {
                        continue;
                    }

                    String[] items = line.split("\t");
                    if (items.length != 9) continue;

                    String hugoSymbol = items[0];
                    allHugoSymbolsFromFile.add(hugoSymbol);

                    Gene gene = GeneUtils.getGeneByEntrezId(Integer.parseInt(items[1]));
                    CancerGene cancerGene = new CancerGene();
                    cancerGene.setEntrezGeneId(Integer.parseInt(items[1]));

                    if (gene == null) {
                        cancerGene.setHugoSymbol(hugoSymbol);
                    } else {
                        if (!gene.getHugoSymbol().equals(hugoSymbol)) {
                            System.out.println("The gene hugo does not match, expect " + gene.getHugoSymbol() + ", but got: " + hugoSymbol);
                        }
                        cancerGene.setHugoSymbol(gene.getHugoSymbol());
                        cancerGene.setOncokbAnnotated(true);
                        cancerGene.setOncogene(gene.getOncogene());
                        cancerGene.setTSG(gene.getTSG());
                    }
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

                // We also need to include genes that not in the initial list
                Set<Gene> allAnnotatedGenes = GeneUtils.getAllGenes();
                allAnnotatedGenes
                    .stream()
                    .filter(gene -> !allHugoSymbolsFromFile.contains(gene.getHugoSymbol()))
                    .forEach(gene -> {
                        CancerGene cancerGene = new CancerGene();
                        cancerGene.setEntrezGeneId(gene.getEntrezGeneId());
                        cancerGene.setHugoSymbol(gene.getHugoSymbol());
                        cancerGene.setOncokbAnnotated(true);
                        cancerGene.setOccurrenceCount(1);
                        cancerGene.setOncogene(gene.getOncogene());
                        cancerGene.setTSG(gene.getTSG());
                        CancerGeneList.add(cancerGene);
                    });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return CancerGeneList;
    }
}
