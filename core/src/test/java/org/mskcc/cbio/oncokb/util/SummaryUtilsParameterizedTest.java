package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.Query;

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
    private static String TUMOR_TYPE_SUMMARY_EXAMPLES_PATH = "src/test/resources/test_tumor_type_summaries.tsv";

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

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, false, null, false);
        String _query = gene + " " + variant + " " + tumorType;
        String _geneSummary = resp.getGeneSummary();
        String _variantSummary = resp.getVariantSummary();
        String _tumorTypeSummary = resp.getTumorTypeSummary();
//        System.out.println("New: " + gene + "&&" + variant + "&&" + tumorType + "&&" + _geneSummary + "&&" + _variantSummary + "&&" + _tumorTypeSummary);
        assertEquals("Gene summary, Query: " + _query, geneSummary, _geneSummary);
        assertEquals("Variant summary, Query: " + _query, variantSummary, _variantSummary);
        assertEquals("Tumor Type summary, Query: " + _query, tumorTypeSummary, _tumorTypeSummary);
    }
    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return importer();
    }

    private static List<String[]> importer() throws IOException {
        if (TUMOR_TYPE_SUMMARY_EXAMPLES_PATH == null) {
            System.out.println("Please specify the testing file path");
            return null;
        }

        File file = new File(TUMOR_TYPE_SUMMARY_EXAMPLES_PATH);
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();

        List<String[]> queries = new ArrayList<>();
        int count = 0;
        while (line != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                try {
                    String parts[] = line.split("\t");
                    if (parts.length < 4) {
                        throw new IllegalArgumentException("Missing a tumor type summary query attribute, parts: " + parts.length);
                    }
                    String gene = parts[0];
                    String variant = parts[1];
                    String tumorType = parts[2];
                    String geneSummary = parts[3];
                    String variantSummary = parts.length > 4 ? parts[4] : "";
                    String tumorTypeSummary = parts.length > 5 ? parts[5] : "";
                    String[] query = {gene, variant, tumorType, geneSummary, variantSummary, tumorTypeSummary};
                    queries.add(query);
                    count++;
                } catch (Exception e) {
                    System.err.println("Could not add line '" + line + "'. " + e);
                }
            }
            line = buf.readLine();
        }
        System.err.println("Contains " + count + " tumor type summary queries.");
        System.err.println("Done.");

        return queries;
    }
}
