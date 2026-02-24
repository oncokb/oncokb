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
public class MutationEffectParameterizedTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MutationEffectParameterizedTest.class);
    private static String VARIANT_SUMMARY_EXAMPLES_PATH = "src/test/resources/test_mutation_effect_summaries.tsv";

    private String gene;
    private String variant;
    private String mutationEffect;
    private String description;

    public MutationEffectParameterizedTest(String gene, String variant, String mutationEffect, String description) {
        this.gene = gene;
        this.variant = variant;
        this.mutationEffect = mutationEffect;
        this.description = description;
    }

    @Test
    public void testSummary() throws Exception {
        Query query = new Query();
        query.setHugoSymbol(gene);
        query.setAlteration(variant);

        SomaticIndicatorQueryResp resp = IndicatorUtils.processQuerySomatic(query, null, false, null, false);
        String _query = gene + " " + variant;
        String _mutationEffect = resp.getMutationEffect().getKnownEffect();
        String _description = resp.getMutationEffect().getDescription();
        assertEquals("Mutation effect, Query: " + _query, mutationEffect, _mutationEffect);
        assertEquals("Description, Query: " + _query, description, _description);
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
                    String mutationEffect = parts.length > 2 ? parts[2] : "";
                    String description = parts.length > 3 ? parts[3] : "";
                    String[] query = {gene, variant, mutationEffect, description};
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
