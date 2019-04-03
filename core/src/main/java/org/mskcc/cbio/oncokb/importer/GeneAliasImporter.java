package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.GeneAnnotatorMyGeneInfo2;

import java.io.IOException;
import java.util.List;

/**
 * @author Hongxin Zhang
 */
public final class GeneAliasImporter {
    private GeneAliasImporter() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws IOException {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        List<Gene> genes = geneBo.findAll();

        for (Gene gene : genes) {
            if (gene.getGeneAliases() == null || gene.getGeneAliases().isEmpty()) {
                try {
                    GeneAnnotatorMyGeneInfo2.includeGeneAlias(gene);
                    geneBo.update(gene);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
}
