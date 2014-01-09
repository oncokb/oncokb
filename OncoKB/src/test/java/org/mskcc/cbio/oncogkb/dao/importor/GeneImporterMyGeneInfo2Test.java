/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.importor;

import org.mskcc.cbio.oncokb.dao.importor.GeneImporterMyGeneInfo2;
import java.io.IOException;
import java.util.List;
import org.mskcc.cbio.oncokb.model.Gene;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jgao
 */
public class GeneImporterMyGeneInfo2Test {
    
    public GeneImporterMyGeneInfo2Test() {
    }
    
    @Test(groups = {"remote"})
    public void testReadByEntrez() throws IOException {
        Gene gene = GeneImporterMyGeneInfo2.readByEntrezId(1017);
        Assert.assertEquals(gene.getHugoSymbol(),"CDK2");
    }
    
    @Test(groups = {"remote"})
    public void testReadBySymbol() throws IOException {
        List<Gene> genes = GeneImporterMyGeneInfo2.readBySymbol("MLL2");
        boolean found = false;
        for (Gene gene : genes) {
            if (gene.getEntrezGeneId()==8085) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    @org.testng.annotations.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.testng.annotations.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.testng.annotations.BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @org.testng.annotations.AfterMethod
    public void tearDownMethod() throws Exception {
    }
}