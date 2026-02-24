package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.SomaticIndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.Query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * Created by Hongxin
 */
@RunWith(Parameterized.class)
public class FdaParameterizedTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FdaParameterizedTest.class);

    private static String FDA_EXAMPLES_PATH = "src/test/resources/test_fda.tsv";

    private String hugoSymbol;
    private String variant;
    private String cancerType;
    private String fdaLevel;

    public FdaParameterizedTest(String hugoSymbol, String variant, String cancerType, String fdaLevel) {
        this.hugoSymbol = hugoSymbol;
        this.variant = variant;
        this.cancerType = cancerType;
        this.fdaLevel = fdaLevel;
    }

    @Test
    public void testSummary() throws Exception {
        Query query = new Query();
        query.setHugoSymbol(hugoSymbol);
        query.setAlteration(variant);
        query.setTumorType(cancerType);
        SomaticIndicatorQueryResp resp = IndicatorUtils.processQuerySomatic(query, null, true, null, false);
        assertEquals(fdaLevel, resp.getHighestFdaLevel().getLevel());
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return importer();
    }

    private static List<String[]> importer() throws IOException {
        if (FDA_EXAMPLES_PATH == null) {
            LOGGER.error("Please specify the testing file path");
            return null;
        }

        File file = new File(FDA_EXAMPLES_PATH);
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
                    String cancerType = parts[2];
                    String fdaLevel = parts.length > 3 ? parts[3] : "";
                    String[] query = {gene, variant, cancerType, fdaLevel};
                    queries.add(query);
                    count++;
                } catch (Exception e) {
                    LOGGER.error("Could not add line '{}'.", line, e);
                }
            }
            line = buf.readLine();
        }
        LOGGER.info("Contains {} tumor type summary queries.", count);
        LOGGER.info("Done.");

        return queries;
    }
}
