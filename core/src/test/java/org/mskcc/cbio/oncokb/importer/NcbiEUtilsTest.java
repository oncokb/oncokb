/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mskcc.cbio.oncokb.util.NcbiEUtils;

/**
 * @author jgao
 */
public class NcbiEUtilsTest {

    public NcbiEUtilsTest() {
    }

    @Test()
    public void testReadPmid() {
        String pmid = "2549426";
        NcbiEUtils.readPubmedArticle(pmid);
    }
}
