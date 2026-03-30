package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.SomaticIndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class VariantSummaryUtilsParameterizedTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(VariantSummaryUtilsParameterizedTest.class);
    private static String VARIANT_SUMMARY_EXAMPLES_PATH = "src/test/resources/test_variant_summaries.tsv";

    private String gene;
    private String variant;
    private String oncogenicity;
    private String variantSummary;

    public VariantSummaryUtilsParameterizedTest(String gene, String variant, String oncogenicity, String variantSummary) {
        this.gene = gene;
        this.variant = variant;
        this.oncogenicity = oncogenicity;
        this.variantSummary = variantSummary;
    }

    @Test
    public void testSummary() throws Exception {
        Query query = new Query();
        query.setHugoSymbol(gene);
        query.setAlteration(variant);

        SomaticIndicatorQueryResp resp = IndicatorUtils.processQuerySomatic(query, null, false, null, false);
        String _query = gene + " " + variant;
        String _oncogenicity = resp.getOncogenic();
        String _variantSummary = resp.getVariantSummary();
        assertEquals("Oncogenicity, Query: " + _query, oncogenicity, _oncogenicity);
        assertEquals("Variant summary, Query: " + _query, variantSummary, _variantSummary);
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return importer();
    }

    private static List<String[]> importer() throws IOException {
        if (VARIANT_SUMMARY_EXAMPLES_PATH == null) {
            LOGGER.error("Please specify the testing file path");
            return null;
        }

        File file = new File(VARIANT_SUMMARY_EXAMPLES_PATH);
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();

        List<String[]> queries = new ArrayList<>();
        int count = 0;
        while (line != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                try {
                    String parts[] = line.split("\t");
                    if (parts.length < 2) {
                        throw new IllegalArgumentException("Missing test elements, parts: " + parts.length);
                    }
                    String gene = parts[0];
                    String variant = parts[1];
                    String oncogenic = parts.length > 2 ? parts[2] : "";
                    String variantSummary = parts.length > 3 ? parts[3] : "";
                    String[] query = {gene, variant, oncogenic, variantSummary};
                    queries.add(query);
                    count++;
                } catch (Exception e) {
                    LOGGER.error("Could not add line '{}'. ", line, e);
                }
            }
            line = buf.readLine();
        }
        LOGGER.info("Contains {} queries.", count);
        LOGGER.info("Done.");

        return queries;
    }
}
