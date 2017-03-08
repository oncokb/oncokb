package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.Gene;

import static org.junit.Assert.assertEquals;

/**
 * Created by Hongxin on 12/5/16.
 */
public class SummaryUtilsTest {
    @Test
    public void testGetGeneMutationNameInVariantSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2 amplification", SummaryUtils.getGeneMutationNameInVariantSummary(gene, "Amplification"));
        assertEquals("ERBB2 amplification (gain)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, "gain"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, "Amplification"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, "amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, "Amplification"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, " amplification"));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInVariantSummary(gene, "Deletion"));
        assertEquals("ERBB2 deletion (loss)", SummaryUtils.getGeneMutationNameInVariantSummary(gene, "loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInVariantSummary(gene, "Deletion"), SummaryUtils.getGeneMutationNameInVariantSummary(gene, "deLetion"));
    }

    @Test
    public void testGetGeneMutationNameInTumorTypeSummary() throws Exception {
        Gene gene = GeneUtils.getGene("ERBB2");
        assertEquals("ERBB2-amplified", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "Amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, " amplification"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "Amplification"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "gain"));

        assertEquals("ERBB2 deletion", SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "Deletion"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, " loss"));
        assertEquals(SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "Deletion"), SummaryUtils.getGeneMutationNameInTumorTypeSummary(gene, "deLetion"));
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
