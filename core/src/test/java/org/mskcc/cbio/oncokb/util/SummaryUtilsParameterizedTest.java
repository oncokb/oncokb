package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.Query;
import util.FileImporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Hongxin on 12/5/16.
 */
@RunWith(Parameterized.class)
public class SummaryUtilsParameterizedTest {
    private String gene;
    private String variant;
    private String tumorType;
    private String geneSummary;
    private String variantSummary;
    private String tumorTypeSummary;

    public SummaryUtilsParameterizedTest(String gene, String variant, String tumorType, String geneSummary, String variantSummary, String tumorTypeSummary) {
        this.gene = gene;
        this.variant = variant;
        this.tumorType = tumorType;
        this.geneSummary = geneSummary;
        this.variantSummary = variantSummary;
        this.tumorTypeSummary = tumorTypeSummary;
    }

    @Test
    public void testSummary() throws Exception {
        Query query = new Query();
        query.setAlteration(variant);
        query.setHugoSymbol(gene);
        query.setTumorType(tumorType);

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, null, null, false);
        String _query = gene + " " + variant + " " + tumorType;
        String _geneSummary = resp.getGeneSummary();
        String _variantSummary = resp.getVariantSummary();
        String _tumorTypeSummary = resp.getTumorTypeSummary();
        assertEquals("Gene summary, Query: " + _query, geneSummary, _geneSummary);
        assertEquals("Variant summary, Query: " + _query, variantSummary, _variantSummary);
        assertEquals("Tumor Type summary, Query: " + _query, tumorTypeSummary, _tumorTypeSummary);
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return FileImporter.tumorTypeSummaryimporter();
    }
}
