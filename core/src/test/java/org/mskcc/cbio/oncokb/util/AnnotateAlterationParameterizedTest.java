package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mskcc.cbio.oncokb.util.TestUtils.getTestFileBufferedReader;

@RunWith(Parameterized.class)
public class AnnotateAlterationParameterizedTest {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String EXAMPLES_PATH = "src/test/resources/test_annotate_alterations.tsv";

    private String proteinChange;
    private String expectedConsequence;
    private String expectedRefAllele;
    private String expectedVarAllele;
    private String expectedProteinStart;
    private String expectedProteinEnd;

    public AnnotateAlterationParameterizedTest(String proteinChange, String expectedConsequence, String expectedRefAllele, String expectedVarAllele, String expectedProteinStart, String expectedProteinEnd) {
        this.proteinChange = proteinChange;
        this.expectedConsequence = expectedConsequence;
        this.expectedRefAllele = expectedRefAllele;
        this.expectedVarAllele = expectedVarAllele;
        this.expectedProteinStart = expectedProteinStart;
        this.expectedProteinEnd = expectedProteinEnd;
    }

    @Test
    public void testSummary() {
        Alteration alteration = new Alteration();

        AlterationUtils.annotateAlteration(alteration, proteinChange);
        testSuite(alteration, proteinChange, expectedConsequence, expectedRefAllele, expectedVarAllele, expectedProteinStart, expectedProteinEnd);

        // match needs to be case-insensitive
        alteration = new Alteration();
        AlterationUtils.annotateAlteration(alteration, proteinChange.toLowerCase());
        testSuite(alteration, proteinChange, expectedConsequence, expectedRefAllele, expectedVarAllele, expectedProteinStart, expectedProteinEnd);
    }

    private void testSuite(Alteration annotatedAlteration, String proteinChange, String expectedConsequence, String expectedRefAllele, String expectedVarAllele, String expectedProteinStart, String expectedProteinEnd) {
        assertEquals("Not expected consequence. Query: " + proteinChange, expectedConsequence, annotatedAlteration.getConsequence() == null ? "" : annotatedAlteration.getConsequence().getTerm());
        assertEquals("Not expected ref allele. Query: " + proteinChange, expectedRefAllele, StringUtils.isEmpty(annotatedAlteration.getRefResidues()) ? "" : annotatedAlteration.getRefResidues());
        assertEquals("Not expected var allele. Query: " + proteinChange, expectedVarAllele, StringUtils.isEmpty(annotatedAlteration.getVariantResidues()) ? "" : annotatedAlteration.getVariantResidues());
        assertEquals("Not expected protein start. Query: " + proteinChange, expectedProteinStart, annotatedAlteration.getProteinStart() == null ? "" : Integer.toString(annotatedAlteration.getProteinStart()));
        assertEquals("Not expected protein end. Query: " + proteinChange, expectedProteinEnd, annotatedAlteration.getProteinEnd() == null ? "" : Integer.toString(annotatedAlteration.getProteinEnd()));
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return importer();
    }

    private static List<String[]> importer() throws IOException {
        BufferedReader buf = getTestFileBufferedReader(EXAMPLES_PATH);
        String line = buf.readLine();

        List<String[]> queries = new ArrayList<>();
        int count = 0;
        while (line != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                try {
                    String parts[] = line.split("\t");
                    if (parts.length < 1) {
                        throw new IllegalArgumentException("Test case should have at least protein change. Current case: " + line);
                    }
                    String proteinChange = parts[0];
                    String expectedConsequence = parts.length > 1 ? parts[1] : "";
                    String expectedRefAllele = parts.length > 2 ? parts[2] : "";
                    String expectedVarAllele = parts.length > 3 ? parts[3] : "";
                    String expectedProteinStart = parts.length > 4 ? parts[4] : "";
                    String expectedProteinEnd = parts.length > 5 ? parts[5] : "";
                    String[] query = {proteinChange, expectedConsequence, expectedRefAllele, expectedVarAllele, expectedProteinStart, expectedProteinEnd};
                    queries.add(query);
                    count++;
                } catch (Exception e) {
                    LOGGER.error("Could not add line '{}'.", line, e);
                }
            }
            line = buf.readLine();
        }
        LOGGER.info("Contains {} queries.", count);
        LOGGER.info("Done.");

        return queries;
    }
}
