package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing.Tumor;

import static org.junit.Assert.assertEquals;
import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Created by Hongxin on 12/5/16.
 */
public class SummaryUtilsTest {
    @Test
    public void testGetGeneMutationNameInVariantSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2 amplification", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", null));
        assertEquals("ERBB2 amplification (gain)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "gain", null));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", null), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "amplification", null));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", null), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " amplification", null));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", null));
        assertEquals("ERBB2 deletion (loss)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "loss", null));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", null), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "deLetion", null));
    }

    @Test
    public void testGetGeneMutationNameInTumorTypeSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2-amplified", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", null));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", null), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "amplification",null));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", null), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " amplification",null));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", null), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "gain",null));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", null));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", null), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "loss", null));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", null), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " loss",null));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", null), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "deLetion",null));
    }
}
