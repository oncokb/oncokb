package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.Oncogenicity;
import org.mskcc.cbio.oncokb.model.Query;
import org.testng.annotations.Test;

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
        assertEquals("The highest sensitive level of CUL1-BRAF fusion should be Level 3A", "LEVEL_3A", indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of CUL1-BRAF fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Check unknown denominator fusion, it should return same data as querying specific fusion.
        Query query1 = new Query("CUL1-BRAF", null, null, "fusion", "Ovarian Cancer", null, null, null);
        IndicatorQueryResp indicatorQueryResp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        assertTrue("Oncogenic should be the same", indicatorQueryResp.getOncogenic().equals(indicatorQueryResp1.getOncogenic()));
        assertTrue("Treatments should be the same", indicatorQueryResp.getTreatments().equals(indicatorQueryResp1.getTreatments()));
        assertTrue("Highest sensitive level should be the same", indicatorQueryResp.getHighestSensitiveLevel().equals(indicatorQueryResp1.getHighestSensitiveLevel()));
        assertTrue("Highest resistance level should be the same", indicatorQueryResp.getHighestResistanceLevel().equals(indicatorQueryResp1.getHighestResistanceLevel()));
    }

}
