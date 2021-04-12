package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.Gene;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Hongxin Zhang on 4/12/21.
 */
public class FusionUtilsTest extends TestCase {

    public void testGetFusionName() {

        Gene geneA = new Gene();
        geneA.setEntrezGeneId(346288);
        geneA.setHugoSymbol("SEPTIN14");
        geneA.setGeneAliases(new HashSet<>(Arrays.asList("SEPT14")));

        Gene geneB = new Gene();
        geneB.setEntrezGeneId(1956);
        geneB.setHugoSymbol("EGFR");
        geneB.setGeneAliases(new HashSet<>(Arrays.asList("ERBB1")));


        String fusionName = FusionUtils.getFusionName(geneA, geneB);
        assertEquals("EGFR-SEPT14 Fusion", fusionName);

        fusionName = FusionUtils.getFusionName(geneA, null);
        assertEquals("", fusionName);

        fusionName = FusionUtils.getFusionName(null, geneB);
        assertEquals("", fusionName);
    }
}
