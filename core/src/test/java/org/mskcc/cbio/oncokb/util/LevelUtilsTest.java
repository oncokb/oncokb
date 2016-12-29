package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;

/**
 * Created by Hongxin on 12/29/16.
 */
public class LevelUtilsTest extends TestCase {
    public void testAreSameLevels() throws Exception {
        assertTrue(LevelUtils.areSameLevels(LevelOfEvidence.LEVEL_1, null) == false);
        assertTrue(LevelUtils.areSameLevels(LevelOfEvidence.LEVEL_1, LevelOfEvidence.LEVEL_R2) == false);
        assertTrue(LevelUtils.areSameLevels(LevelOfEvidence.LEVEL_R1, LevelOfEvidence.LEVEL_2B) == false);
        assertTrue(LevelUtils.areSameLevels(LevelOfEvidence.LEVEL_1, LevelOfEvidence.LEVEL_1) == true);
        assertTrue(LevelUtils.areSameLevels(null, null) == true);
    }

}
