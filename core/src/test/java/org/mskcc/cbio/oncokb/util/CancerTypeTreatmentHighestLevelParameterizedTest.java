package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.IndicatorQueryTreatment;
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
public class CancerTypeTreatmentHighestLevelParameterizedTest {
    private static String TREATMENT_EXAMPLES_PATH = "src/test/resources/test_cancer_type_treatments_highest_level.tsv";

    private String gene;
    private String variant;
    private String tumorType;
    private String treatmentLevel;

    public CancerTypeTreatmentHighestLevelParameterizedTest(String gene, String variant, String tumorType, String treatmentLevel) {
        this.gene = gene;
        this.variant = variant;
        this.tumorType = tumorType;
        this.treatmentLevel = treatmentLevel;
    }

    @Test
    public void testSummary() throws Exception {
        Query query = new Query();
        query.setAlteration(variant);
        query.setHugoSymbol(gene);
        query.setTumorType(tumorType);

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, true, null, false);
        List<IndicatorQueryTreatment> resps = resp.getTreatments();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < resps.size(); i++) {
            IndicatorQueryTreatment treatment = resps.get(i);
            List<String> drugNames = new ArrayList<>();
            for (Drug drug : treatment.getDrugs()) {
                if (drug.getDrugName() != null) {
                    drugNames.add(drug.getDrugName().trim());
                }
            }
            stringBuilder.append(treatment.getLevel().getLevel());
            stringBuilder.append(": ");
            stringBuilder.append(MainUtils.listToString(drugNames, "+"));
            stringBuilder.append("; ");
        }
        String tl = stringBuilder.toString().trim();

//        System.out.println("New: " + gene + "&&" + variant + "&&" + tumorType + "&&" + tl);

        assertEquals("Gene summary, Query: " + gene + " " + variant + " " + tumorType, treatmentLevel, tl);

    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return importer();
    }

    private static List<String[]> importer() throws IOException {
        if (TREATMENT_EXAMPLES_PATH == null) {
            System.out.println("Please specify the testing file path");
            return null;
        }

        File file = new File(TREATMENT_EXAMPLES_PATH);
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
                        throw new IllegalArgumentException("Test case should have at least gene variant and tumor type. Current case: " + line);
                    }
                    String gene = parts[0];
                    String variant = parts[1];
                    String tumorType = parts[2];
                    String treatmentLevel = parts.length > 3 ? parts[3] : "";
                    String[] query = {gene, variant, tumorType, treatmentLevel};
                    queries.add(query);
                    count++;
                } catch (Exception e) {
                    System.err.println("Could not add line '" + line + "'. " + e);
                }
            }
            line = buf.readLine();
        }
        System.err.println("Contains " + count + " queries.");
        System.err.println("Done.");

        return queries;
    }
}
