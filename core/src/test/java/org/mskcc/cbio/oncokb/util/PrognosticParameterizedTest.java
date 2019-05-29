package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.apiModels.Implication;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.Query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by Hongxin
 */
@RunWith(Parameterized.class)
public class PrognosticParameterizedTest {
    private static String TUMOR_TYPE_SUMMARY_EXAMPLES_PATH = "src/test/resources/test_prognostic.tsv";

    private String gene;
    private String variant;
    private String tumorType;
    private String prognosticSummary;
    private String prognosticImplicationLevel;

    public PrognosticParameterizedTest(String gene, String variant, String tumorType, String prognosticSummary, String prognosticImplicationLevel) {
        this.gene = gene;
        this.variant = variant;
        this.tumorType = tumorType;
        this.prognosticSummary = prognosticSummary;
        this.prognosticImplicationLevel = prognosticImplicationLevel;
    }

    @Test
    public void testSummary() throws Exception {
        Query query = new Query();
        query.setAlteration(variant);
        query.setHugoSymbol(gene);
        query.setTumorType(tumorType);

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, null, null, false, null);
        String _query = gene + " " + variant + " " + tumorType;
        String _prognosticSummary = resp.getPrognosticSummary();
        List<Implication> _prognosticImplications = resp.getPrognosticImplications();

        assertEquals("Prognostic summary, Query: " + _query, prognosticSummary, _prognosticSummary);
        Set<String> levels = _prognosticImplications.stream().map(implication -> implication.getLevelOfEvidence().getLevel()).collect(Collectors.toSet());
        if(levels.size() > 0) {
            assertTrue("Prognostic implication level, Query: " + _query, levels.contains(prognosticImplicationLevel));
        } else {
            assertEquals("Prognostic implication level, Query: " + _query, prognosticImplicationLevel, "");
        }
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
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("Missing a tumor type summary query attribute, parts: " + parts.length);
                    }
                    String gene = parts[0];
                    String variant = parts[1];
                    String tumorType = parts[2];
                    String prognosticSummary = parts.length > 3 ? parts[3] : "";
                    String prognosticLevel = parts.length > 4 ? parts[4] : "";
                    String[] query = {gene, variant, tumorType, prognosticSummary, prognosticLevel};
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
