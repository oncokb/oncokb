package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.FusionSeparatorStatus;
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

        // The HGVS separator says the same thing without the guessing
        genes = getGenesStrs("H1-4::H2BC5");
        assertEquals(2, genes.size());
        assertTrue(genes.stream().filter(gene -> gene.equals("H2BC5")).findAny().isPresent());
        assertTrue(genes.stream().filter(gene -> gene.equals("H1-4")).findAny().isPresent());
    }

    public void testNormalizeSeparator() {
        // A single hyphen is unambiguous and is rewritten onto the HGVS separator
        assertEquals("A::B Fusion", normalizeSeparator("A-B Fusion").getName());
        assertEquals(FusionSeparatorStatus.NORMALIZED, normalizeSeparator("A-B Fusion").getStatus());
        assertEquals("A::B fusion", normalizeSeparator("A-B fusion").getName());
        assertEquals("A::B fusion", normalizeSeparator(" A-B  fusion ").getName());
        assertEquals("A::B Fusions", normalizeSeparator("A-B Fusions").getName());

        // A name already using the HGVS separator is left alone, hyphenated gene symbols and all
        assertEquals(FusionSeparatorStatus.HGVS, normalizeSeparator("A::B Fusion").getStatus());
        assertEquals("H1-4::H2BC5 Fusion", normalizeSeparator("H1-4::H2BC5 Fusion").getName());
        assertEquals(FusionSeparatorStatus.HGVS, normalizeSeparator("H1-4::H2BC5 Fusion").getStatus());

        // More than one hyphen: which one separates the partners cannot be determined
        assertEquals(FusionSeparatorStatus.AMBIGUOUS, normalizeSeparator("H1-4-H2BC5 Fusion").getStatus());
        assertEquals("H1-4-H2BC5 Fusion", normalizeSeparator("H1-4-H2BC5 Fusion").getName());
        assertTrue(normalizeSeparator("A-B-C Fusion").isAmbiguous());

        // A single gene whose symbol contains a hyphen has nothing to separate
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("H1-4 Fusion").getStatus());
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("NKX2-1 Fusion").getStatus());

        // The same holds for a gene OncoKB does not curate. These are absent from the curated gene table
        // but are real symbols, and splitting them yields partners that do not exist.
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("COX10-AS1 Fusion").getStatus());
        assertEquals("COX10-AS1 Fusion", normalizeSeparator("COX10-AS1 Fusion").getName());
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("HLA-DRB1 Fusion").getStatus());
        assertEquals("HLA-DRB1 Fusion", normalizeSeparator("HLA-DRB1 Fusion").getName());
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("SOX2-OT Fusion").getStatus());
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("KCNMB2-AS1 Fusion").getStatus());

        // A hyphenated pair of genuine partners is still rewritten
        assertEquals("BCR::ABL1 Fusion", normalizeSeparator("BCR-ABL1 Fusion").getName());
        assertEquals(FusionSeparatorStatus.NORMALIZED, normalizeSeparator("BCR-ABL1 Fusion").getStatus());

        // Without the fusion keyword there is no fusion name to rewrite
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("A-B").getStatus());
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("V600E").getStatus());
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator("").getStatus());
        assertEquals(FusionSeparatorStatus.NOT_APPLICABLE, normalizeSeparator(null).getStatus());
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
        // Gene symbols may contain hyphens, which the HGVS separator keeps unambiguous
        assertTrue(isFusion("H1-4::H2BC5"));
        assertTrue(isFusion("H1-4::H2BC5 Fusion"));

        assertFalse(isFusion("A-B"));
    }

    public void testRevertFusionName() {
        // The reverted name is looked up in the alteration table, which curates fusions under the
        // HGVS separator, so both spellings of the query revert onto that form
        assertEquals("B::A Fusion", getRevertFusionName("A-B fusion"));
        assertEquals("B::A Fusion", getRevertFusionName("A-B fusion "));
        assertEquals("B::A Fusion", getRevertFusionName("A-B  fusion "));
        assertEquals("B::A Fusion", getRevertFusionName(" A-B  fusion "));
        assertEquals("B::A Fusion", getRevertFusionName("A::B fusion"));
        assertEquals("B::A Fusion", getRevertFusionName("A::B Fusion"));
        // A name curated without the keyword reverts without it too
        assertEquals("B::A", getRevertFusionName("A::B"));
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
        assertEquals("EGFR::SEPTIN14", fusionName);

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
