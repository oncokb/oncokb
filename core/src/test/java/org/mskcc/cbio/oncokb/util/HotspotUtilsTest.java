package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by Hongxin on 3/17/17.
 */
public class HotspotUtilsTest extends TestCase {
    public void testIsHotspot() throws Exception {
        Alteration alteration = AlterationUtils.getAlteration("AKT1", "E17K", null, null, null, null, null);
        assertTrue("This missense mutation should be hotspot", HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17*", null, null, null, null, null);
        assertFalse("This stop gain variant should not be hotspot", HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P68_C77dup", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P65_C77dup", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P60_C65dup", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P60_C80dup", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P76_C80dup", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P76_C80delinsS", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P76_C77delinsSFG", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("EGFR", "L747Rfs*13", null, null, null, null, null);
        assertFalse(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("MET", "X1010splice", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        // The range missense mutations should not be hotspot
        alteration = AlterationUtils.getAlteration("PIK3CA", "979_1068mis", null, null, null, null, null);
        assertFalse(HotspotUtils.isHotspot(alteration));

        // PAK7 is an alias of PAK5
        alteration = AlterationUtils.getAlteration("PAK7", "M173I", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));

        // This is a test to govern when sample is in-frame indel and that range happens to be a hotspot of splie site. The variant should not be a hotspot
        alteration = AlterationUtils.getAlteration("TP53", "A307_L308insASFLS", null, null, null, null, null);
        assertFalse(HotspotUtils.isHotspot(alteration));

        // Positional variant should be considered similar to missense mutation
        alteration = AlterationUtils.getAlteration("BRAF", "V600", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration = AlterationUtils.getAlteration("BRAF", "T599", null, null, null, null, null);
        assertFalse(HotspotUtils.isHotspot(alteration));

        // Test reference genome
        // test a curated case
        alteration = AlterationUtils.getAlteration("BRAF", "V600E", null, null, null, null, null);
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh37));
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh38));
        assertTrue(HotspotUtils.isHotspot(alteration));

        // test a none curated case
        alteration = AlterationUtils.getAlteration("ANKRD11", "K369R", null, null, null, null, null);
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh37));
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh38));
        assertTrue(HotspotUtils.isHotspot(alteration));

        // For MYD88, we do not map cancer hotspot on GRCh38 yet
        alteration = AlterationUtils.getAlteration("MYD88", "M232T", null, null, null, null, null);
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh37));
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh38));
        assertFalse(HotspotUtils.isHotspot(alteration));

        // For missense hotspot, the reference residues need to be matched
        // For instance V600 is a valid hotspot, not A600
        alteration = AlterationUtils.getAlteration("BRAF", "V600E", null, null, null, null, null);
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration = AlterationUtils.getAlteration("BRAF", "A600E", null, null, null, null, null);
        assertFalse(HotspotUtils.isHotspot(alteration));
    }

}
