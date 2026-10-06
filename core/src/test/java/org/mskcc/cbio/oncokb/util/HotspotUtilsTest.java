package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.CancerHotspot;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Created by Hongxin on 3/17/17.
 */
public class HotspotUtilsTest extends TestCase {
    public void testIsHotspot() throws Exception {
        Alteration alteration = AlterationUtils.getAlteration("AKT1", "E17K", null, null, null, null, null, false);
        assertTrue("This missense mutation should be hotspot", HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17*", null, null, null, null, null, false);
        assertFalse("This stop gain variant should not be hotspot", HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P68_C77dup", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P65_C77dup", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P60_C65dup", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P60_C80dup", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P76_C80dup", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P76_C80delinsS", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "P76_C77delinsSFG", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("EGFR", "L747Rfs*13", null, null, null, null, null, false);
        assertFalse(HotspotUtils.isHotspot(alteration));

        alteration = AlterationUtils.getAlteration("MET", "X1010splice", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        // The range missense mutations should not be hotspot
        alteration = AlterationUtils.getAlteration("PIK3CA", "979_1068mis", null, null, null, null, null, false);
        assertFalse(HotspotUtils.isHotspot(alteration));

        // PAK7 is an alias of PAK5
        alteration = AlterationUtils.getAlteration("PAK7", "M173I", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));

        // This is a test to govern when sample is in-frame indel and that range happens to be a hotspot of splie site. The variant should not be a hotspot
        alteration = AlterationUtils.getAlteration("TP53", "A307_L308insASFLS", null, null, null, null, null, false);
        assertFalse(HotspotUtils.isHotspot(alteration));

        // Positional variant should be considered similar to missense mutation
        alteration = AlterationUtils.getAlteration("BRAF", "V600", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration = AlterationUtils.getAlteration("BRAF", "T599", null, null, null, null, null, false);
        assertFalse(HotspotUtils.isHotspot(alteration));

        // Test reference genome
        // test a curated case
        alteration = AlterationUtils.getAlteration("BRAF", "V600E", null, null, null, null, null, false);
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh37));
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh38));
        assertTrue(HotspotUtils.isHotspot(alteration));

        // test a none curated case
        alteration = AlterationUtils.getAlteration("ANKRD11", "K369R", null, null, null, null, null, false);
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh37));
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh38));
        assertTrue(HotspotUtils.isHotspot(alteration));

        // For MYD88, we do not map cancer hotspot on GRCh38 yet
        alteration = AlterationUtils.getAlteration("MYD88", "M232T", null, null, null, null, null, false);
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh37));
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration.setReferenceGenomes(Collections.singleton(ReferenceGenome.GRCh38));
        assertFalse(HotspotUtils.isHotspot(alteration));

        // For missense hotspot, the reference residues need to be matched
        // For instance V600 is a valid hotspot, not A600
        alteration = AlterationUtils.getAlteration("BRAF", "V600E", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration = AlterationUtils.getAlteration("BRAF", "A600E", null, null, null, null, null, false);
        assertFalse(HotspotUtils.isHotspot(alteration));

        // for range alteration, unless the hotspot covers the whole range, it should not be considered hotspot
        // the exact range
        alteration = AlterationUtils.getAlteration("EGFR", "745_759del", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration = AlterationUtils.getAlteration("EGFR", "745_759ins", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));
        // the range covered by hotspot
        alteration = AlterationUtils.getAlteration("EGFR", "746_759del", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));
        alteration = AlterationUtils.getAlteration("EGFR", "746_759ins", null, null, null, null, null, false);
        assertTrue(HotspotUtils.isHotspot(alteration));
        // partial the range is outside the hotspot range
        alteration = AlterationUtils.getAlteration("EGFR", "744_759del", null, null, null, null, null, false);
        assertFalse(HotspotUtils.isHotspot(alteration));
        alteration = AlterationUtils.getAlteration("EGFR", "744_759ins", null, null, null, null, null, false);
        assertFalse(HotspotUtils.isHotspot(alteration));
    }

    public void testGetHotspotType() throws Exception {
        // A missense mutation matches the single residue hotspot at its position
        Alteration alteration = AlterationUtils.getAlteration("BRAF", "V600E", null, null, null, null, null, false);
        assertEquals("single residue", HotspotUtils.getHotspotType(alteration));

        // An in-frame deletion covering that same residue is a hotspot of the
        // in-frame indel range, not of the single residue hotspot it overlaps
        alteration = AlterationUtils.getAlteration("BRAF", "L485_P490del", null, null, null, null, null, false);
        assertEquals("in-frame indel", HotspotUtils.getHotspotType(alteration));

        alteration = AlterationUtils.getAlteration("MET", "X1010splice", null, null, null, null, null, false);
        assertEquals("splice site", HotspotUtils.getHotspotType(alteration));

        // Not a hotspot, so no type
        alteration = AlterationUtils.getAlteration("AKT1", "E17*", null, null, null, null, null, false);
        assertNull(HotspotUtils.getHotspotType(alteration));
    }

    public void testGetHotspotPmids() throws Exception {
        Alteration alteration = AlterationUtils.getAlteration("BRAF", "V600E", null, null, null, null, null, false);
        assertEquals(new LinkedHashSet<>(Arrays.asList("26619011", "29247016")), HotspotUtils.getHotspotPmids(alteration));

        alteration = AlterationUtils.getAlteration("TP53", "X307splice", null, null, null, null, null, false);
        assertEquals(Collections.singleton("29247016"), HotspotUtils.getHotspotPmids(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E40K", null, null, null, null, null, false);
        assertEquals(Collections.singleton("41895280"), HotspotUtils.getHotspotPmids(alteration));

        alteration = AlterationUtils.getAlteration("BRAF", "L485_P490del", null, null, null, null, null, false);
        assertEquals(Collections.singleton("29247016"), HotspotUtils.getHotspotPmids(alteration));

        alteration = AlterationUtils.getAlteration("AKT1", "E17*", null, null, null, null, null, false);
        assertTrue(HotspotUtils.getHotspotPmids(alteration).isEmpty());
    }

    public void testGetHotspot() throws Exception {
        CancerHotspot hotspot = HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("BRAF"), "V600");
        assertEquals("V600", hotspot.getName());
        assertEquals("single residue", hotspot.getType());
        assertEquals(Integer.valueOf(600), hotspot.getProteinStart());
        assertEquals(Integer.valueOf(600), hotspot.getProteinEnd());
        assertEquals(Integer.valueOf(897), hotspot.getTumorCount());
        assertEquals(new LinkedHashSet<>(Arrays.asList("26619011", "29247016")), hotspot.getPmids());

        hotspot = HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("TP53"), "X307");
        assertEquals("splice site", hotspot.getType());
        assertEquals("X307", hotspot.getName());

        hotspot = HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("PIK3R1"), "439-470");
        assertEquals("in-frame indel", hotspot.getType());
        assertEquals("439_470insdel", hotspot.getName());
        assertEquals(Integer.valueOf(439), hotspot.getProteinStart());
        assertEquals(Integer.valueOf(470), hotspot.getProteinEnd());

        assertNull(HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("BRAF"), "V601"));
        assertNull(HotspotUtils.getHotspot(null, "V600"));
    }

    public void testGetCuratedAlterations() throws Exception {
        CancerHotspot v600 = HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("BRAF"), "V600");
        List<String> names = HotspotUtils.getCuratedAlterations(GeneUtils.getGeneByHugoSymbol("BRAF"), v600).stream().map(Alteration::getAlteration).collect(Collectors.toList());
        assertTrue(names.contains("V600E"));
        assertTrue(names.contains("V600K"));
        assertFalse(names.contains("V600"));
        assertFalse(names.contains("K601E"));
        assertTrue(names.stream().allMatch(name -> name.matches("V600[A-Z]")));

        CancerHotspot range = HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("PIK3R1"), "439-470");
        List<Alteration> indels = HotspotUtils.getCuratedAlterations(GeneUtils.getGeneByHugoSymbol("PIK3R1"), range);
        assertFalse(indels.isEmpty());
        for (Alteration indel : indels) {
            assertTrue(AlterationUtils.isInframeAlteration(indel));
            assertTrue(indel.getProteinStart() <= 470 && indel.getProteinEnd() >= 439);
        }

        CancerHotspot splice = HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("TP53"), "X307");
        for (Alteration alteration : HotspotUtils.getCuratedAlterations(GeneUtils.getGeneByHugoSymbol("TP53"), splice)) {
            assertEquals(Integer.valueOf(307), alteration.getProteinStart());
        }

        assertTrue(HotspotUtils.getCuratedAlterations(GeneUtils.getGeneByHugoSymbol("BRAF"), null).isEmpty());
    }

    public void testCancerHotspotText() throws Exception {
        CancerHotspot v600 = HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("BRAF"), "V600");
        assertEquals("BRAF V600 has been identified as a statistically significant hotspot and single residue mutations at this position are considered likely oncogenic unless functional evidence suggests otherwise.", SummaryUtils.cancerHotspotSummary(v600));
        String description = SummaryUtils.cancerHotspotMutationEffectDescription(v600);
        assertTrue(description.startsWith("BRAF V600 has been identified as a statistically significant recurrent mutational hotspot (PMID: 26619011, 29247016). In these analyses,"));
        assertTrue(description.endsWith("Therefore, single-residue substitutions at BRAF V600 are considered likely oncogenic unless functional evidence indicates otherwise (PMID: 26619011, 29247016, 41895280)."));

        CancerHotspot range = HotspotUtils.getHotspot(GeneUtils.getGeneByHugoSymbol("PIK3R1"), "439-470");
        assertEquals("PIK3R1 439_470insdel has been identified as a statistically significant hotspot and in-frame indels overlapping this region are considered likely oncogenic unless functional evidence suggests otherwise.", SummaryUtils.cancerHotspotSummary(range));
        description = SummaryUtils.cancerHotspotMutationEffectDescription(range);
        assertTrue(description.startsWith("PIK3R1 439_470insdel has been identified as a statistically significant recurrent in-frame indel hotspot (PMID: 29247016). In this analysis,"));
        assertTrue(description.contains("overlap at least one amino acid within PIK3R1 439\u2013470 are considered"));
    }
}
