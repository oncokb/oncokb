package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.oncotree.MainType;
import org.mskcc.cbio.oncokb.model.oncotree.TumorType;;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Hongxin on 6/2/17.
 */
public class TumorTypeUtilsTest extends TestCase {
    public void testIsSolidTumor() throws Exception {
        TumorType tumorType = new TumorType();
        MainType mainType = new MainType();

        tumorType.setMainType(mainType);
        assertFalse("Empty tumor type is solid tumor, but it should not be.", TumorTypeUtils.isSolidTumor(tumorType));

        mainType.setName("Melanoma");
        assertTrue("Melanoma is not solid tumor, but it should be.", TumorTypeUtils.isSolidTumor(tumorType));

        mainType.setName("Leukemia");
        assertFalse("Leukemia is solid tumor, but it should not be.", TumorTypeUtils.isSolidTumor(tumorType));

        mainType.setName("Leukemia");
        tumorType.setTissue("EYE");
        assertTrue("Tissue does not have higher priority than main type, but it should have.", TumorTypeUtils.isSolidTumor(tumorType));
    }

    public void testIsLiquidTumor() throws Exception {
        TumorType tumorType = new TumorType();
        MainType mainType = new MainType();

        tumorType.setMainType(mainType);
        assertFalse("Empty tumor type is liquid tumor, but it should not be.", TumorTypeUtils.isLiquidTumor(tumorType));

        mainType.setName("Leukemia");
        assertTrue("Leukemia is not liquid tumor, but it should be.", TumorTypeUtils.isLiquidTumor(tumorType));

        mainType.setName("Melanoma");
        assertFalse("Melanoma is liquid tumor, but it should not be.", TumorTypeUtils.isLiquidTumor(tumorType));

        mainType.setName("Melanoma");
        tumorType.setTissue("BLOOD");
        assertTrue("Tissue does not have higher priority than main type, but it should have.", TumorTypeUtils.isLiquidTumor(tumorType));
    }

    public void testHasSolidTumor() throws Exception {
        Set<TumorType> tumorTypeSet = new HashSet<>();
        TumorType tt1 = new TumorType();
        TumorType tt2 = new TumorType();
        tt1.setId(0);
        tt1.setId(1);
        tumorTypeSet.add(tt1);
        tumorTypeSet.add(tt2);

        assertFalse("Empty tumor type set has solid tumor, but it should not.", TumorTypeUtils.hasSolidTumor(tumorTypeSet));

        tt1.setTissue("EYE");
        assertTrue("Tumor types set does not have solid tumor, but it should because of EYE is solid tumor.", TumorTypeUtils.hasSolidTumor(tumorTypeSet));

        tt2.setTissue("BONE");
        assertTrue("Tumor types set does not have solid tumor, but both tumor types in the set are solid tumor.", TumorTypeUtils.hasSolidTumor(tumorTypeSet));

        tt1.setTissue("BLOOD");
        tt2.setTissue("BONE");
        assertTrue("Tumor types set does not have solid tumor, but one of tumor types BONE is solid tumor.", TumorTypeUtils.hasSolidTumor(tumorTypeSet));
    }

    public void testHasLiquidTumor() throws Exception {
        Set<TumorType> tumorTypeSet = new HashSet<>();
        TumorType tt1 = new TumorType();
        TumorType tt2 = new TumorType();
        tt1.setId(0);
        tt1.setId(1);
        tumorTypeSet.add(tt1);
        tumorTypeSet.add(tt2);

        assertFalse("Empty tumor type set has liquid tumor, but it should not have", TumorTypeUtils.hasLiquidTumor(tumorTypeSet));

        tt1.setTissue("BLOOD");
        assertTrue("Tumor types set does not have liquid tumor, but one tumor type in the set is liquid tumor.", TumorTypeUtils.hasLiquidTumor(tumorTypeSet));

        tt1.setTissue("BLOOD");
        tt2.setTissue("BONE");
        assertTrue("Tumor types set does not have liquid tumor, but one of tumor types BLOOD is liquid tumor.", TumorTypeUtils.hasLiquidTumor(tumorTypeSet));
    }

}
