package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        Query query = new Query(null, null, null, "TEST", "V123M", null, "Pancreatic Adenocarcinoma", null, null, null, null);
        IndicatorQueryResp indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertTrue("The geneExist in the response is not false, but it should be.", indicatorQueryResp.getGeneExist() == false);
        assertEquals("The oncogenicity is not empty, but it should.", "", indicatorQueryResp.getOncogenic());
        assertTrue("There is treatment(s) in the response, but it should no have any.", indicatorQueryResp.getTreatments().size() == 0);

        // The last update should be a date even if we don't have any annotation for the gene/varaint
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        try {

            Date date = formatter.parse(indicatorQueryResp.getLastUpdate());
            assertTrue("The last update should be a valid date format MM/dd/yyyy, but it is not.", date != null);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Oncogenic should always match with oncogenic summary, similar to likely oncogenic
        query = new Query(null, null, null, "TP53", "R248Q", null, "Pancreatic Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The oncogenicity is not matched in variant summary.", "The TP53 R248Q mutation is likely oncogenic.", indicatorQueryResp.getVariantSummary());
        query = new Query(null, null, null, "KRAS", "V14I", null, "Pancreatic Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The oncogenicity is not matched in variant summary.", "The KRAS V14I mutation is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        // Check critical case
        query = new Query(null, null, null, "BRAF", "V600E", null, "Melanoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertTrue("The geneExist in the response is not true, but it should.", indicatorQueryResp.getGeneExist() == true);
        assertTrue("The variantExist in the response is not true, but it should.", indicatorQueryResp.getVariantExist() == true);
        assertTrue("The alleleExist in the response is not true, but it should.", indicatorQueryResp.getAlleleExist() == true);
        assertTrue("The query is not hotspot, but it should.", indicatorQueryResp.getHotspot() == true);
        assertTrue("The query is VUS, but it should not be.", indicatorQueryResp.getVUS() == false);
        assertEquals("The oncogenicity should be 'Oncogenic'", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 2B", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue("Should have no other significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() == 0);

        // Check fusion.
        query = new Query(null, null, null, "BRAF", "CUL1-BRAF Fusion", null, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level of CUL1-BRAF fusion should be Level 3A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of CUL1-BRAF fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Both genes have relevant alterations, should return highest level then highest oncogenicity
        query = new Query(null, null, null, "BRAF-TMPRSS2", null, "fusion", "Prostate Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level of CUL1-BRAF fusion should be Level 3A", LevelOfEvidence.LEVEL_3B, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of BRAF-TMPRSS2 fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Test Intragenic Mutation
        query = new Query(null, null, null, "CTCF", "CTCF-intragenic", null, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The oncogenicity of CTCF-intragenic should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Check other significant level
        query = new Query(null, null, null, "BRAF", "V600E", null, "Colorectal Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level should be 2B", LevelOfEvidence.LEVEL_2B, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_2B));

        query = new Query(null, null, null, "BRAF", "V600E", null, "Breast Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level should be 2B", LevelOfEvidence.LEVEL_2B, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue("Shouldn't have any significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() == 0);
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_2B));

        // Test for predicted oncogenic
        query = new Query(null, null, null, "PIK3R1", "K567E", null, "Pancreatic Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false);
        assertEquals("The oncogenicity should be 'Predicted Oncogenic'", Oncogenicity.PREDICTED.getOncogenic(), indicatorQueryResp.getOncogenic());

        query = new Query(null, null, null, "ALK", "R401Q", null, "Colon Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false);
        assertEquals("The oncogenicity should be 'Predicted Oncogenic'", Oncogenicity.PREDICTED.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "As of 10/06/2017, there was no available functional data about the ALK R401Q mutation. However, it has been identified as a statistically significant hotspot and is predicted to be oncogenic.", indicatorQueryResp.getVariantSummary());

        // No longer test 3A. KRAS has been downgraded to level 4
//        assertEquals("The highest sensitive level should be null, the level 3A evidence under Colorectal Cancer has been maked as NO propagation.",
//            null, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query(null, null, null, "KRAS", "Q61K", null, "Colorectal Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false);
        assertEquals("The oncogenicity should be 'Likely Oncogenic'", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 4",
            LevelOfEvidence.LEVEL_4, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level should be R1",
            LevelOfEvidence.LEVEL_R1, indicatorQueryResp.getHighestResistanceLevel());


        // Test cases generated through MSK-IMPACT reports which ran into issue before
        query = new Query(null, null, null, "EGFR", "S768_V769delinsIL", null, "Non-Small Cell Lung Cancer", "missense_variant", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, "cbioportal", true);
        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
        assertEquals("Variant should not exist", false, indicatorQueryResp.getVariantExist());
        assertEquals("Is expected to be likely oncogenic", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 1",
            LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query(null, null, null, "TMPRSS2-ERG", null, "Fusion", "Prostate Adenocarcinoma", "missense_variant", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, "cbioportal", true);
        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
        assertEquals("The Oncogenicity is not YES, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be null",
            null, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query(null, null, null, "CDKN2A", "M1?", null, "Colon Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, "cbioportal", true);
        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
        assertEquals("The Oncogenicity is not Likely Oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());


        // If alternative allele has resistance treatment, all sensitive treatments related to it should not be applied.
        // PDGFRA D842Y Gastrointestinal Stromal Tumor
        // Dasatinib should not be listed as D842V has resistance treatment
        query = new Query(null, null, null, "PDGFRA", "D842Y", null, "Gastrointestinal Stromal Tumor", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, "cbioportal", true);
        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
        assertEquals("The Oncogenicity is not Likely Oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 2A, but it is not.",
            LevelOfEvidence.LEVEL_2A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level should be null, but it is not.",
            null, indicatorQueryResp.getHighestResistanceLevel());
        assertEquals("The number of treatments should be two",
            1, indicatorQueryResp.getTreatments().size());

        // Hotspot manually curated as Likely Neutral should be converted to Predicted Oncogenic
        // EGFR A289D is manually curated as likely neutral.
        query = new Query(null, null, null, "EGFR", "A289D", null, "Gastrointestinal Stromal Tumor", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, "cbioportal", true);
        assertEquals("The Oncogenicity is not Predicted Oncogenic, but it should be.", Oncogenicity.PREDICTED.getOncogenic(), indicatorQueryResp.getOncogenic());

        // Oncogenicity of Alternative Allele overwrites Inconclusive
        // C24Y is annotated as Inconclusive but C24R is Likely Oncogenic
//        query = new Query(null, null, null, "BRCA1", "C24Y", null, "Colon Adenocarcinoma", null, null, null, null);
//        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, "cbioportal", true);
//        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
//        assertEquals("The Oncogenicity is not Likely Oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
//        assertEquals("Summary is not expected.", "The BRCA1 C24Y mutation has not been functionally or clinically validated. However, BRCA1 C24R is likely oncogenic, and therefore BRCA1 C24Y is considered likely oncogenic.", indicatorQueryResp.getVariantSummary());

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
        assertTrue("The oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));

        // Match Loss with Deletion
        query1 = new Query("PTEN", "Loss", null);
        query2 = new Query("PTEN", "Deletion", null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, false);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, false);
        assertTrue("The oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));

        // Match Truncating Mutations section to Deletion if no Deletion section specifically curated
        // In this test case, MAP3K1 does not have Deletion beening curated yet, but this may be changed due to
        // continue annotating process.
        query1 = new Query("MAP3K1", "T779*", null);
        query2 = new Query("MAP3K1", "Deletion", null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, false);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, false);
        assertTrue("The oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));

        // Check unknown denominator fusion, it should return same data as querying specific fusion.
        query1 = new Query(null, null, null, "BRAF", "CUL1-BRAF Fusion", null, "Ovarian Cancer", null, null, null, null);
        query2 = new Query(null, null, null, "CUL1-BRAF", null, "fusion", "Ovarian Cancer", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, true);
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));

        // Check hugoSymbol and entrezGene pair
        query1 = new Query(null, null, 673, null, "V600E", null, "Melanoma", null, null, null, null);
        query2 = new Query(null, null, null, "BRAF", "V600E", null, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, true);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));


        //EntrezGeneId has higher priority then hugoSymbol
        query1 = new Query(null, null, 673, "EGFR", "V600E", null, "Melanoma", null, null, null, null);
        query2 = new Query(null, null, null, "BRAF", "V600E", null, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, true);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));

        //Check whether empty hugoSymbol will effect the result
        query1 = new Query(null, null, 673, "", "V600E", null, "Melanoma", null, null, null, null);
        query2 = new Query(null, null, null, "BRAF", "V600E", null, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, true);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));

        //Other Biomarker tests
        query = new Query(null, null, null, null, "MSI-H", null, "Colorectal Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertTrue("The geneExist is not false, but it should be.", indicatorQueryResp.getGeneExist() == false);
        assertEquals("The oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level is not 1, but it should be.", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The gene summary is not empty, but it should be.", "", indicatorQueryResp.getGeneSummary());

        // Test indicator endpoint supports HGVS
        query = new Query(null, null, null, null, null, null, "Melanoma", null, null, null, "7:g.140453136A>T");
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertTrue("The geneExist is not true, but it should be.", indicatorQueryResp.getGeneExist() == true);
        assertEquals("The oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level is not 1, but it should be.", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());

        query1 = new Query(null, null, null, null, null, null, "Melanoma", null, null, null, "7:g.140453136A>T");
        query2 = new Query(null, null, null, "BRAF", "V600E", null, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        resp2 = IndicatorUtils.processQuery(query2, null, null, null, true);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));

        // Test HGVS has higher priority than gene/variant pair
        // 7:g.140453136A>T is BRAF V600E
        query = new Query(null, null, null, "ALK", "R401Q", null, "Melanoma", null, null, null, "7:g.140453136A>T");
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertTrue("The geneExist is not true, but it should be.", indicatorQueryResp.getGeneExist() == true);
        assertEquals("The oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level is not 1, but it should be.", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
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
