package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.genomenexus.TranscriptConsequence;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 * Created by Hongxin Zhang on 7/20/17.
 */
public class GenomeNexusUtilsTest extends TestCase {
    public void testGetTranscriptConsequence() throws Exception {
        TranscriptConsequence consequence = GenomeNexusUtils.getTranscriptConsequence("7:g.140453136A>T");
        Gene gene = GeneUtils.getGeneByHugoSymbol("BRAF");
        assertEquals("Picked transcript gene symbol is not BRAF, but it should.",
            gene.getHugoSymbol(), consequence.getGeneSymbol());
        assertEquals("Picked transcript hgvs p short is not p.V600E, but it should.",
            "p.V600E", consequence.getHgvspShort());
        assertEquals("Picked transcript protein start is not 600, but it should.",
            "600", consequence.getProteinStart());
        assertEquals("Picked transcript protein end is not 600, but it should.",
            "600", consequence.getProteinEnd());
        assertEquals("Picked transcript RefSeq is not the same with MSKIMPACT BRAF RefSeq, but it should.",
            gene.getCuratedRefSeq(), consequence.getRefSeq());
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT BRAF isoform, but it should.",
            gene.getCuratedIsoform(), consequence.getTranscriptId());


        // Isoform of TCF3 should be ENST00000344749, uniport default isoform is ENST00000262965
        consequence = GenomeNexusUtils.getTranscriptConsequence("19:g.1615454C>A");
        gene = GeneUtils.getGeneByHugoSymbol("TCF3");
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT TCF3 isoform, but it should.",
            gene.getCuratedIsoform(), consequence.getTranscriptId());
        assertEquals("Picked transcript isoform is not ENST00000344749, but it should.",
            "ENST00000344749", consequence.getTranscriptId());

        // Test frame shift variant
        consequence = GenomeNexusUtils.getTranscriptConsequence("22:g.41574678_41574679insC");
        assertEquals("Picked transcript protein change is not tH2324Pfs*55, but it should.",
            "p.H2324Pfs*55", consequence.getHgvspShort());
        assertEquals("Picked transcript protein change is not tH2324Pfs*55, but it should.",
            VariantConsequenceUtils.findVariantConsequenceByTerm("frameshift_variant"), VariantConsequenceUtils.findVariantConsequenceByTerm(consequence.getConsequence()));
    }

}
