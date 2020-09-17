package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

/**
 * Created by Hongxin on 5/26/16.
 */
public final class GeneralImporter {

    public static void main(String[] args) throws Exception {
        VariantConsequenceImporter.main(args);

        // Save special genes
        Gene gene = new Gene();
        gene.setHugoSymbol("Other Biomarkers");
        gene.setEntrezGeneId(-2);
        ApplicationContextSingleton.getGeneBo().save(gene);
    }
}
