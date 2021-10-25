package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.apiModels.FdaAlteration;
import org.mskcc.cbio.oncokb.apiModels.Implication;
import org.mskcc.cbio.oncokb.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Hongxin
 */
@RunWith(Parameterized.class)
public class FdaParameterizedTest {
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
        Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        Set<FdaAlteration> fdaAlterations = FdaAlterationUtils.getGeneFdaAlterations(gene).stream().filter(fdaAlteration -> fdaAlteration.getAlteration().getAlteration().equals(variant) && fdaAlteration.getCancerType().equals(cancerType)).collect(Collectors.toSet());
        String _query = hugoSymbol + " " + variant + " " + cancerType;
        assertEquals("FDA Level, Query: " + _query, fdaLevel, fdaAlterations.stream().map(FdaAlteration::getLevel).collect(Collectors.joining(", ")));
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() throws IOException {
        return importer();
    }

    private static List<String[]> importer() throws IOException {
        if (FDA_EXAMPLES_PATH == null) {
            System.out.println("Please specify the testing file path");
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
