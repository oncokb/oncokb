package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.TestCase.assertEquals;
import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Created by Hongxin on 12/14/16.
 */
@RunWith(value = Parameterized.class)
public class IsOncogenicAlterationTest {

    private String hugoSymbol;
    private String alterationName;
    private Boolean isOncogenic;

    public IsOncogenicAlterationTest(String hugoSymbol, String alterationName, Boolean isOncogenic) {
        this.hugoSymbol = hugoSymbol;
        this.alterationName = alterationName;
        this.isOncogenic = isOncogenic;
    }

    @Parameters(name = "{index}: testAdd({0} , {1}) is oncogenic : {2}")
    public static Collection<Object[]> getTestData() {
        return Arrays.asList(new Object[][]{
            {"BRAF", "V600E", true}
        });
    }

    @Test
    public void test_is_oncogenic() {
        Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        Alteration alteration = AlterationUtils.findAlteration(gene, DEFAULT_REFERENCE_GENOME, alterationName);
        assertEquals(isOncogenic, AlterationUtils.isOncogenicAlteration(alteration));
    }
}
