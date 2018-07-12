package org.mskcc.cbio.oncokb.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamingUtils {
    private static Map<String, String> abbreviations = new HashMap<>();
    private static final String ABBREVIATION_ONTOLOGY_FILE = "/data/abbreviation-ontology.tsv";

    public static void cacheAllAbbreviations() {
        List<String> lines = new ArrayList<>();
        try {
            lines = FileUtils.readTrimedLinesStream(
                NamingUtils.class.getResourceAsStream(ABBREVIATION_ONTOLOGY_FILE));
        } catch (Exception e) {
            System.out.println("Failed to read abbreviation ontology file.");
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("#")) continue;

            String[] parts = line.split("\t");
            if (parts.length >= 2)
                abbreviations.put(parts[0], parts[1]);
        }
    }

    public static String getFullName(String abbreviation) {
        return abbreviation == null ? null : abbreviations.get(abbreviation);
    }

    public static boolean hasAbbreviation(String abbreviation) {
        return abbreviation == null ? false : abbreviations.containsKey(abbreviation);
    }
}
