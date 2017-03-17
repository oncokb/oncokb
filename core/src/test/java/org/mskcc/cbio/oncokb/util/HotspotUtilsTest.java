package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.Alteration;

/**
 * Created by Hongxin on 3/17/17.
 */
public class HotspotUtilsTest extends TestCase {
    public void testIsHotspot() throws Exception {
        Alteration alteration = AlterationUtils.getAlteration("AKT1", "E17K", null, null, null, null);
        assertTrue("This missense mutation should be hotspot", HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17*", null, null, null, null);
        assertFalse("This stop gain variant should not be hotspot", HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17", null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

    }

}
