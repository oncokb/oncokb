package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.genomenexus.TranscriptConsequence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

/**
 * Created by Hongxin Zhang on 7/20/17.
 */
public class GenomeNexusUtilsTest extends TestCase {
    public void testGetTranscriptConsequence() throws Exception {
        ReferenceGenome mskReferenceGenome = ReferenceGenome.GRCH37;
        TranscriptConsequence consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, "7:g.140453136A>T", mskReferenceGenome);
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
            gene.getGrch37RefSeq(), consequence.getRefSeq());
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT BRAF isoform, but it should.",
            gene.getGrch37Isoform(), consequence.getTranscriptId());


        // Isoform of TCF3 should be ENST00000344749, uniport default isoform is ENST00000262965
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, "19:g.1615454C>A", mskReferenceGenome);
        gene = GeneUtils.getGeneByHugoSymbol("TCF3");
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT TCF3 isoform, but it should.",
            gene.getGrch37Isoform(), consequence.getTranscriptId());
        assertEquals("Picked transcript isoform is not ENST00000344749, but it should.",
            "ENST00000344749", consequence.getTranscriptId());

        // Test frame shift variant
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, "22:g.41574678_41574679insC", mskReferenceGenome);
        assertEquals("Picked transcript protein change is not tH2324Pfs*55, but it should.",
            "p.H2324Pfs*55", consequence.getHgvspShort());
        assertEquals("Picked transcript protein change is not tH2324Pfs*55, but it should.",
            VariantConsequenceUtils.findVariantConsequenceByTerm("frameshift_variant"), VariantConsequenceUtils.findVariantConsequenceByTerm(consequence.getConsequence()));

        // BRAF V600E genomic location
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.GENOMIC_LOCATION, "7,140453136,140453136,A,T", mskReferenceGenome);
        gene = GeneUtils.getGeneByHugoSymbol("BRAF");
        assertEquals("Picked transcript gene symbol is not BRAF, but it should.",
            gene.getHugoSymbol(), consequence.getGeneSymbol());
        assertEquals("Picked transcript hgvs p short is not p.V600E, but it should.",
            "p.V600E", consequence.getHgvspShort());
        assertEquals("Picked transcript protein start is not 600, but it should.",
            "600", consequence.getProteinStart());
        assertEquals("Picked transcript protein end is not 600, but it should.",
            "600", consequence.getProteinEnd());
        assertEquals("Picked transcript RefSeq is not the same with MSKIMPACT BRAF RefSeq, but it should.",
            gene.getGrch37RefSeq(), consequence.getRefSeq());
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT BRAF isoform, but it should.",
            gene.getGrch37Isoform(), consequence.getTranscriptId());


        // Potential multiple consequences
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.GENOMIC_LOCATION, "3,178917478,178917478,G,A", mskReferenceGenome);
        gene = GeneUtils.getGeneByHugoSymbol("PIK3CA");
        assertEquals("Picked transcript gene symbol is not PIK3CA, but it should.",
            gene.getHugoSymbol(), consequence.getGeneSymbol());
        assertEquals("Picked transcript hgvs p short is not p.V600E, but it should.",
            "p.G118D", consequence.getHgvspShort());
        assertEquals("Picked transcript protein start is not 118, but it should.",
            "118", consequence.getProteinStart());
        assertEquals("Picked transcript protein end is not 118, but it should.",
            "118", consequence.getProteinEnd());
        assertEquals("Picked transcript RefSeq is not the same with MSKIMPACT PIK3CA RefSeq, but it should.",
            gene.getGrch37RefSeq(), consequence.getRefSeq());
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT BRAF isoform, but it should.",
            gene.getGrch37Isoform(), consequence.getTranscriptId());
        assertTrue("There are multiple consequences", consequence.getConsequenceTerms().size() > 1);
    }

}
