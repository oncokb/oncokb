package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.Gene;

import static org.junit.Assert.assertEquals;
import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Created by Hongxin on 12/5/16.
 */
public class SummaryUtilsTest {
    @Test
    public void testGetGeneMutationNameInVariantSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2 amplification", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "Amplification"));
        assertEquals("ERBB2 amplification (gain)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "gain"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "Amplification"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "Amplification"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, " amplification"));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "Deletion"));
        assertEquals("ERBB2 deletion (loss)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "Deletion"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, DEFAULT_REFERENCE_GENOME, "deLetion"));
    }

    @Test
    public void testGetGeneMutationNameInTumorTypeSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2-amplified", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "Amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, " amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "gain"));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "Deletion"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, " loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, DEFAULT_REFERENCE_GENOME, "deLetion"));
    }

    public void testVariantTumorTypeSummary() throws Exception {

    }

    public void testVariantCustomizedSummary() throws Exception {

    }

    public void testTumorTypeSummary() throws Exception {

    }

    public void testUnknownOncogenicSummary() throws Exception {

    }

    public void testSynonymousSummary() throws Exception {

    }

    public void testOncogenicSummary() throws Exception {

    }

    public void testGeneSummary() throws Exception {

    }

    public void testFullSummary() throws Exception {

    }

    public void testAlleleSummary() throws Exception {

    }

    public void testHotspotSummary() throws Exception {

    }
}
