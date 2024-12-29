package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;
import java.util.stream.Collectors;

;

/**
 * Created by Hongxin on 6/2/17.
 */
public class TumorTypeUtilsTest extends TestCase {
    public void testFindTumorTypesWithSource() throws Exception {
        List<TumorType> tumorTypes = TumorTypeUtils.findRelevantTumorTypes("LIPO");
        String expectedResult = "Liposarcoma, M:Soft Tissue Sarcoma, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("DDLS");
        expectedResult = "Dedifferentiated Liposarcoma, M:Soft Tissue Sarcoma, Liposarcoma, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("NSCLC");
        expectedResult = "Non-Small Cell Lung Cancer, M:Non-Small Cell Lung Cancer, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("Chronic Myeloid Leukemia, BCR-ABL1+");
        expectedResult = "Chronic Myeloid Leukemia, BCR-ABL1+, M:Myeloproliferative Neoplasms, Chronic Myelogenous Leukemia, Myeloproliferative Neoplasms, M:All Liquid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("All Liquid Tumors");
        expectedResult = "M:All Liquid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("All Solid Tumors");
        expectedResult = "M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("All Tumors");
        expectedResult = "M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        // When parent node's main type does not match with child, it should not be listed as relevant tumor type
        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("Small Cell Lung Cancer");
        expectedResult = "Small Cell Lung Cancer, M:Small Cell Lung Cancer, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

    }

    public void testFindTumorTypesWithDirection() throws Exception {
        List<TumorType> tumorTypes = TumorTypeUtils.findRelevantTumorTypes("CML", false, RelevantTumorTypeDirection.DOWNWARD);
        String expectedResult = "Chronic Myelogenous Leukemia, M:Myeloproliferative Neoplasms, Chronic Myeloid Leukemia, BCR-ABL1+, M:All Liquid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("CML", false, RelevantTumorTypeDirection.UPWARD);
        expectedResult = "Chronic Myelogenous Leukemia, M:Myeloproliferative Neoplasms, Myeloproliferative Neoplasms, M:All Liquid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("MEL", false, RelevantTumorTypeDirection.UPWARD);
        expectedResult = "Melanoma, M:Melanoma, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("MEL", false, RelevantTumorTypeDirection.DOWNWARD);
        expectedResult = "Melanoma, M:Melanoma, Acral Melanoma, Congenital Nevus, Lentigo Maligna Melanoma, Cutaneous Melanoma, Melanoma of Unknown Primary, Desmoplastic Melanoma, Spitzoid Melanoma, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("Melanoma", true, RelevantTumorTypeDirection.UPWARD);
        expectedResult = "M:Melanoma, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("Melanoma", true, RelevantTumorTypeDirection.DOWNWARD);
        expectedResult = "M:Melanoma, M:All Solid Tumors, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        // Soft Tissue Sarcoma is a mixed main type
        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("Soft Tissue Sarcoma", true, RelevantTumorTypeDirection.UPWARD);
        expectedResult = "M:Soft Tissue Sarcoma, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));

        tumorTypes = TumorTypeUtils.findRelevantTumorTypes("Soft Tissue Sarcoma", true, RelevantTumorTypeDirection.DOWNWARD);
        expectedResult = "M:Soft Tissue Sarcoma, M:All Tumors";
        assertEquals(expectedResult, tumorTypesToString(tumorTypes));
        // 863 subtypes, 117 main types, 7 special tumor types
        assertEquals(987, ApplicationContextSingleton.getTumorTypeBo().getAllTumorTypes().size());
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

        tumorType = ApplicationContextSingleton.getTumorTypeBo().getByCode("CMLBCRABL1");
        tumorType.setTumorForm(TumorTypeUtils.getTumorForm(tumorType.getTissue()));
        assertTrue("Blood tissue is not liquid tumor, but it should be.", TumorTypeUtils.isLiquidTumor(tumorType));
    }

    public void testHasSolidTumor() throws Exception {
        Set<TumorType> tumorTypeSet = new HashSet<>();
        TumorType tt1 = new TumorType();
        TumorType tt2 = new TumorType();
        tt1.setId(0);
        tt1.setId(0);
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
        tt1.setId(0);
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

    public void testFindEvidenceRelevantCancerTypes() {
        Evidence testEvi = new Evidence();
        Set<TumorType> cancerTypes = new HashSet<>();
        String expectedResult = "";

        // Check subtype
        testEvi.setCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getByCode("MEL")));
        testEvi.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        cancerTypes = TumorTypeUtils.findEvidenceRelevantCancerTypes(testEvi);
        expectedResult = "Acral Melanoma, Congenital Nevus, Cutaneous Melanoma, Desmoplastic Melanoma, Lentigo Maligna Melanoma, Melanoma, Melanoma of Unknown Primary, Spitzoid Melanoma";
        assertEquals(expectedResult, tumorTypesToString(new ArrayList<>(cancerTypes), true));

        // Check subtype with exclusion
        testEvi.setExcludedCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getByName("Desmoplastic Melanoma")));
        cancerTypes = TumorTypeUtils.findEvidenceRelevantCancerTypes(testEvi);
        expectedResult = "Acral Melanoma, Congenital Nevus, Cutaneous Melanoma, Lentigo Maligna Melanoma, Melanoma, Melanoma of Unknown Primary, Spitzoid Melanoma";
        assertEquals(expectedResult, tumorTypesToString(new ArrayList<>(cancerTypes), true));

        // Check main type
        testEvi = new Evidence();
        testEvi.setCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getByMainType("Melanoma")));
        testEvi.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        cancerTypes = TumorTypeUtils.findEvidenceRelevantCancerTypes(testEvi);
        expectedResult = "Acral Melanoma, Anorectal Mucosal Melanoma, Congenital Nevus, Conjunctival Melanoma, Cutaneous Melanoma, Desmoplastic Melanoma, Head and Neck Mucosal Melanoma, Lentigo Maligna Melanoma, M:Melanoma, Melanoma, Melanoma of Unknown Primary, Mucosal Melanoma of the Esophagus, Mucosal Melanoma of the Urethra, Mucosal Melanoma of the Vulva/Vagina, Ocular Melanoma, Primary CNS Melanoma, Spitzoid Melanoma, Uveal Melanoma";
        assertEquals(expectedResult, tumorTypesToString(new ArrayList<>(cancerTypes), true));

        // Check main type with exclusion
        testEvi.setExcludedCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getByName("Desmoplastic Melanoma")));
        cancerTypes = TumorTypeUtils.findEvidenceRelevantCancerTypes(testEvi);
        expectedResult = "Acral Melanoma, Anorectal Mucosal Melanoma, Congenital Nevus, Conjunctival Melanoma, Cutaneous Melanoma, Head and Neck Mucosal Melanoma, Lentigo Maligna Melanoma, M:Melanoma, Melanoma, Melanoma of Unknown Primary, Mucosal Melanoma of the Esophagus, Mucosal Melanoma of the Urethra, Mucosal Melanoma of the Vulva/Vagina, Ocular Melanoma, Primary CNS Melanoma, Spitzoid Melanoma, Uveal Melanoma";
        assertEquals(expectedResult, tumorTypesToString(new ArrayList<>(cancerTypes), true));

        // Check main type with main type exclusion
        testEvi.setCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getBySpecialTumor(SpecialTumorType.ALL_SOLID_TUMORS)));
        testEvi.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        testEvi.setExcludedCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getByName("Bladder Cancer")));
        cancerTypes = TumorTypeUtils.findEvidenceRelevantCancerTypes(testEvi);
        assertFalse(cancerTypes.stream().filter(cancerType -> cancerType.getMainType() == "Bladder Cancer").findAny().isPresent());

        // Check for special cancer types
        testEvi.setCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getBySpecialTumor(SpecialTumorType.ALL_TUMORS)));
        testEvi.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        testEvi.setExcludedCancerTypes(new HashSet<>());
        cancerTypes = TumorTypeUtils.findEvidenceRelevantCancerTypes(testEvi);
        assertEquals(983, cancerTypes.size());

        testEvi.setCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getBySpecialTumor(SpecialTumorType.ALL_LIQUID_TUMORS)));
        testEvi.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        cancerTypes = TumorTypeUtils.findEvidenceRelevantCancerTypes(testEvi);
        assertEquals(242, cancerTypes.size());

        testEvi.setCancerTypes(Collections.singleton(ApplicationContextSingleton.getTumorTypeBo().getBySpecialTumor(SpecialTumorType.ALL_SOLID_TUMORS)));
        testEvi.setLevelOfEvidence(LevelOfEvidence.LEVEL_1);
        cancerTypes = TumorTypeUtils.findEvidenceRelevantCancerTypes(testEvi);
        assertEquals(742, cancerTypes.size());
    }

    private String tumorTypesToString(List<TumorType> tumorTypes) {
        return tumorTypesToString(tumorTypes, false);
    }

    private String tumorTypesToString(List<TumorType> tumorTypes, Boolean sortAsc) {
        List<String> name = new ArrayList<>();
        for (TumorType tumorType : tumorTypes) {
            name.add(StringUtils.isEmpty(tumorType.getSubtype()) ?
                ("M:" + tumorType.getMainType()) : tumorType.getSubtype());
        }
        if (sortAsc) {
            Collections.sort(name);
        }
        return org.apache.commons.lang3.StringUtils.join(name, ", ");
    }
}
