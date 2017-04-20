package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.apiModels.LevelOfEvidenceWithTime;
import org.mskcc.cbio.oncokb.apiModels.SearchResult;
import org.mskcc.cbio.oncokb.apiModels.TreatmentInfo;
import org.mskcc.cbio.oncokb.model.*;
import util.FileImporter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Hongxin on 4/18/17.
 */
@RunWith(Parameterized.class)
public class QueryAnnotationParameterizedTest {
    private String gene;
    private String variant;
    private String tumorType;
    private String geneSummary;
    private String variantSummary;
    private String tumorTypeSummary;


    public QueryAnnotationParameterizedTest(String gene, String variant, String tumorType, String geneSummary, String variantSummary, String tumorTypeSummary) {
        this.gene = gene;
        this.variant = variant;
        this.tumorType = tumorType;
        this.geneSummary = geneSummary;
        this.variantSummary = variantSummary;
        this.tumorTypeSummary = tumorTypeSummary;
    }

    @Test
    public void testAnnotateSearchQuery() throws Exception {
        Query query = new Query();
        query.setHugoSymbol(gene);
        query.setAlteration(variant);
        query.setTumorType(tumorType);

        SearchResult searchResult = QueryAnnotation.annotateSearchQuery(query);
        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, null, null, false);

        assertEquals("Oncogenicity should be the same. " + query.toString(), resp.getOncogenic() == "" ? "Unknown" : resp.getOncogenic(), searchResult.getOncogenic() == null ? "" : searchResult.getOncogenic().getKnownEffect());
        assertTrue("Gene exist should match. " + query.toString(), searchResult.getGeneAnnotated().equals(resp.getGeneExist()));
        assertTrue("Variant exist should match. " + query.toString(), searchResult.getVariantAnnotated().equals(resp.getVariantExist()));
        assertTrue("Alternative allele exist should match. " + query.toString(), searchResult.getAlternativeVariantAlleleAnnotated().equals(resp.getAlleleExist()));

        assertTrue("Gene summary should match. " + query.toString(), searchResult.getGeneSummary().getSummary().equals(resp.getGeneSummary()));
        assertTrue("Variant summary should match. " + query.toString(), searchResult.getVariantSummary().getSummary().equals(resp.getVariantSummary()));
        assertTrue("Tumor type summary should match. " + query.toString(), searchResult.getTumorTypeSummary().getSummary().equals(resp.getTumorTypeSummary()));

        assertEquals("Highest sensitive level should match. " + query.toString(), resp.getHighestSensitiveLevel(), searchResult.getHighestSensitiveLevel() == null ? null : searchResult.getHighestSensitiveLevel().getLevel());
        assertEquals("Highest Resistance level should match. " + query.toString(), resp.getHighestResistanceLevel(), searchResult.getHighestResistanceLevel() == null ? null : searchResult.getHighestResistanceLevel().getLevel());
        assertTrue("Other highest sensitive level should match. " + query.toString(), (resp.getOtherSignificantSensitiveLevels() == null ? new HashSet<>() : new HashSet<>(resp.getOtherSignificantSensitiveLevels())).equals(convertLevelofEvidenceWithTime(searchResult.getOtherSignificantSensitiveLevels())));
        assertTrue("Other highest resistance level should match. " + query.toString(), (resp.getOtherSignificantResistanceLevels() == null ? new HashSet<>() : new HashSet<>(resp.getOtherSignificantResistanceLevels())).equals(convertLevelofEvidenceWithTime(searchResult.getOtherSignificantResistanceLevels())));

        assertEquals("Treatment length should be the same. " + query.toString(), resp.getTreatments().size(), searchResult.getTreatments().size());
        assertEquals("Treatment names should be the same. " + query.toString(), TreatmentUtils.getTreatmentName(getIndicatorTreatments(resp.getTreatments()), true), TreatmentUtils.getTreatmentName(getTreatments(searchResult.getTreatments()), true));


        assertTrue("VUS should match. " + query.toString(), searchResult.getVUS().getStatus().equals(resp.getVUS()));
        assertTrue("Hotspot should match. " + query.toString(), searchResult.getOtherSources().getHotspot().equals(resp.getHotspot()));
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return FileImporter.tumorTypeSummaryimporter();
    }

    private Set<Treatment> getTreatments(List<TreatmentInfo> treatmentInfos) {
        Set<Treatment> treatments = new HashSet<>();
        for (TreatmentInfo treatmentInfo : treatmentInfos) {
            Treatment newTreatment = new Treatment();
            newTreatment.setDrugs(treatmentInfo.getDrugs());
            treatments.add(newTreatment);
        }
        return treatments;
    }

    private Set<Treatment> getIndicatorTreatments(List<IndicatorQueryTreatment> treatmentInfos) {
        Set<Treatment> treatments = new HashSet<>();
        for (IndicatorQueryTreatment indicatorQueryTreatment : treatmentInfos) {
            Treatment newTreatment = new Treatment();
            newTreatment.setDrugs(indicatorQueryTreatment.getDrugs());
            treatments.add(newTreatment);
        }
        return treatments;
    }

    private Set<LevelOfEvidence> convertLevelofEvidenceWithTime(List<LevelOfEvidenceWithTime> levelOfEvidenceWithTimes) {
        Set<LevelOfEvidence> levelOfEvidences = new HashSet<>();
        if (levelOfEvidenceWithTimes == null)
            return levelOfEvidences;
        for (LevelOfEvidenceWithTime levelOfEvidenceWithTime : levelOfEvidenceWithTimes) {
            levelOfEvidences.add(levelOfEvidenceWithTime.getLevel());
        }
        return levelOfEvidences;
    }
}
