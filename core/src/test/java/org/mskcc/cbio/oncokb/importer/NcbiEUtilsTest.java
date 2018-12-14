/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.junit.Test;
import org.mskcc.cbio.oncokb.util.NcbiEUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jgao
 */
public class NcbiEUtilsTest {

    public NcbiEUtilsTest() {
    }

    @Test()
    public void testReadPmid() {
        Set<String> pmids = new HashSet<>();
        pmids.add("2549426");
        NcbiEUtils.readPubmedArticles(pmids);
    }
}
