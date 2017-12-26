package org.mskcc.cbio.oncokb.util;

import org.junit.Assert;
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
    private static String EXAMPLES_PATH = "src/test/resources/test_structural_variants.txt";

    private String fusionPair;
    private String svClass;
    private String tumorType;
    private Boolean isFunctionalFusion;

    public StructuralVariantParameterizedTest(String fusionPair, String svClass, String tumorType, String isFunctionalFusion) {
        this.fusionPair = fusionPair;
        this.svClass = svClass;
        this.tumorType = tumorType;
        this.isFunctionalFusion = Boolean.valueOf(isFunctionalFusion);
    }

    @Test
    public void testSummary() throws Exception {
        // Testing functional fusion
        if(isFunctionalFusion) {
            Query query1 = new Query();
            query1.setAlterationType("structural_variant");
            query1.setConsequence("fusion");
            query1.setSvType(StructuralVariantType.valueOf(svClass));
            query1.setHugoSymbol(fusionPair);
            query1.setTumorType(tumorType);

            Query query2 = new Query();
            query2.setHugoSymbol(fusionPair);
            query2.setAlterationType("fusion");
            query2.setTumorType(tumorType);

            // if it is functional fusion. The result should be the same as passing as fusion
            IndicatorQueryResp resp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
            IndicatorQueryResp resp2 = IndicatorUtils.processQuery(query2, null, null, null, false);

            assertEquals("Oncogenicities are not matched.", resp1.getOncogenic(), resp2.getOncogenic());
            assertEquals("Highest sensitive levels are not matched.", resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel());
            assertEquals("Highest sensitive levels are not matched.", resp1.getHighestSensitiveLevel(), resp2.getHighestSensitiveLevel());
            assertEquals("Highest resistance levels are not matched.", resp1.getHighestResistanceLevel(), resp2.getHighestResistanceLevel());
        } else {
            // If not functional, the result should be the same with querying Truncating Mutations.
            // But this logic will be tested separated due to the complexity of the fusion pairs.
            // The logic will be tested in IndicatorUtilsTest.
//            Query query1 = new Query();
//            query1.setAlterationType("structural_variant");
//            query1.setSvType(StructuralVariantType.valueOf(svClass));
//            query1.setHugoSymbol(fusionPair);
//            query1.setTumorType(tumorType);
//
//            IndicatorQueryResp resp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
//
//            System.out.println("Non-functional: " + resp1.getOncogenic());
//            System.out.println(resp1.getTumorTypeSummary());
        }
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
                    if (parts.length < 4) {
                        throw new IllegalArgumentException("Test case should have at least fusion variant pair, sv class, tumor type and isFunctionalFusion. Current case: " + line);
                    }
                    String fusionPair = parts[0];
                    String svClass = parts[1];
                    String tumorType = parts[2];
                    String isFunctionalFusion = parts[3];
                    String[] query = {fusionPair, svClass, tumorType, isFunctionalFusion};
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
