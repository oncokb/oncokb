package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.StructuralVariantType;

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
public class StructuralVariantParameterizedTest {
    private static String EXAMPLES_PATH = "src/test/resources/test_structural_variants.tsv";

    private String fusionPair;
    private String alteration;
    private String svClass;
    private String tumorType;
    private Boolean isFunctionalFusion;
    private String oncogenicity;
    private String geneSummary;
    private String variantSummary;
    private String tumorTypeSummary;

    public StructuralVariantParameterizedTest(String fusionPair, String alteration, String svClass, String tumorType, String isFunctionalFusion, String oncogenicity, String geneSummary, String variantSummary, String tumorTypeSummary) {
        this.fusionPair = fusionPair;
        this.alteration = alteration;
        this.svClass = svClass;
        this.tumorType = tumorType;
        this.isFunctionalFusion = Boolean.valueOf(isFunctionalFusion);
        this.oncogenicity = oncogenicity;
        this.geneSummary = geneSummary;
        this.variantSummary = variantSummary;
        this.tumorTypeSummary = tumorTypeSummary;
    }

    @Test
    public void testSummary() throws Exception {
        String _query = fusionPair + " " + alteration + " " + svClass + " " + tumorType + " " + isFunctionalFusion;

        // Testing functional fusion
        if (isFunctionalFusion) {
            Query query1 = new Query();
            query1.setAlterationType("structural_variant");
            query1.setAlteration(alteration);
            query1.setConsequence("fusion");
            query1.setSvType(StructuralVariantType.valueOf(svClass));
            query1.setHugoSymbol(fusionPair);
            query1.setTumorType(tumorType);

            Query query2 = new Query();
            query2.setHugoSymbol(fusionPair);
            query2.setAlteration(alteration);
            query2.setAlterationType("fusion");
            query2.setTumorType(tumorType);
            query2.setSvType(StructuralVariantType.valueOf(svClass));

            // if it is functional fusion. The result should be the same as passing as fusion
            IndicatorQueryResp resp1 = IndicatorUtils.processQuery(query1, null, true, null, false);
            IndicatorQueryResp resp2 = IndicatorUtils.processQuery(query2, null, false, null, false);

            assertEquals("Oncogenicities are not matched. Query: " + _query, resp1.getOncogenic(), resp2.getOncogenic());
            assertEquals("Highest sensitive levels are not matched. Query: " + _query, resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel());
            assertEquals("Highest sensitive levels are not matched. Query: " + _query, resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel());
            assertEquals("Highest resistance levels are not matched. Query: " + _query, resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel());
        }

        Query query = new Query();
        query.setAlterationType("structural_variant");
        query.setAlteration(alteration);
        query.setSvType(StructuralVariantType.valueOf(svClass));
        query.setHugoSymbol(fusionPair);
        query.setTumorType(tumorType);
        if (isFunctionalFusion) {
            query.setConsequence("fusion");
        }
        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, true, null, false);
//        System.out.println("New: " + fusionPair + "&&" + alteration + "&&" + svClass + "&&" + tumorType + "&&" + isFunctionalFusion.toString().toUpperCase() + "&&" + resp.getOncogenic() + "&&" + resp.getGeneSummary() + "&&" + resp.getVariantSummary() + "&&" + resp.getTumorTypeSummary());

        assertEquals("Oncogenicities are not matched. Query: " + _query, oncogenicity, resp.getOncogenic());
        assertEquals("Gene summaries are not matched. Query: " + _query, geneSummary, resp.getGeneSummary());
        assertEquals("Variant Summaries are not matched. Query: " + _query, variantSummary, resp.getVariantSummary());
        assertEquals("Tumor Type Summaries are not matched. Query: " + _query, tumorTypeSummary, resp.getTumorTypeSummary());
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return importer();
    }

    private static List<String[]> importer() throws IOException {
        if (EXAMPLES_PATH == null) {
            System.out.println("Please specify the testing file path");
            return null;
        }

        File file = new File(EXAMPLES_PATH);
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();

        List<String[]> queries = new ArrayList<>();
        int count = 0;
        while (line != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                try {
                    String parts[] = line.split("\t");
                    if (parts.length < 8) {
                        throw new IllegalArgumentException("Test case should have at least 8 columns. Current case: " + line);
                    }
                    String fusionPair = parts[0];
                    String alteration = parts[1];
                    String svClass = parts[2];
                    String tumorType = parts[3];
                    String isFunctionalFusion = parts[4];
                    String oncogenicity = parts[5];
                    String geneSummary = parts[6];
                    String variantSummary = parts[7];
                    String tumorTypeSummary = parts[8];
                    String[] query = {fusionPair, alteration, svClass, tumorType, isFunctionalFusion, oncogenicity, geneSummary, variantSummary, tumorTypeSummary};
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
