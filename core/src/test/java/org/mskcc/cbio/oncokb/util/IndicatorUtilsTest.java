package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.*;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Hongxin on 12/23/16.
 */
public class IndicatorUtilsTest {
    @Test
    public void testProcessQuery() throws Exception {
        // We do not check gene/variant/tumor type summaries here. The test will be done in SummaryUtilsTest.

        // Gene not exists
        Query query = new Query("FGF6", null, "V123M", null, "Pancreatic Adenocarcinoma", null, null, null);
        IndicatorQueryResp indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertTrue("The geneExist in the response should be set to false", indicatorQueryResp.getGeneExist() == false);
        assertEquals("The oncogenicity of should be empty", "", indicatorQueryResp.getOncogenic());
        assertTrue("No treatment should be given", indicatorQueryResp.getTreatments().size() == 0);

        // Oncogenic should always match with oncogenic summary, similar to likely oncogenic
        query = new Query("TP53", null, "R248Q", null, "Pancreatic Adenocarcinoma", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The oncogenicity is not matched in variant summary.", "The TP53 R248Q mutation is likely oncogenic.", indicatorQueryResp.getVariantSummary());
        query = new Query("KRAS", null, "V14I", null, "Pancreatic Adenocarcinoma", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The oncogenicity is not matched in variant summary.", "The KRAS V14I mutation is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        // Check fusion.
        query = new Query("BRAF", null, "CUL1-BRAF Fusion", null, "Ovarian Cancer", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level of CUL1-BRAF fusion should be Level 3A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of CUL1-BRAF fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Check other significant level
        query = new Query("BRAF", null, "V600E", null, "Colorectal Cancer", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level should be 2B", LevelOfEvidence.LEVEL_2B, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue("Should have one significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() == 1);
        assertEquals("The other significant level should be 3A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getOtherSignificantSensitiveLevels().get(0));
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_3A));
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_2B));

        query = new Query("BRAF", null, "V600E", null, "Breast Cancer", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level should be 2B", LevelOfEvidence.LEVEL_2B, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue("Shouldn't have any significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() == 0);
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_2B));

        // Test for predicted oncogenic
        query = new Query("KRAS", null, "\tQ61Kfs*7", null, "Pancreatic Adenocarcinoma", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false);
        assertEquals("The oncogenicity should be 'Predicted Oncogenic'", Oncogenicity.PREDICTED.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 4, the level 3A evidence under Colorectal Cancer has been maked as NO propagation.",
            LevelOfEvidence.LEVEL_4, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query("KRAS", null, "\tQ61Kfs*7", null, "Colorectal Cancer", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false);
        assertEquals("The oncogenicity should be 'Predicted Oncogenic'", Oncogenicity.PREDICTED.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 3A",
            LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level should be R1",
            LevelOfEvidence.LEVEL_R1, indicatorQueryResp.getHighestResistanceLevel());


        /**
         * Comparing between two queries
         */
        Query query1, query2;
        IndicatorQueryResp resp1, resp2;

        // Match Gain with Amplification
        query1 = new Query("PTEN", "Gain", null);
        query2 = new Query("PTEN", "Amplification", null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, false);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, false);
        assertTrue("The oncogenicity should be the same", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatment should be the same", resp1.getTreatments().equals(resp2.getTreatments()));

        // Match Loss with Deletion
        query1 = new Query("PTEN", "Loss", null);
        query2 = new Query("PTEN", "Deletion", null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, false);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, false);
        assertTrue("The oncogenicity should be the same", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatment should be the same", resp1.getTreatments().equals(resp2.getTreatments()));

        // Match Truncating Mutations section to Deletion if no Deletion section specifically curated
        // In this test case, MAP3K1 does not have Deletion beening curated yet, but this may be changed due to
        // continue annotating process.
        query1 = new Query("MAP3K1", "T779*", null);
        query2 = new Query("MAP3K1", "Deletion", null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, false);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, false);
        assertTrue("The oncogenicity should be the same", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatment should be the same", resp1.getTreatments().equals(resp2.getTreatments()));

        // Check unknown denominator fusion, it should return same data as querying specific fusion.
        query1 = new Query("BRAF", null, "CUL1-BRAF Fusion", null, "Ovarian Cancer", null, null, null);
        query2 = new Query("CUL1-BRAF", null, null, "fusion", "Ovarian Cancer", null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, true);
        assertTrue("Oncogenic should be the same", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments should be the same", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive level should be the same", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance level should be the same", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));
    }

    private Boolean treatmentsContainLevel(List<IndicatorQueryTreatment> treatments, LevelOfEvidence level) {
        if (level == null || treatments == null) {
            return false;
        }

        for (IndicatorQueryTreatment treatment : treatments) {
            if (treatment.getLevel() != null && treatment.getLevel().equals(level)) {
                return true;
            }
        }
        return false;
    }
}
