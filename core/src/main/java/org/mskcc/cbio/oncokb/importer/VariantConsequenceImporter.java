/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import java.io.IOException;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.VariantConsequenceBo;
import org.mskcc.cbio.oncokb.model.VariantConsequence;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author jgao
 */
public class VariantConsequenceImporter {
    private static final Logger LOGGER = LogManager.getLogger();

    private VariantConsequenceImporter() {
        throw new AssertionError();
    }

    private static final String VARIANT_CONSEQUENCES_FILE = "/data/variant-consequences.txt";

    public static void main(String[] args) throws IOException {
        List<String> lines = FileUtils.readTrimedLinesStream(
                VariantConsequenceImporter.class.getResourceAsStream(VARIANT_CONSEQUENCES_FILE));

    	VariantConsequenceBo variantConsequenceBo = ApplicationContextSingleton.getVariantConsequenceBo();

        int nLines = lines.size();
        LOGGER.info("importing...");
        for (int i=0; i<nLines; i++) {
            String line = lines.get(i);
            if (line.startsWith("#")) continue;

            String[] parts = line.split("\t");

            String term = parts[0];
            String isGenerallyTruncating = parts[1];
            String desc = parts[2];

            VariantConsequence variantConsequence = new VariantConsequence(term, desc, isGenerallyTruncating.equalsIgnoreCase("yes"));

            variantConsequenceBo.save(variantConsequence);
        }

        // Save NA special case
        VariantConsequence variantConsequence = new VariantConsequence("NA", "NA", false);
        variantConsequenceBo.save(variantConsequence);
    }
}
