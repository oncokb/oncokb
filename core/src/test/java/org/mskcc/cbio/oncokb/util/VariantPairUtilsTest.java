package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.VariantQuery;

import java.util.List;

/**
 * Created by Hongxin Zhang on 6/21/18.
 */
public class VariantPairUtilsTest extends TestCase {
    // The goal of having tests for this function is to prevent the null pointer exception.
    public void testGetGeneAlterationTumorTypeConsequence() throws Exception {
        List<VariantQuery> results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null, null, null, null, null, null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(673, null, null, null, null, null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null, "BRAF", null, null, null, null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null, null, "V600E", null, null, null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null, null, null, "MELANOMA", null, null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null, null, null, null, "MISSENSE", null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null, null, null, null, null, 600, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null, null, null, null, null, null, 600);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(null, null, null, null, null, null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(673, null, "V600E", null, null, null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(673, null, "V600E", "MELANOMA", null, null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(673, null, "V600E", "MELANOMA", "missense", null, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(673, null, "V600E", "MELANOMA", "missense", 600, null);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(673, null, "V600E", "MELANOMA", "missense", 600, 600);
        results = VariantPairUtils.getGeneAlterationTumorTypeConsequence(673, "BRAF", "V600E", "Melanoma", "missense", 600, 600);
    }

}
