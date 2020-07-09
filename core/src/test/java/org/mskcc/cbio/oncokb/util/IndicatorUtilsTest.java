package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mskcc.cbio.oncokb.Constants.MISSENSE_VARIANT;
import static org.mskcc.cbio.oncokb.util.SummaryUtils.ONCOGENIC_MUTATIONS_DEFAULT_SUMMARY;

/**
 * Created by Hongxin on 12/23/16.
 */
public class IndicatorUtilsTest {
    @Test
    public void testProcessQuery() throws Exception {
        // We do not check gene/variant/tumor type summaries here. The test will be done in SummaryUtilsTest.

        // Gene not exists
        Query query = new Query(null, null, null, "TEST", "V123M", null, null, "Pancreatic Adenocarcinoma", null, null, null, null);
        IndicatorQueryResp indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertTrue("The geneExist in the response is not false, but it should be.", indicatorQueryResp.getGeneExist() == false);
        assertEquals("The oncogenicity is not empty, but it should.", "", indicatorQueryResp.getOncogenic());
        assertTrue("There is treatment(s) in the response, but it should no have any.", indicatorQueryResp.getTreatments().size() == 0);

        query = new Query(null, null, null, "CD74-CD74", null, "structural_variant", StructuralVariantType.DELETION, "Pancreatic Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("Gene should not exist, but it does.", false, indicatorQueryResp.getGeneExist());
        assertEquals("The oncogenicity is not empty, but it should.", "", indicatorQueryResp.getOncogenic());
        assertTrue("There is treatment(s) in the response, but it should no have any.", indicatorQueryResp.getTreatments().size() == 0);

        query = new Query(null, null, null, "CD74-CD74", null, "structural_variant", StructuralVariantType.DELETION, "Pancreatic Adenocarcinoma", "fusion", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("Gene should not exist, but it does.", false, indicatorQueryResp.getGeneExist());
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

        // Alteration not available
        query = new Query(null, null, null, "MSH2", "", null, null, "CANCER", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The oncogenicity is not empty, but it should.", "", indicatorQueryResp.getOncogenic());

        // Oncogenic should always match with oncogenic summary, similar to likely oncogenic
        query = new Query(null, null, null, "TP53", "R248Q", null, null, "Pancreatic Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The oncogenicity is not matched in variant summary.", "The TP53 R248Q mutation is likely oncogenic.", indicatorQueryResp.getVariantSummary());
        query = new Query(null, null, null, "KRAS", "V14I", null, null, "Pancreatic Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The oncogenicity is not matched in variant summary.", "The KRAS V14I mutation is likely oncogenic.", indicatorQueryResp.getVariantSummary());

        // Check critical case
        query = new Query(null, null, null, "BRAF", "V600E", null, null, "Melanoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertTrue("The geneExist in the response is not true, but it should.", indicatorQueryResp.getGeneExist() == true);
        assertTrue("The variantExist in the response is not true, but it should.", indicatorQueryResp.getVariantExist() == true);
        assertTrue("The alleleExist in the response is not true, but it should.", indicatorQueryResp.getAlleleExist() == true);
        assertTrue("The query is not hotspot, but it should.", indicatorQueryResp.getHotspot() == true);
        assertTrue("The query is VUS, but it should not be.", indicatorQueryResp.getVUS() == false);
        assertEquals("The oncogenicity should be 'Oncogenic'", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 1", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue("Should have no other significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() == 0);

        // Check fusion.
        query = new Query(null, null, null, "BRAF", "CUL1-BRAF Fusion", null, null, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The highest sensitive level of CUL1-BRAF fusion should be Level 3A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of CUL1-BRAF fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Both genes have relevant alterations, should return highest level then highest oncogenicity
        query = new Query(null, null, null, "BRAF-TMPRSS2", null, "fusion", null, "Prostate Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The highest sensitive level of CUL1-BRAF fusion should be Level 3A", LevelOfEvidence.LEVEL_3B, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of BRAF-TMPRSS2 fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Test Intragenic Mutation
        query = new Query(null, null, null, "NOTCH1", "NOTCH1-intragenic", null, null, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The oncogenicity of NOTCH1-intragenic should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        query = new Query(null, null, null, "NOTCH1", "NOTCH1 intragenic", null, null, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The oncogenicity of NOTCH1 intragenic should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Check other significant level
        query = new Query(null, null, null, "BRAF", "V600E", null, null, "Bladder Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The highest sensitive level should be 3B", LevelOfEvidence.LEVEL_3B, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_3B));

        query = new Query(null, null, null, "BRAF", "V600E", null, null, "Breast Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The highest sensitive level should be 3B", LevelOfEvidence.LEVEL_3B, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue("Shouldn't have any significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() == 0);
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_3B));

        // For treatments include both 2B and 3A, 3A should be shown first
        // Test disabled: we no longer have 2B which means the other significant levels are not used
//        query = new Query(null, null, null, "RET", "Fusions", null, null, "Medullary Thyroid Cancer", null, null, null, null);
//        indicatorQueryResp = IndicatorUtils.processQuery(query, null true, null);
//        assertEquals("The highest sensitive level should be 2B", LevelOfEvidence.LEVEL_2B, indicatorQueryResp.getHighestSensitiveLevel());
//        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
//        assertTrue("Should have any significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() > 0);
//        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_2B));
//        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_3A));
//        assertEquals("The level 3A should be shown before 2A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getTreatments().get(0).getLevel());

        // Test for predicted oncogenic
        query = new Query(null, null, null, "PIK3R1", "K567E", null, null, "Pancreatic Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, false, null);
        assertEquals("The oncogenicity should be 'Predicted Oncogenic'", Oncogenicity.PREDICTED.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The isHotspot is not true, but it should be.", Boolean.TRUE, indicatorQueryResp.getHotspot());

        // ALK R401Q should not be hotspot. It later was removed from the hotspot list.
        // The position has high truncating rate
        query = new Query(null, null, null, "ALK", "R401Q", null, null, "Colon Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, false, null);
        assertEquals("The oncogenicity should not be 'Predicted Oncogenic'", "", indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "As of 02/01/2019, there was no available functional data about the ALK R401Q mutation.", indicatorQueryResp.getVariantSummary());
        assertEquals("The isHotspot is not false, but it should be.", Boolean.FALSE, indicatorQueryResp.getHotspot());

        // No longer test 3A. KRAS has been downgraded to level 4
//        assertEquals("The highest sensitive level should be null, the level 3A evidence under Colorectal Cancer has been maked as NO propagation.",
//            null, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query(null, null, null, "KRAS", "Q61K", null, null, "Colorectal Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, false, null);
        assertEquals("The oncogenicity should be 'Likely Oncogenic'", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 4",
            LevelOfEvidence.LEVEL_4, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level should be R1",
            LevelOfEvidence.LEVEL_R1, indicatorQueryResp.getHighestResistanceLevel());

        // Check special variant Oncogenic Mutations
        query = new Query(null, null, null, "BRAF", InferredMutation.ONCOGENIC_MUTATIONS.getVariant(), null, null, "Colorectal Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, false, null);
        assertEquals("The oncogenicity should be 'Unknown'", Oncogenicity.UNKNOWN.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The mutation effect is not unknown, but it should be.", MutationEffect.UNKNOWN.getMutationEffect(), indicatorQueryResp.getMutationEffect().getKnownEffect());

        assertEquals("The variant summary does not match",
            ONCOGENIC_MUTATIONS_DEFAULT_SUMMARY, indicatorQueryResp.getVariantSummary());


        // Test R2 data
        query = new Query(null, null, null, "ALK", "I1171N", null, null, "Lung Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, false, null);
        assertEquals("The oncogenicity should be 'Oncogenic'", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 4",
            LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level should be R1",
            LevelOfEvidence.LEVEL_R2, indicatorQueryResp.getHighestResistanceLevel());

        query = new Query(null, null, null, "EGFR", "C797S", null, null, "Lung Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, false, null);
        assertEquals("The oncogenicity should be 'Oncogenic'", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 4",
            null, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level should be R1",
            LevelOfEvidence.LEVEL_R2, indicatorQueryResp.getHighestResistanceLevel());

        // Test cases generated through MSK-IMPACT reports which ran into issue before
        query = new Query(null, null, null, "EGFR", "S768_V769delinsIL", null, null, "Non-Small Cell Lung Cancer", MISSENSE_VARIANT, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
        assertEquals("Variant should exist(mapped to S768I)", true, indicatorQueryResp.getVariantExist());
        assertEquals("Is expected to be likely oncogenic", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 1",
            LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query(null, null, null, "TMPRSS2-ERG", null, "Fusion", null, "Prostate Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
        assertEquals("The Oncogenicity is not YES, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be null",
            null, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query(null, null, null, "CDKN2A", "M1?", null, null, "Colon Adenocarcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
        assertEquals("The Oncogenicity is not Likely Oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());


        // The tumor type summary should come from the positional variant instead of alternative allele
        // in this case, the V600K should get summary from V600
        query = new Query(null, null, null, "BRAF", "V600K", null, null, "anaplastic thyroid cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The tumor type summary does not match.", "The RAF-targeted inhibitor dabrafenib in combination with the MEK1/2-targeted inhibitor trametinib is FDA-approved for the treatment of patients with BRAF V600E mutant anaplastic thyroid cancer (ATC), and NCCN-compendium listed for the treatment of patients with BRAF V600-mutant ATC.", indicatorQueryResp.getTumorTypeSummary());

        // If alternative allele has resistance treatment, all sensitive treatments related to it should not be applied.
        // PDGFRA D842Y Gastrointestinal Stromal Tumor
        // Dasatinib should not be listed as D842V has resistance treatment
        query = new Query(null, null, null, "PDGFRA", "D842Y", null, null, "Gastrointestinal Stromal Tumor", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
        assertEquals("The Oncogenicity is not Likely Oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 1, but it is not.",
            LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level should be null, but it is not.",
            null, indicatorQueryResp.getHighestResistanceLevel());
        assertEquals("The number of treatments should be two",
            1, indicatorQueryResp.getTreatments().size());

        // Manually curated likely neutral should overwrite hotspot predicted oncogenic rule
        // EGFR A289D is manually curated as likely neutral.
        // Update at 01/29/2018 A289D is no longer curated as likely neutral
//        query = new Query(null, null, null, "EGFR", "A289D", null, null, "Gastrointestinal Stromal Tumor", null, null, null, null);
//        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
//        assertEquals("The Oncogenicity is not likely neutral, but it should be.", Oncogenicity.LIKELY_NEUTRAL.getOncogenic(), indicatorQueryResp.getOncogenic());

        // BRAF R462I is manually curated as Likely Neutral, then the oncogenic mutations shouldn't be associated.
        query = new Query(null, null, null, "BRAF", "R462I", null, null, "Gastrointestinal Stromal Tumor", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not likely neutral, but it should be.", Oncogenicity.LIKELY_NEUTRAL.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level of BRAF R462I should be null.", null, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level of BRAF R462I should be null.", null, indicatorQueryResp.getHighestResistanceLevel());
        assertEquals("The tumor type summary does not match.", "There are no FDA-approved or NCCN-compendium listed treatments specifically for patients with BRAF R462I mutant gastrointestinal stromal tumors.", indicatorQueryResp.getTumorTypeSummary());

        // Likely Neutral oncogenicity should not be propagated to alternative allele
        query = new Query(null, null, null, "AKT1", "P42I", null, null, "Anaplastic Astrocytoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not unknown, but it should be.", Oncogenicity.UNKNOWN.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The mutation effect is not unknown, but it should be.", MutationEffect.UNKNOWN.getMutationEffect(), indicatorQueryResp.getMutationEffect().getKnownEffect());

        // Check EGFR vIII vII vIV
        query = new Query(null, null, null, "EGFR", "EGFRvIII", null, null, "Gastrointestinal Stromal Tumor", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR vIII alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        query = new Query(null, null, null, "EGFR", "vIII", null, null, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR vIII alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        query = new Query(null, null, null, "EGFR", "vII", null, null, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR vII alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        query = new Query(null, null, null, "EGFR", "vV", null, null, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR vV alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        // Check EGFR CTD
        query = new Query(null, null, null, "EGFR", "CTD", null, null, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR C-terminal domain (CTD) alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        // Check EGFR KDD
        query = new Query(null, null, null, "EGFR", "KDD", null, null, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR kinase domain duplication (KDD) alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());
        assertEquals("The highest sensitive level should be 1", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query(null, null, null, "EGFR", "KDD", "structural_variant", StructuralVariantType.DELETION, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR kinase domain duplication (KDD) alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());
        assertEquals("The highest sensitive level should be 1", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query(null, null, null, "EGFR", "kinase domain duplication", null, null, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR kinase domain duplication alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());
        assertEquals("The highest sensitive level should be 1", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());

        // Check FLT3 ITD
        query = new Query(null, null, null, "FLT3", "ITD", null, null, "AML", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, false, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The FLT3 internal tandem duplication (ITD) alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());
        assertEquals("The highest sensitive level should be 1", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("There should be level 1 treatment in the list", treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_1));
        assertTrue("There should be level 3A treatment in the list", treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_3A));

        query = new Query(null, null, null, "EGFR-EGFR", "vIII", "structural_variant", StructuralVariantType.DELETION, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR vIII alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        query = new Query(null, null, null, "EGFR-EGFR", "vIII", "structural_variant", StructuralVariantType.DELETION, "NSCLC", "", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR vIII alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        query = new Query(null, null, null, "EGFR", "vIII", "structural_variant", StructuralVariantType.DELETION, "NSCLC", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", "The EGFR vIII alteration is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        query = new Query(null, null, null, "ALK", "", "structural_variant", StructuralVariantType.DELETION, "Gastrointestinal Stromal Tumor", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not empty.", "", indicatorQueryResp.getOncogenic());

        query = new Query(null, null, null, "ALK", "", "structural_variant", StructuralVariantType.DELETION, "Gastrointestinal Stromal Tumor", "fusion", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not likely oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());

        // For duplication, proteinStart/proteinEnd in OncoKB annotation should overwrite the input from outside
        // The hotspot range is 65_77indel.
        // In original design, if the caller calls the duplication happened at 78, this variant will not be qualified for predicted oncogenic. But it could be treated the insertion happened at 68.
        query = new Query(null, null, null, "AKT1", "P68_C77dup", null, null, "Gastrointestinal Stromal Tumor", "In_Frame_Ins", 78, 78, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not Likely Oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The isHotspot is not true, but it should be.", Boolean.TRUE, indicatorQueryResp.getHotspot());

        // For variant has VUS as relevant alteration and the this VUS happens to be a hotspot, this variant should not be annotated as hotspot mutation.
        // No longer applicable
//        query = new Query(null, null, null, "MAP2K1", "N109_R113del", null, null, "Gastrointestinal Stromal Tumor", null, null, null, null);
//        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
//        assertEquals("The Oncogenicity is not empty, but it should be.", "", indicatorQueryResp.getOncogenic());
//        assertEquals("The isHotspot is true, but it should not be.", Boolean.FALSE, indicatorQueryResp.getHotspot());
//        assertEquals("The highest level of sensitive treatment is not null, but it should be.", null, indicatorQueryResp.getHighestSensitiveLevel());

        // For non-functional fusion, the Deletion should still be mapped
        query = new Query(null, null, null, "BRCA2", null, "structural_variant", StructuralVariantType.DELETION, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The mutation effect is not expected.", "Loss-of-function", indicatorQueryResp.getMutationEffect().getKnownEffect());
        assertEquals("The highest level of sensitive treatment is not level 1, but it should be.", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());

        // For special case in cBioPortal
        // The fusion event may not have the keyword `fusion` in the protein change, but the mutation type correctly added as Fusion
        query = new Query(null, null, null, "NTRK3", "ETV6-NTRK3", AlterationType.FUSION.label(), null, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        query = new Query(null, null, null, "NTRK3", "ETV6-NTRK3", "Mutation", null, "Ovarian Cancer", "Fusion", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());

        // Oncogenicity of Alternative Allele overwrites Inconclusive
        // C24Y is annotated as Inconclusive but C24R is Likely Oncogenic
//        query = new Query(null, null, null, "BRCA1", "C24Y", null, "Colon Adenocarcinoma", null, null, null, null);
//        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true);
//        assertEquals("Gene should exist", true, indicatorQueryResp.getGeneExist());
//        assertEquals("The Oncogenicity is not Likely Oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());
//        assertEquals("Summary is not expected.", "The BRCA1 C24Y mutation has not been functionally or clinically validated. However, BRCA1 C24R is likely oncogenic, and therefore BRCA1 C24Y is considered likely oncogenic.", indicatorQueryResp.getVariantSummary());


        // Check the predefined TERT Promoter summary
        query = new Query(null, null, null, "TERT", "Promoter", null, null, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", SummaryUtils.TERT_PROMOTER_MUTATION_SUMMARY, indicatorQueryResp.getVariantSummary());
        assertEquals("The tumor type summary is not expected.", SummaryUtils.TERT_PROMOTER_NO_THERAPY_TUMOR_TYPE_SUMMARY.replace("[[tumor type]]", "ovarian cancer"), indicatorQueryResp.getTumorTypeSummary());

        query = new Query(null, null, null, "TERT", "promoter ", null, null, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The variant summary is not expected.", SummaryUtils.TERT_PROMOTER_MUTATION_SUMMARY, indicatorQueryResp.getVariantSummary());
        assertEquals("The tumor type summary is not expected.", SummaryUtils.TERT_PROMOTER_NO_THERAPY_TUMOR_TYPE_SUMMARY.replace("[[tumor type]]", "ovarian cancer"), indicatorQueryResp.getTumorTypeSummary());

        // Check the case when alteration is empty but consequence is specified. This avoids positional variants
        query = new Query(null, null, null, "KIT", "", null, null, "Ovarian Cancer", MISSENSE_VARIANT, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The Oncogenicity is not empty, but it should be.", "", indicatorQueryResp.getOncogenic());
        assertTrue("There should not be any treatments", indicatorQueryResp.getTreatments().isEmpty());

        // Give a default mutation effect when it is not available: Unknown
        query = new Query(null, null, null, "PIK3R1", "W583del", null, null, null, null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertTrue("The mutation effect is null, but it should not be.", indicatorQueryResp.getMutationEffect() != null);
        assertEquals("The mutation effect is not unknown, but it should be.", "Unknown", indicatorQueryResp.getMutationEffect().getKnownEffect());


        /**
         * Comparing between two queries
         */
        Query query1, query2;
        IndicatorQueryResp resp1, resp2;

        // Match Gain with Amplification
        query1 = new Query("PTEN", "Gain", null);
        query2 = new Query("PTEN", "Amplification", null);
        resp1 = IndicatorUtils.processQuery(query1, null, false, null);
        resp2 = IndicatorUtils.processQuery(query2, null, false, null);
        assertTrue("The oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));

        // Match Loss with Deletion
        query1 = new Query("PTEN", "Loss", null);
        query2 = new Query("PTEN", "Deletion", null);
        resp1 = IndicatorUtils.processQuery(query1, null, false, null);
        resp2 = IndicatorUtils.processQuery(query2, null, false, null);
        assertTrue("The oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));


        // Match intragenic to structural variant deletion
        query1 = new Query("ESR1", "ESR1 intragenic", "Melanoma");
        query2 = new Query(null, null, null, "ESR1", null, "structural_variant", StructuralVariantType.DELETION, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, false, null);
        resp2 = IndicatorUtils.processQuery(query2, null, false, null);
        assertTrue("The oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));


        query1 = new Query(null, null, 2099, null, null, "structural_variant", StructuralVariantType.DELETION, "Melanoma", null, null, null, null);
        query2 = new Query(null, null, null, "ESR1", null, "structural_variant", StructuralVariantType.DELETION, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, false, null);
        resp2 = IndicatorUtils.processQuery(query2, null, false, null);
        assertTrue("The oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));

        // Match Truncating Mutations section to Deletion if no Deletion section specifically curated
        // In this test case, MAP3K1 does not have Deletion beening curated yet, but this may be changed due to
        // continue annotating process.
        query1 = new Query("MAP3K1", "T779*", null);
        query2 = new Query("MAP3K1", "Deletion", null);
        resp1 = IndicatorUtils.processQuery(query1, null, false, null);
        resp2 = IndicatorUtils.processQuery(query2, null, false, null);
        assertTrue("The oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));

        // Check unknown denominator fusion, it should return same data as querying specific fusion.
        query1 = new Query(null, null, null, "BRAF", "CUL1-BRAF Fusion", null, null, "Ovarian Cancer", null, null, null, null);
        query2 = new Query(null, null, null, "CUL1-BRAF", null, "fusion", null, "Ovarian Cancer", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));

        // Check hugoSymbol and entrezGene pair
        query1 = new Query(null, null, 673, null, "V600E", null, null, "Melanoma", null, null, null, null);
        query2 = new Query(null, null, null, "BRAF", "V600E", null, null, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));


        //EntrezGeneId has higher priority then hugoSymbol
        query1 = new Query(null, null, 673, "EGFR", "V600E", null, null, "Melanoma", null, null, null, null);
        query2 = new Query(null, null, null, "BRAF", "V600E", null, null, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));

        //Check whether empty hugoSymbol will effect the result
        query1 = new Query(null, null, 673, "", "V600E", null, null, "Melanoma", null, null, null, null);
        query2 = new Query(null, null, null, "BRAF", "V600E", null, null, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));

        // Compare EGFR CTD AND EGFR, EGFR CTD
        // Check EGFR CTD
        query1 = new Query(null, null, null, "EGFR", "EGFR CTD", null, null, "Gastrointestinal Stromal Tumor", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query, null, true, null);
        query2 = new Query(null, null, null, "EGFR", "CTD", null, null, "Gastrointestinal Stromal Tumor", null, null, null, null);
        resp2 = IndicatorUtils.processQuery(query, null, true, null);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("The Oncogenicities are not the same.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));


        //Other Biomarker tests
        query = new Query(null, null, null, null, "MSI-H", null, null, "Colorectal Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertTrue("The geneExist is not false, but it should be.", indicatorQueryResp.getGeneExist() == false);
        assertEquals("The oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level is not 1, but it should be.", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The gene summary is not empty, but it should be.", "", indicatorQueryResp.getGeneSummary());
        assertEquals("The variant summary is not expected.", "Genetic or epigenetic alterations resulting in loss of function of mismatch repair (MMR) genes can lead to a microsatellite instability-high (MSI-H)/mismatch repair deficient (MMR-D) phenotype.", indicatorQueryResp.getVariantSummary());
        assertEquals("The tumor type summary is not expected.", "The anti-PD-1 antibodies pembrolizumab or nivolumab, as single-agents, and the anti-CTLA4 antibody ipilimumab in combination with nivolumab are FDA-approved for the treatment of patients with MMR-D or MSI-H metastatic colorectal cancer.", indicatorQueryResp.getTumorTypeSummary());

        query = new Query(null, null, null, null, "MSI-H", null, null, "Cervical Endometrioid Carcinoma", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertTrue("The geneExist is not false, but it should be.", indicatorQueryResp.getGeneExist() == false);
        assertEquals("The oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level is not 1, but it should be.", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The gene summary is not empty, but it should be.", "", indicatorQueryResp.getGeneSummary());
        assertEquals("The variant summary is not expected.", "Genetic or epigenetic alterations resulting in loss of function of mismatch repair (MMR) genes can lead to a microsatellite instability-high (MSI-H)/mismatch repair deficient (MMR-D) phenotype.", indicatorQueryResp.getVariantSummary());
        assertEquals("The tumor type summary is not expected.", "The anti-PD-1 antibody pembrolizumab is an FDA-approved drug for therapy of adult and pediatric patients with unresectable or metastatic MSI-H/MMR-D solid cancers that have progressed following prior treatment.", indicatorQueryResp.getTumorTypeSummary());

        // Test indicator endpoint supports HGVS
        query = new Query(null, null, null, null, null, null, null, "Melanoma", null, null, null, "7:g.140453136A>T");
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertTrue("The geneExist is not true, but it should be.", indicatorQueryResp.getGeneExist() == true);
        assertEquals("The oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level is not 1, but it should be.", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());

        query1 = new Query(null, null, null, null, null, null, null, "Melanoma", null, null, null, "7:g.140453136A>T");
        query2 = new Query(null, null, null, "BRAF", "V600E", null, null, "Melanoma", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        assertTrue("Genes are not the same, but they should.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
        assertTrue("Oncogenicities are not the same, but they should.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("Treatments are not the same, but they should.", resp1.getTreatments().equals(resp2.getTreatments()));
        assertTrue("Highest sensitive levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel()));
        assertTrue("Highest resistance levels are not the same, but they should.", LevelUtils.areSameLevels(resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel()));

        // Test HGVS has higher priority than gene/variant pair
        // 7:g.140453136A>T is BRAF V600E
        query = new Query(null, null, null, "ALK", "R401Q", null, null, "Melanoma", null, null, null, "7:g.140453136A>T");
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertTrue("The geneExist is not true, but it should be.", indicatorQueryResp.getGeneExist() == true);
        assertEquals("The oncogenicity is not Oncogenic, but it should be.", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level is not 1, but it should be.", LevelOfEvidence.LEVEL_1, indicatorQueryResp.getHighestSensitiveLevel());

        // Check structural variant fusion
        // a) BRAF is oncogenic gene. No Truncating Mutations is curated.
        query = new Query(null, null, null, "BRAF-BRAF", null, "structural_variant", StructuralVariantType.INSERTION, "Ovarian Cancer", "fusion", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The highest sensitive level of BRAF insertion should be Level 3A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of BRAF insertion functional fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        query = new Query(null, null, null, "BRAF-BRAF", null, "structural_variant", StructuralVariantType.INSERTION, "Ovarian Cancer", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The highest sensitive level of BRAF insertion should be null", null, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of BRAF insertion non-functional fusion should be empty", "", indicatorQueryResp.getOncogenic());

        // b)  KMT2A is tumor suppressor gene, it has both fusions and Truncating Mutations curated. Functional fusion should have fusions mapped, non-functional genes should have Truncating Mutations mapped. And both of them are likely oncogenic.
        query = new Query(null, null, null, "KMT2A-MLLT3", null, "structural_variant", StructuralVariantType.TRANSLOCATION, "Acute Myeloid Leukemia", "fusion", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The oncogenicity of KMT2A translocation functional fusion should be likely oncogenic", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());
        query = new Query(null, null, null, "KMT2A-MLLT3", null, "structural_variant", StructuralVariantType.TRANSLOCATION, "Acute Myeloid Leukemia", "fusion", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The oncogenicity of KMT2A translocation non-functional fusion should be likely oncogenic", Oncogenicity.YES.getOncogenic(), indicatorQueryResp.getOncogenic());

        // Test for the newly added gene
        query = new Query(null, null, null, "KLF5", "P301S", null, null, "Acute Myeloid Leukemia", null, null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
        assertEquals("The oncogenicity is not likely oncogenic, but it should be.", Oncogenicity.LIKELY.getOncogenic(), indicatorQueryResp.getOncogenic());


        // Test Structural Variants
        // Fusion as alteration type should have same result from structural variant as alteration type and fusion as consequence
        query1 = new Query(null, null, null, "EGFR-RAD51", null, "fusion", null, "Ovarian Cancer", null, null, null, null);
        query2 = new Query(null, null, null, "EGFR-RAD51", null, "structural_variant", StructuralVariantType.INSERTION, "Ovarian Cancer", "fusion", null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        pairComparison(resp1, resp2);

        // When the structural variant is functional fusion, the alteration name if is fusion, it should be ignored.
        query1 = new Query(null, null, null, "KIF5B-MET", null, "structural_variant", StructuralVariantType.DELETION, "Lung Adenocarcinoma", "fusion", null, null, null);
        query2 = new Query(null, null, null, "KIF5B-MET", "FUSION", "structural_variant", StructuralVariantType.DELETION, "Lung Adenocarcinoma", "fusion", null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        pairComparison(resp1, resp2);

        // When queried gene order is different, but one of the combinations exists, then the variantExist should be set to true
        query1 = new Query(null, null, null, "BCR-ABL1", null, "structural_variant", StructuralVariantType.DELETION, "Lung Adenocarcinoma", "fusion", null, null, null);
        query2 = new Query(null, null, null, "ABL1-BCR", null, "structural_variant", StructuralVariantType.DELETION, "Lung Adenocarcinoma", "fusion", null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        pairComparison(resp1, resp2);

        // handle mixed input for structural variant deletion
        query1 = new Query(null, null, null, "EGFR", "KDD", "structural_variant", StructuralVariantType.DUPLICATION, "NSCLC", null, null, null, null);
        query2 = new Query(null, null, null, "EGFR", "KDD", "structural_variant", StructuralVariantType.DUPLICATION, "NSCLC", "KDD", null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        pairComparison(resp1, resp2);

        query1 = new Query(null, null, null, "EGFR", "KDD", null, null, "NSCLC", null, null, null, null);
        query2 = new Query(null, null, null, "EGFR", "KDD", "structural_variant", StructuralVariantType.DUPLICATION, "NSCLC", "fusion", null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        pairComparison(resp1, resp2);

        // test RDD, it used to belong to Histiocytic Disorder, now under Histiocytosis
        query1 = new Query(null, null, null, "MAP2K1", "C121S", null, null, "RDD", null, null, null, null);
        query2 = new Query(null, null, null, "MAP2K1", "C121S", null, null, "Histiocytosis", null, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        pairComparison(resp1, resp2);

        // Test positional variant but with a missense variant consequence
        query1 = new Query(null, null, null, "TP53", "R248", null, null, "RDD", null, null, null, null);
        query2 = new Query(null, null, null, "TP53", "R248", null, null, "RDD", MISSENSE_VARIANT, null, null, null);
        resp1 = IndicatorUtils.processQuery(query1, null, true, null);
        resp2 = IndicatorUtils.processQuery(query2, null, true, null);
        pairComparison(resp1, resp2);

    }

    private void pairComparison(IndicatorQueryResp resp1, IndicatorQueryResp resp2) {
        pairComparison(resp1, resp1, false);
    }

    private void pairComparison(IndicatorQueryResp resp1, IndicatorQueryResp resp2, boolean skipSummary) {
        assertTrue("The gene exist should be the same.", resp1.getGeneExist().equals(resp2.getGeneExist()));
        assertTrue("The variant exist should be the same.", resp1.getVariantExist().equals(resp2.getVariantExist()));
        assertTrue("The oncogenicity should be the same.", resp1.getOncogenic().equals(resp2.getOncogenic()));
        assertTrue("The VUS info should be the same", resp1.getVUS().equals(resp2.getVUS()));

        if (resp1.getHighestSensitiveLevel() == null || resp2.getHighestSensitiveLevel() == null) {
            assertTrue("The highest sensitive level should be the same.", resp1.getHighestSensitiveLevel() == resp2.getHighestSensitiveLevel());
        } else {
            assertTrue("The highest sensitive level should be the same.", resp1.getHighestSensitiveLevel().equals(resp2.getHighestSensitiveLevel()));
        }

        if (resp1.getHighestResistanceLevel() == null || resp2.getHighestResistanceLevel() == null) {
            assertTrue("The highest sensitive level should be the same.", resp1.getHighestResistanceLevel() == resp2.getHighestResistanceLevel());
        } else {
            assertTrue("The highest resistance level should be the same.", resp1.getHighestResistanceLevel().equals(resp2.getHighestResistanceLevel()));
        }

        if (!skipSummary) {
            assertTrue("The gene summary should be the same.", resp1.getGeneSummary().equals(resp2.getGeneSummary()));
            assertTrue("The variant summary should be the same.", resp1.getVariantSummary().equals(resp2.getVariantSummary()));
            assertTrue("The tumor type summary should be the same.", resp1.getTumorTypeSummary().equals(resp2.getTumorTypeSummary()));
        }
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
