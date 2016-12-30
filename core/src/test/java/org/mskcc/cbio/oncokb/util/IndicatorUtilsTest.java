package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.IndicatorQueryTreatment;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.Query;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Hongxin on 12/23/16.
 */
public class IndicatorUtilsTest {
    @Test
    public void testProcessQuery() throws Exception {
        // We dont check gene/variant/tumor type summaries here. The test will be done in SummaryUtilsTest.

        // Check fusion.
        Query query = new Query("BRAF", null, "CUL1-BRAF Fusion", null, "Ovarian Cancer", null, null, null);
        IndicatorQueryResp indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level of CUL1-BRAF fusion should be Level 3A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of CUL1-BRAF fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Check unknown denominator fusion, it should return same data as querying specific fusion.
        Query query1 = new Query("CUL1-BRAF", null, null, "fusion", "Ovarian Cancer", null, null, null);
        IndicatorQueryResp indicatorQueryResp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        assertTrue("Oncogenic should be the same", indicatorQueryResp.getOncogenic().equals(indicatorQueryResp1.getOncogenic()));
        assertTrue("Treatments should be the same", indicatorQueryResp.getTreatments().equals(indicatorQueryResp1.getTreatments()));
        assertTrue("Highest sensitive level should be the same", LevelUtils.areSameLevels(indicatorQueryResp.getHighestSensitiveLevel(), indicatorQueryResp1.getHighestSensitiveLevel()));
        assertTrue("Highest resistance level should be the same", LevelUtils.areSameLevels(indicatorQueryResp.getHighestResistanceLevel(), indicatorQueryResp1.getHighestResistanceLevel()));

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
