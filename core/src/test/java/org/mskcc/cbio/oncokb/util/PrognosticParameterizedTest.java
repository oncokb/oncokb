package org.mskcc.cbio.oncokb.util;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.apiModels.Implication;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
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
    private String numOfPrognosticImplications;

    public PrognosticParameterizedTest(String gene, String variant, String tumorType, String prognosticSummary, String prognosticImplicationLevel, String numOfPrognosticImplications) {
        this.gene = gene;
        this.variant = variant;
        this.tumorType = tumorType;
        this.prognosticSummary = prognosticSummary;
        this.prognosticImplicationLevel = prognosticImplicationLevel;
        this.numOfPrognosticImplications = numOfPrognosticImplications;
    }

    // Temporary disable the test due to lack of the data
    @Test
    public void testSummary() throws Exception {
        Query query = new Query();
        query.setAlteration(variant);
        query.setHugoSymbol(gene);
        query.setTumorType(tumorType);

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, false, null, false);
        String _query = gene + " " + variant + " " + tumorType;
        String _prognosticSummary = resp.getPrognosticSummary();
        LevelOfEvidence theHighestPrognosticImplicationLevel = resp.getHighestPrognosticImplicationLevel();
        List<Implication> implications = resp.getPrognosticImplications();

        assertEquals("Prognostic summary, Query: " + _query, prognosticSummary, _prognosticSummary);
        assertEquals("Prognostic implication level, Query: " + _query, prognosticImplicationLevel, theHighestPrognosticImplicationLevel == null ? "" : theHighestPrognosticImplicationLevel.getLevel());
        assertEquals("Number of prognostic implications, Query: " + _query, numOfPrognosticImplications, Integer.toString(implications.size()));
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
                    if (parts.length < 6) {
                        throw new IllegalArgumentException("Missing a tumor type summary query attribute, parts: " + parts.length);
                    }
                    String gene = parts[0];
                    String variant = parts[1];
                    String tumorType = parts[2];
                    String prognosticSummary = parts[3];
                    String prognosticLevel = parts[4];
                    String numOfPrognosticImplications = parts[5];
                    String[] query = {gene, variant, tumorType, prognosticSummary, prognosticLevel, numOfPrognosticImplications};
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
