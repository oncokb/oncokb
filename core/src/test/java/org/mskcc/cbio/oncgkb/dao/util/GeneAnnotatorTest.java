/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncgkb.dao.util;

import org.junit.Assert;
import org.junit.Test;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.GeneAnnotator;

import java.io.IOException;
import java.util.List;

/**
 * @author jgao
 */
public class GeneAnnotatorTest {

    public GeneAnnotatorTest() {
    }

    @Test()
    public void testReadByEntrez() throws IOException {
        Gene gene = GeneAnnotator.readByEntrezId(1017);
        Assert.assertEquals(gene.getHugoSymbol(), "CDK2");
    }

    @Test()
    public void testReadByAlias() throws IOException {
        List<Gene> genes = GeneAnnotator.readByAlias("MLL2");
        boolean found = false;
        for (Gene gene : genes) {
            if (gene.getEntrezGeneId() == 8085) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }
}
