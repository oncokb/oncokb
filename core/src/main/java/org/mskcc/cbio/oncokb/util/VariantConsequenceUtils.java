package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.VariantConsequence;

import java.io.IOException;
import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 */
public class VariantConsequenceUtils {

    private static final String VARIANT_CONSEQUENCE_FILE_PATH = "/data/variant-consequences.txt";
    private static Map<String, VariantConsequence> VariantConsequencesMap = null;
    public static VariantConsequence findVariantConsequenceByTerm(String searchTerm){
        if (VariantConsequencesMap==null) {
            try {
                List<String> lines = null;
                VariantConsequencesMap = new HashMap<String, VariantConsequence>();
                lines = FileUtils.readTrimedLinesStream(
                        VariantConsequenceUtils.class.getResourceAsStream(VARIANT_CONSEQUENCE_FILE_PATH));

                int nLines = lines.size();
                System.out.println("importing...");
                for (int i=0; i<nLines; i++) {
                    String line = lines.get(i);
                    if (line.startsWith("#")) continue;

                    String[] parts = line.split("\t");

                    String term = parts[0];
                    String isGenerallyTruncating = parts[1];
                    String desc = parts[2];

                    VariantConsequence variantConsequence = new VariantConsequence(term, desc, isGenerallyTruncating.equalsIgnoreCase("yes"));
                    VariantConsequencesMap.put(term, variantConsequence);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        VariantConsequence variantConsequence = VariantConsequencesMap.get(searchTerm);
        return variantConsequence;
    }
}
