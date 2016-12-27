/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncgkb.dao.util;

import org.junit.Assert;
import org.junit.Test;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.GeneAnnotatorMyGeneInfo2;

import java.io.IOException;
import java.util.List;

/**
 * @author jgao
 */
public class GeneAnnotatorMyGeneInfo2Test {

    public GeneAnnotatorMyGeneInfo2Test() {
    }

    @Test()
    public void testReadByEntrez() throws IOException {
        Gene gene = GeneAnnotatorMyGeneInfo2.readByEntrezId(1017);
        Assert.assertEquals(gene.getHugoSymbol(), "CDK2");
    }

    @Test()
    public void testReadByAlias() throws IOException {
        List<Gene> genes = GeneAnnotatorMyGeneInfo2.readByAlias("MLL2");
        boolean found = false;
        for (Gene gene : genes) {
            if (gene.getEntrezGeneId() == 8085) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }
}
