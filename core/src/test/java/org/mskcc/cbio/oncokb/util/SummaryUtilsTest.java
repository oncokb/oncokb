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
        assertEquals("ERBB2 amplification", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", false));
        assertEquals("ERBB2 amplification (gain)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "gain", false));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", false), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "amplification", false));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", false), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " amplification", false));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", false));
        assertEquals("ERBB2 deletion (loss)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "loss", false));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", false), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "deLetion", false));
    }

    @Test
    public void testGetGeneMutationNameInTumorTypeSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2-amplified", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", false));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", false), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "amplification", false));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", false), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " amplification", false));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Amplification", false), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "gain", false));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", false));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", false), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "loss", false));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", false), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), " loss", false));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "Deletion", false), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, gene.getHugoSymbol(), "deLetion", false));
    }
}
