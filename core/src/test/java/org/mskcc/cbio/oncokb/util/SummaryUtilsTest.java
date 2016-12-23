package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.model.VariantQuery;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Hongxin on 12/5/16.
 */
public class SummaryUtilsTest {
    private String TUMOR_TYPE_SUMMARY_EXAMPLES_PATH = "src/test/resources/test_tumor_type_summaries.txt";

    @Test
    public void testSummary() throws Exception {
        List<Map<String, String>> queries = importer();

        if (queries != null) {
            for (Map<String, String> query : queries) {
                VariantQuery variantQuery = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null,
                    query.get("gene"), query.get("variant"), query.get("tumorType"), null,
                    null, null, "cbioportal").get(0);
                String geneSummary = SummaryUtils.geneSummary(variantQuery.getGene());
                String variantSummary = SummaryUtils.oncogenicSummary(variantQuery.getGene(), variantQuery.getAlterations(), variantQuery.getQueryAlteration(), false);
                String tumorTypeSummary = SummaryUtils.tumorTypeSummary(variantQuery.getGene(), variantQuery.getQueryAlteration(), variantQuery.getAlterations(), variantQuery.getQueryTumorType(), new HashSet<>(variantQuery.getTumorTypes()));
                assertEquals("Gene summary on " + variantQuery.getGene() + " " + variantQuery.getQueryAlteration() + " " + variantQuery.getQueryTumorType(), query.get("geneSummary"), geneSummary);
                assertEquals("Variant summary on " + variantQuery.getGene() + " " + variantQuery.getQueryAlteration() + " " + variantQuery.getQueryTumorType(), query.get("variantSummary"), variantSummary);
                assertEquals("TumorType summary on " + variantQuery.getGene() + " " + variantQuery.getQueryAlteration() + " " + variantQuery.getQueryTumorType(), query.get("tumorTypeSummary"), tumorTypeSummary);
            }
        }
    }

    private List<Map<String, String>> importer() throws IOException {
        if (TUMOR_TYPE_SUMMARY_EXAMPLES_PATH == null) {
            System.out.println("Please specify the testing file path");
            return null;
        }

        File file = new File(TUMOR_TYPE_SUMMARY_EXAMPLES_PATH);
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();

        List<Map<String, String>> queries = new ArrayList<>();
        int count = 0;
        while (line != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                try {
                    String parts[] = line.split("\t");
                    if (parts.length != 6) {
                        throw new IllegalArgumentException("Missing a tumor type summary query attribute, parts: " + parts.length);
                    }
                    String gene = parts[0];
                    String variant = parts[1];
                    String tumorType = parts[2];
                    String geneSummary = parts[3];
                    String variantSummary = parts[4];
                    String tumorTypeSummary = parts[5];
                    Map<String, String> query = new HashedMap();
                    query.put("gene", gene);
                    query.put("variant", variant);
                    query.put("tumorType", tumorType);
                    query.put("geneSummary", geneSummary);
                    query.put("variantSummary", variantSummary);
                    query.put("tumorTypeSummary", tumorTypeSummary);
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
