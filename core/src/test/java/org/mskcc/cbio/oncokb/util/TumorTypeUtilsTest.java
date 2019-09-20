package org.mskcc.cbio.oncokb.util;

import com.google.common.collect.ImmutableList;
import com.mysql.jdbc.StringUtils;
import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.RelevantTumorTypeDirection;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

;

/**
 * Created by Hongxin on 6/2/17.
 */
public class TumorTypeUtilsTest extends TestCase {
    public void testFindTumorTypesWithSource() throws Exception {
        List<TumorType> tumorTypes = TumorTypeUtils.findTumorTypes("LIPO", "oncotree");
        String expectedResult = "Liposarcoma, M:Soft Tissue Sarcoma, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findTumorTypes("DDLS", "oncotree");
        expectedResult = "Dedifferentiated Liposarcoma, M:Soft Tissue Sarcoma, Liposarcoma, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findTumorTypes("NSCLC", "oncotree");
        expectedResult = "Non-Small Cell Lung Cancer, M:Non-Small Cell Lung Cancer, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findTumorTypes("Chronic Myeloid Leukemia, BCR-ABL1+", "oncotree");
        expectedResult = "Chronic Myeloid Leukemia, BCR-ABL1+, M:Myeloproliferative Neoplasms, Chronic Myelogenous Leukemia, Myeloproliferative Neoplasms, M:All Liquid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findTumorTypes("All Liquid Tumors", "oncotree");
        expectedResult = "M:All Liquid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findTumorTypes("All Solid Tumors", "oncotree");
        expectedResult = "M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findTumorTypes("All Tumors", "oncotree");
        expectedResult = "M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        // When parent node's main type does not match with child, it should not be listed as relevant tumor type
        tumorTypes = TumorTypeUtils.findTumorTypes("Small Cell Lung Cancer", "oncotree");
        expectedResult = "Small Cell Lung Cancer, M:Small Cell Lung Cancer, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

    }

    public void testFindTumorTypesWithDirection() throws Exception {
        List<TumorType> tumorTypes = TumorTypeUtils.findTumorTypes("CML", RelevantTumorTypeDirection.DOWNWARD);
        String expectedResult = "Chronic Myelogenous Leukemia, M:Myeloproliferative Neoplasms, Chronic Myeloid Leukemia, BCR-ABL1+, M:All Liquid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findTumorTypes("", RelevantTumorTypeDirection.DOWNWARD);
        assertEquals(976, tumorTypes.size());
    }

    public void testGetAllOncoTreeCancerTypes() throws Exception {
        List<TumorType> cancerTypes = TumorTypeUtils.getAllOncoTreeCancerTypes();
        System.out.println(cancerTypes.size() + " cancer types in total");
        //All cancer types should have name
        for (TumorType cancerType : cancerTypes) {
            assertTrue("Tumor type " + cancerType.getMainType() + " does not have code, but it should have.", !StringUtils.isNullOrEmpty(cancerType.getMainType().getName()));
        }

        // Test few liquid main types whether they are all defined as liquid tumor.
        List<String> mainTypes = ImmutableList.of(
            "Blastic Plasmacytoid Dendritic Cell Neoplasm", "Histiocytosis", "Leukemia", "Multiple Myeloma",
            "Myelodysplasia", "Myeloproliferative Neoplasm", "Mastocytosis", "Hodgkin Lymphoma", "Non-Hodgkin Lymphoma",
            "Blood Cancer, NOS", "Myelodysplastic Syndromes", "Lymphatic Cancer, NOS", " B-Lymphoblastic Leukemia/Lymphoma",
            "Mature B-Cell Neoplasms", "Mature T and NK Neoplasms", "Posttransplant Lymphoproliferative Disorders",
            "T-Lymphoblastic Leukemia/Lymphoma", "Histiocytic Disorder"
        );
        for (TumorType cancerType : cancerTypes) {
            if (cancerType.getMainType() != null && cancerType.getMainType().getName() != null && mainTypes.contains(cancerType.getMainType().getName())) {
                assertTrue(cancerType.getMainType().getName() + " is not liquid tumor, but it should be.", TumorTypeUtils.isLiquidTumor(cancerType));
            }
        }
    }

    public void testGetAllOncoTreeSubtypes() throws Exception {
        List<TumorType> subtypes = TumorTypeUtils.getAllOncoTreeSubtypes();
        System.out.println(subtypes.size() + " subtypes in total");

        //All subtypes should have tissue and code
        for (TumorType subtype : subtypes) {
            // Do not test the root node
            if (subtype.getCode().equals("TISSUE")) {
                continue;
            }
            assertTrue("Tumor type " + subtype.getMainType().getName() + " " + subtype.getCode() + " does not have code, but it should have.", !StringUtils.isNullOrEmpty(subtype.getCode()));
            assertTrue("Tumor type " + subtype.getMainType().getName() + " " + subtype.getCode() + " does not have tissue, but it should have.", !StringUtils.isNullOrEmpty(subtype.getTissue()));
        }

        // Test few liquid subtype codes whether they are all defined as liquid tumor.
        List<String> codes = ImmutableList.of(
            "LEUK", "MDS", "LCH", "NHL", "HL", "NLPHL"
        );
        for (TumorType subtype : subtypes) {
            if (subtype.getCode() != null && codes.contains(subtype.getCode())) {
                assertTrue(subtype.getMainType().getName() + " " + subtype.getCode() + " is not liquid tumor, but it should be.", TumorTypeUtils.isLiquidTumor(subtype));
            }
        }
    }

    // Is solid tumor is decided on tissue level
    public void testIsSolidTumor() throws Exception {
        TumorType tumorType = new TumorType();

        // null
        assertFalse("Null tissue is solid tumor, but it should not be.", TumorTypeUtils.isSolidTumor(tumorType));

        // Empty
        tumorType.setTissue("");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertFalse("Empty tissue is solid tumor, but it should not be.", TumorTypeUtils.isSolidTumor(tumorType));

        tumorType.setTissue("Blood");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertFalse("Blood tissue is solid tumor, but it should not be.", TumorTypeUtils.isSolidTumor(tumorType));

        tumorType.setTissue("Lymph");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertFalse("Lymph tissue is solid tumor, but it should not be.", TumorTypeUtils.isSolidTumor(tumorType));

        tumorType.setTissue("Eye");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertTrue("Eye tissue is not solid tumor, but it should be.", TumorTypeUtils.isSolidTumor(tumorType));
    }

    // Is liquid tumor is decided on tissue level
    public void testIsLiquidTumor() throws Exception {
        TumorType tumorType = new TumorType();

        // null
        assertFalse("Null is liquid tumor, but it should not be.", TumorTypeUtils.isLiquidTumor(tumorType));

        // empty
        tumorType.setTissue("");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertFalse("Empty is liquid tumor, but it should not be.", TumorTypeUtils.isLiquidTumor(tumorType));

        tumorType.setTissue("Skin");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertFalse("Skin tissue is liquid tumor, but it should not be.", TumorTypeUtils.isLiquidTumor(tumorType));

        tumorType.setTissue("Blood");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertTrue("Blood tissue is not liquid tumor, but it should be.", TumorTypeUtils.isLiquidTumor(tumorType));

        tumorType = TumorTypeUtils.getOncoTreeSubtypeByCode("CMLBCRABL1");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertTrue("Blood tissue is not liquid tumor, but it should be.", TumorTypeUtils.isLiquidTumor(tumorType));
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

        tt1.setTissue("Eye");
        tt1.setTumorForm(TumorTypeUtils.getTumorForm(tt1.getTissue()));
        assertTrue("Tumor types set does not have solid tumor, but it should because of EYE is solid tumor.", TumorTypeUtils.hasSolidTumor(tumorTypeSet));

        tt2.setTissue("Bone");
        tt2.setTumorForm(TumorTypeUtils.getTumorForm(tt2.getTissue()));
        assertTrue("Tumor types set does not have solid tumor, but both tumor types in the set are solid tumor.", TumorTypeUtils.hasSolidTumor(tumorTypeSet));

        tt1.setTissue("Blood");
        tt1.setTumorForm(TumorTypeUtils.getTumorForm(tt1.getTissue()));
        tt2.setTissue("Bone");
        tt2.setTumorForm(TumorTypeUtils.getTumorForm(tt2.getTissue()));
        assertTrue("Tumor types set does not have solid tumor, but one of tumor types Bone is solid tumor.", TumorTypeUtils.hasSolidTumor(tumorTypeSet));
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

        tt1.setTissue("Blood");
        tt1.setTumorForm(TumorTypeUtils.getTumorForm(tt1.getTissue()));
        assertTrue("Tumor types set does not have liquid tumor, but one tumor type in the set is liquid tumor.", TumorTypeUtils.hasLiquidTumor(tumorTypeSet));

        tt1.setTissue("Blood");
        tt1.setTumorForm(TumorTypeUtils.getTumorForm(tt1.getTissue()));
        tt2.setTissue("Bone");
        tt2.setTumorForm(TumorTypeUtils.getTumorForm(tt2.getTissue()));
        assertTrue("Tumor types set does not have liquid tumor, but one of tumor types Blood is liquid tumor.", TumorTypeUtils.hasLiquidTumor(tumorTypeSet));
    }

    private String tumorTypesToString(List<TumorType> tumorTypes) {
        List<String> name = new ArrayList<>();
        for (TumorType tumorType : tumorTypes) {
            name.add(tumorType.getCode() == null ?
                ("M:" + tumorType.getMainType().getName()) : tumorType.getName());
        }
        return org.apache.commons.lang3.StringUtils.join(name, ", ");
    }
}
