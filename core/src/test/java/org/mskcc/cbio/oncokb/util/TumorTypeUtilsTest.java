package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.oncotree.model.MainType;
import org.mskcc.oncotree.model.TumorType;

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
        assertFalse("Empty tumor type is not solid tumor", TumorTypeUtils.isSolidTumor(tumorType));

        mainType.setName("Melanoma");
        assertTrue("Melanoma is solid tumor", TumorTypeUtils.isSolidTumor(tumorType));

        mainType.setName("Leukemia");
        assertFalse("Leukemia is not solid tumor", TumorTypeUtils.isSolidTumor(tumorType));

        mainType.setName("Leukemia");
        tumorType.setTissue("EYE");
        assertTrue("Tissue has higher priority than main type", TumorTypeUtils.isSolidTumor(tumorType));
    }

    public void testIsLiquidTumor() throws Exception {
        TumorType tumorType = new TumorType();
        MainType mainType = new MainType();

        tumorType.setMainType(mainType);
        assertFalse("Empty tumor type is not liquid tumor", TumorTypeUtils.isLiquidTumor(tumorType));

        mainType.setName("Leukemia");
        assertTrue("Leukemia is liquid tumor", TumorTypeUtils.isLiquidTumor(tumorType));

        mainType.setName("Melanoma");
        assertFalse("Melanoma is not liquid tumor", TumorTypeUtils.isLiquidTumor(tumorType));

        mainType.setName("Melanoma");
        tumorType.setTissue("BLOOD");
        assertTrue("Tissue has higher priority than main type", TumorTypeUtils.isLiquidTumor(tumorType));
    }

    public void testHasSolidTumor() throws Exception {
        Set<TumorType> tumorTypeSet = new HashSet<>();
        TumorType tt1 = new TumorType();
        TumorType tt2 = new TumorType();
        tt1.setId(0);
        tt1.setId(1);
        tumorTypeSet.add(tt1);
        tumorTypeSet.add(tt2);

        assertFalse("No tumor type should be solid tumor", TumorTypeUtils.hasSolidTumor(tumorTypeSet));

        tt1.setTissue("EYE");
        assertTrue("One tumor type is solid tumor, should be true", TumorTypeUtils.hasSolidTumor(tumorTypeSet));

        tt2.setTissue("BONE");
        assertTrue("Both tumor types are solid tumor, should be true", TumorTypeUtils.hasSolidTumor(tumorTypeSet));

        tt1.setTissue("BLOOD");
        tt2.setTissue("BONE");
        assertTrue("One tumor type is solid tumor, should be true", TumorTypeUtils.hasSolidTumor(tumorTypeSet));
    }

    public void testHasLiquidTumor() throws Exception {
        Set<TumorType> tumorTypeSet = new HashSet<>();
        TumorType tt1 = new TumorType();
        TumorType tt2 = new TumorType();
        tt1.setId(0);
        tt1.setId(1);
        tumorTypeSet.add(tt1);
        tumorTypeSet.add(tt2);

        assertFalse("No tumor type should be liquid tumor", TumorTypeUtils.hasLiquidTumor(tumorTypeSet));

        tt1.setTissue("BLOOD");
        assertTrue("One tumor type is liquid tumor, should be true", TumorTypeUtils.hasLiquidTumor(tumorTypeSet));

        tt1.setTissue("BLOOD");
        tt2.setTissue("BONE");
        assertTrue("One tumor type is liquid tumor, should be true", TumorTypeUtils.hasLiquidTumor(tumorTypeSet));
    }

}
