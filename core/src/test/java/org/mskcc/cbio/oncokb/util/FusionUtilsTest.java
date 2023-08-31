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

    }

    public void testIsFusion() {
        assertTrue(isFusion("fusion"));
        assertTrue(isFusion("Fusion"));
        assertTrue(isFusion("fusions"));
        assertTrue(isFusion("Fusions"));
        assertTrue(isFusion("A-B fusion"));
        assertTrue(isFusion("A-B fusion "));
        assertTrue(isFusion("A-B  fusion "));
        assertTrue(isFusion(" A-B  fusion "));
        assertTrue(isFusion("A::B"));
        assertTrue(isFusion("A::B fusion"));

        assertFalse(isFusion("A-B"));
    }

    public void testRevertFusionName() {
        assertEquals("B-A Fusion", getRevertFusionName("A-B fusion"));
        assertEquals("B-A Fusion", getRevertFusionName("A-B fusion "));
        assertEquals("B-A Fusion", getRevertFusionName("A-B  fusion "));
        assertEquals("B-A Fusion", getRevertFusionName(" A-B  fusion "));
        assertEquals("B::A", getRevertFusionName("A::B"));
        assertEquals("B::A", getRevertFusionName("A::B fusion"));
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
        assertEquals("EGFR::SEPT14", fusionName);

        geneA = new Gene();
        geneA.setEntrezGeneId(23175);
        geneA.setHugoSymbol("LPIN1");

        geneB = new Gene();
        geneB.setEntrezGeneId(238);
        geneB.setHugoSymbol("ALK");

        fusionName = FusionUtils.getFusionName(geneA, geneB);
        assertEquals("LPIN1::ALK", fusionName);

        fusionName = FusionUtils.getFusionName(geneA, null);
        assertEquals("", fusionName);

        fusionName = FusionUtils.getFusionName(null, geneB);
        assertEquals("", fusionName);
    }
}
