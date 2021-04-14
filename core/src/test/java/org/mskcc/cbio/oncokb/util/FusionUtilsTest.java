package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.Gene;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mskcc.cbio.oncokb.util.FusionUtils.*;

/**
 * Created by Hongxin Zhang on 4/12/21.
 */
public class FusionUtilsTest extends TestCase {

    public void testGetGenesStrs() {
        List<String> genes = getGenesStrs("H1-4");
        assertEquals(1, genes.size());
        assertEquals("H1-4", genes.get(0));

        genes = getGenesStrs("HIST1H2BD-HIST1H1E");
        assertEquals(2, genes.size());
        assertTrue(genes.stream().filter(gene -> gene.equals("HIST1H2BD")).findAny().isPresent());
        assertTrue(genes.stream().filter(gene -> gene.equals("HIST1H1E")).findAny().isPresent());

        genes = getGenesStrs("H2BC5-H1-4");
        assertEquals(2, genes.size());
        assertTrue(genes.stream().filter(gene -> gene.equals("H2BC5")).findAny().isPresent());
        assertTrue(genes.stream().filter(gene -> gene.equals("H1-4")).findAny().isPresent());

        genes = getGenesStrs("H1-4-H2BC5");
        assertEquals(2, genes.size());
        assertTrue(genes.stream().filter(gene -> gene.equals("H2BC5")).findAny().isPresent());
        assertTrue(genes.stream().filter(gene -> gene.equals("H1-4")).findAny().isPresent());

        genes = getGenesStrs("H1-H2BC5-4");
        assertEquals(2, genes.size());
        assertTrue(genes.stream().filter(gene -> gene.equals("H2BC5-4")).findAny().isPresent());
        assertTrue(genes.stream().filter(gene -> gene.equals("H1")).findAny().isPresent());
    }

    public void testGetGenes() {
        List<Gene> genes = getGenes("H1-4");
        assertEquals(1, genes.size());
        assertEquals("H1-4", genes.get(0).getHugoSymbol());

        genes = getGenes("HIST1H2BD-HIST1H1E");
        assertEquals(2, genes.size());
        assertTrue(genes.stream().filter(gene -> gene.getHugoSymbol().equals("H1-4")).findAny().isPresent());
        assertTrue(genes.stream().filter(gene -> gene.getHugoSymbol().equals("H2BC5")).findAny().isPresent());

        genes = getGenes("H2BC5-H1-4");
        assertEquals(2, genes.size());
        assertTrue(genes.stream().filter(gene -> gene.getHugoSymbol().equals("H2BC5")).findAny().isPresent());
        assertTrue(genes.stream().filter(gene -> gene.getHugoSymbol().equals("H1-4")).findAny().isPresent());

        genes = getGenes("H1-4-H2BC5");
        assertEquals(2, genes.size());
        assertTrue(genes.stream().filter(gene -> gene.getHugoSymbol().equals("H2BC5")).findAny().isPresent());
        assertTrue(genes.stream().filter(gene -> gene.getHugoSymbol().equals("H1-4")).findAny().isPresent());

        genes = getGenes("H1-H2BC5-4");
        assertEquals(1, genes.size());
        assertEquals("H1-5", genes.get(0).getHugoSymbol());
    }

    public void testGetFusionName() {

        Gene geneA = new Gene();
        geneA.setEntrezGeneId(346288);
        geneA.setHugoSymbol("SEPTIN14");
        geneA.setGeneAliases(new HashSet<>(Arrays.asList("SEPT14")));

        Gene geneB = new Gene();
        geneB.setEntrezGeneId(1956);
        geneB.setHugoSymbol("EGFR");
        geneB.setGeneAliases(new HashSet<>(Arrays.asList("ERBB1")));


        String fusionName = FusionUtils.getFusionName(geneA, geneB);
        assertEquals("EGFR-SEPT14 Fusion", fusionName);

        fusionName = FusionUtils.getFusionName(geneA, null);
        assertEquals("", fusionName);

        fusionName = FusionUtils.getFusionName(null, geneB);
        assertEquals("", fusionName);
    }
}
