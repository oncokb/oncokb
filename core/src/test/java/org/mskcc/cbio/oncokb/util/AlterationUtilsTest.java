package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.Alteration;

/**
 * Created by Hongxin Zhang on 6/20/18.
 */
public class AlterationUtilsTest extends TestCase {
    public void testIsPositionVariant() throws Exception {
        Alteration alteration = AlterationUtils.getAlteration("AKT1", "E17", null, "NA", null, null);
        assertTrue("This variant should be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17", null, null, null, null);
        assertTrue("This variant should be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17*", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17*", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "17", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17A", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17AA", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "EE17AA", null, null, null, null);
        assertFalse("This variant should NOT be position variant.", AlterationUtils.isPositionVariant(alteration));
    }

}
