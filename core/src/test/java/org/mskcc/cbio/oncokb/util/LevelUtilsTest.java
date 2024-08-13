package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;

/**
 * Created by Hongxin on 12/29/16.
 */
public class LevelUtilsTest extends TestCase {
    public void testCompareLevel() throws Exception {
        assertTrue(LevelUtils.compareLevel(null, null) == 0);
        assertTrue(LevelUtils.compareLevel(LevelOfEvidence.LEVEL_1, null) < 0);
        assertTrue(LevelUtils.compareLevel(null, LevelOfEvidence.LEVEL_1) > 0);
        assertTrue(LevelUtils.compareLevel(LevelOfEvidence.LEVEL_1, LevelOfEvidence.LEVEL_1) == 0);
        assertTrue(LevelUtils.compareLevel(LevelOfEvidence.LEVEL_1, LevelOfEvidence.LEVEL_3B) < 0);
        assertTrue(LevelUtils.compareLevel(LevelOfEvidence.LEVEL_3B, LevelOfEvidence.LEVEL_4) < 0);
        assertTrue(LevelUtils.compareLevel(LevelOfEvidence.LEVEL_1, LevelOfEvidence.LEVEL_R2) < 0);
        assertTrue(LevelUtils.compareLevel(LevelOfEvidence.LEVEL_R1, LevelOfEvidence.LEVEL_3B) < 0);
        assertTrue(LevelUtils.compareLevel(LevelOfEvidence.LEVEL_R1, LevelOfEvidence.LEVEL_1) < 0);
        assertTrue(LevelUtils.compareLevel(LevelOfEvidence.LEVEL_R2, LevelOfEvidence.LEVEL_4) > 0);
    }

    public void testGetHighestLevelFromEvidence() throws Exception {

    }

    public void testGetHighestLevel() throws Exception {

    }

    public void testGetHighestLevelFromEvidenceByLevels() throws Exception {

    }

    public void testGetLevelsFromEvidence() throws Exception {

    }

    public void testGetLevelsFromEvidenceByLevels() throws Exception {

    }

    public void testGetPublicLevels() throws Exception {

    }

    public void testGetPublicAndOtherIndicationLevels() throws Exception {

    }

    public void testSetToAlleleLevel() throws Exception {

    }

    public void testParseStringLevelOfEvidences() throws Exception {

    }

    public void testIsSensitiveLevel() throws Exception {

    }

    public void testIsResistanceLevel() throws Exception {

    }

    public void testGetAllLevels() throws Exception {

    }

    public void testGetSensitiveLevels() throws Exception {

    }

    public void testGetResistanceLevels() throws Exception {

    }

    public void testAreSameLevels() throws Exception {
        assertTrue(LevelUtils.areSameLevels(LevelOfEvidence.LEVEL_1, null) == false);
        assertTrue(LevelUtils.areSameLevels(LevelOfEvidence.LEVEL_1, LevelOfEvidence.LEVEL_R2) == false);
        assertTrue(LevelUtils.areSameLevels(LevelOfEvidence.LEVEL_R1, LevelOfEvidence.LEVEL_3B) == false);
        assertTrue(LevelUtils.areSameLevels(LevelOfEvidence.LEVEL_1, LevelOfEvidence.LEVEL_1) == true);
        assertTrue(LevelUtils.areSameLevels(null, null) == true);
    }

}
