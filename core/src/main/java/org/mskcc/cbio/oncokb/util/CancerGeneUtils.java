package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.mskcc.cbio.oncokb.model.CancerGene;
import org.mskcc.cbio.oncokb.model.Gene;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiaojiao on 6/9/17.
 */
public class CancerGeneUtils {
    private static final String MSKIMPACT_GENES = "msk_impact_505_genes.csv";
    private static final String MSKIMPACT_HEME_GENES = "msk_impact_heme_400_heme_pact_v4_575_genes.csv";
    private static final String FOUNDATION_GENES = "foundation_one_cdx_324_genes.csv";
    private static final String FOUNDATION_HEME_GENES = "foundation_one_heme_593_genes.csv";
    private static final String VOGELSTEIN_GENES = "vogelstein_125_genes.csv";
    private static final String CGC_GENES = "cancer_gene_census_v99_tier_1_only.csv";

    public static List<CancerGene> getCancerGeneList() throws IOException {
        return CacheUtils.getCancerGeneList();
    }

    public static List<CancerGene> populateCancerGeneList() throws IOException {
        Set<CancerGene> cancerGenes = new HashSet<>();

        // We need to include all annotated genes
        Set<Gene> allAnnotatedGenes = CacheUtils.getAllGenes();
        allAnnotatedGenes
            .stream()
            .filter(gene -> gene.getEntrezGeneId() > 0)
            .forEach(gene -> {
                CancerGene cancerGene = new CancerGene();
                cancerGene.setEntrezGeneId(gene.getEntrezGeneId());
                cancerGene.setHugoSymbol(gene.getHugoSymbol());
                cancerGene.setGrch37Isoform(gene.getGrch37Isoform());
                cancerGene.setGrch37RefSeq(gene.getGrch37RefSeq());
                cancerGene.setGrch38Isoform(gene.getGrch38Isoform());
                cancerGene.setGrch38RefSeq(gene.getGrch38RefSeq());
                cancerGene.setOncokbAnnotated(true);
                cancerGene.setOccurrenceCount(1);
                cancerGene.setOncogene(gene.getOncogene());
                cancerGene.setTSG(gene.getTSG());
                cancerGene.setGeneAliases(gene.getGeneAliases());
                cancerGenes.add(cancerGene);
            });

        for (Gene gene : getGenes(MSKIMPACT_GENES)) {
            Optional<CancerGene> match = cancerGenes.stream().filter(cancerGene -> cancerGene.getEntrezGeneId().equals(gene.getEntrezGeneId())).findFirst();
            if (match.isPresent()) {
                match.get().setmSKImpact(true);
                match.get().setOccurrenceCount(match.get().getOccurrenceCount() + 1);
            } else {
                CancerGene cancerGene = new CancerGene();
                cancerGene.setHugoSymbol(gene.getHugoSymbol());
                cancerGene.setEntrezGeneId(gene.getEntrezGeneId());
                cancerGene.setOccurrenceCount(1);
                cancerGene.setmSKImpact(true);
                cancerGenes.add(cancerGene);
            }
        }

        for (Gene gene : getGenes(MSKIMPACT_HEME_GENES)) {
            Optional<CancerGene> match = cancerGenes.stream().filter(cancerGene -> cancerGene.getEntrezGeneId().equals(gene.getEntrezGeneId())).findFirst();
            if (match.isPresent()) {
                match.get().setmSKHeme(true);
                match.get().setOccurrenceCount(match.get().getOccurrenceCount() + 1);
            } else {
                CancerGene cancerGene = new CancerGene();
                cancerGene.setHugoSymbol(gene.getHugoSymbol());
                cancerGene.setEntrezGeneId(gene.getEntrezGeneId());
                cancerGene.setOccurrenceCount(1);
                cancerGene.setmSKHeme(true);
                cancerGenes.add(cancerGene);
            }
        }

        for (Gene gene : getGenes(FOUNDATION_GENES)) {
            Optional<CancerGene> match = cancerGenes.stream().filter(cancerGene -> cancerGene.getEntrezGeneId().equals(gene.getEntrezGeneId())).findFirst();
            if (match.isPresent()) {
                match.get().setFoundation(true);
                match.get().setOccurrenceCount(match.get().getOccurrenceCount() + 1);
            } else {
                CancerGene cancerGene = new CancerGene();
                cancerGene.setHugoSymbol(gene.getHugoSymbol());
                cancerGene.setEntrezGeneId(gene.getEntrezGeneId());
                cancerGene.setOccurrenceCount(1);
                cancerGene.setFoundation(true);
                cancerGenes.add(cancerGene);
            }
        }

        for (Gene gene : getGenes(FOUNDATION_HEME_GENES)) {
            Optional<CancerGene> match = cancerGenes.stream().filter(cancerGene -> cancerGene.getEntrezGeneId().equals(gene.getEntrezGeneId())).findFirst();
            if (match.isPresent()) {
                match.get().setFoundationHeme(true);
                match.get().setOccurrenceCount(match.get().getOccurrenceCount() + 1);
            } else {
                CancerGene cancerGene = new CancerGene();
                cancerGene.setHugoSymbol(gene.getHugoSymbol());
                cancerGene.setEntrezGeneId(gene.getEntrezGeneId());
                cancerGene.setOccurrenceCount(1);
                cancerGene.setFoundationHeme(true);
                cancerGenes.add(cancerGene);
            }
        }

        for (Gene gene : getGenes(VOGELSTEIN_GENES)) {
            Optional<CancerGene> match = cancerGenes.stream().filter(cancerGene -> cancerGene.getEntrezGeneId().equals(gene.getEntrezGeneId())).findFirst();
            if (match.isPresent()) {
                match.get().setVogelstein(true);
                match.get().setOccurrenceCount(match.get().getOccurrenceCount() + 1);
            } else {
                CancerGene cancerGene = new CancerGene();
                cancerGene.setHugoSymbol(gene.getHugoSymbol());
                cancerGene.setEntrezGeneId(gene.getEntrezGeneId());
                cancerGene.setOccurrenceCount(1);
                cancerGene.setVogelstein(true);
                cancerGenes.add(cancerGene);
            }
        }

        for (Gene gene : getGenes(CGC_GENES)) {
            Optional<CancerGene> match = cancerGenes.stream().filter(cancerGene -> cancerGene.getEntrezGeneId().equals(gene.getEntrezGeneId())).findFirst();
            if (match.isPresent()) {
                match.get().setSangerCGC(true);
                match.get().setOccurrenceCount(match.get().getOccurrenceCount() + 1);
            } else {
                CancerGene cancerGene = new CancerGene();
                cancerGene.setHugoSymbol(gene.getHugoSymbol());
                cancerGene.setEntrezGeneId(gene.getEntrezGeneId());
                cancerGene.setOccurrenceCount(1);
                cancerGene.setSangerCGC(true);
                cancerGenes.add(cancerGene);
            }
        }

        return cancerGenes.stream().sorted((Comparator.comparing(CancerGene::getOccurrenceCount).reversed().thenComparing(CancerGene::getHugoSymbol))).collect(Collectors.toList());
    }

    private static Set<Gene> getGenes(String file) throws IOException {
            List<String> lines = FileUtils.readTrimedLinesStream(
                CancerGeneUtils.class.getResourceAsStream("/data/cancerGenes/" + file));

            Iterator itr = lines.iterator();
            Set<Gene> allGenes = new HashSet<>();
            // Skip the first line
            itr.next();
            while (itr.hasNext()) {
                String line = itr.next().toString().trim();

                String[] items = line.split(",");
                if (items.length != 2) {
                    System.out.println("ERROR line format: " + line);
                    continue;
                }

                String hugoSymbol = items[0].trim().replaceAll("\"", "");
                Integer entrezGeneId = Integer.parseInt(items[1].trim());

                if (entrezGeneId != null && hugoSymbol != null) {
                    Gene gene = new Gene();
                    gene.setHugoSymbol(hugoSymbol);
                    gene.setEntrezGeneId(entrezGeneId);
                    allGenes.add(gene);
                } else {
                    System.out.println("ERROR line content: " + items[0] + " " + items[1]);
                }
            }
            return allGenes;
    }
}
