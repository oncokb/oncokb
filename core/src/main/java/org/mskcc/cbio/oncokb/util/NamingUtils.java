package org.mskcc.cbio.oncokb.util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NamingUtils {
    private static Map<String, String> abbreviations = new HashMap<>();
    private static final String ABBREVIATION_ONTOLOGY_FILE = "/data/abbreviation-ontology.tsv";

    public static void cacheAllAbbreviations() throws IOException {
        List<String> lines = new ArrayList<>();
        lines = FileUtils.readTrimedLinesStream(
            NamingUtils.class.getResourceAsStream(ABBREVIATION_ONTOLOGY_FILE));
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

    public static Set<String> getAllAbbreviations() {
        return abbreviations.keySet();
    }

    public static Set<String> getAllAbbreviationFullNames() {
        return abbreviations.values().stream().collect(Collectors.toSet());
    }
}
