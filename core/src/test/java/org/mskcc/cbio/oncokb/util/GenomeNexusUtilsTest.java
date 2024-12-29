package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.genome_nexus.client.TranscriptConsequenceSummary;
import org.junit.Assert;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.VariantConsequence;


import static org.junit.Assert.assertNotEquals;
import static org.mskcc.cbio.oncokb.Constants.FRAMESHIFT_VARIANT;
import static org.mskcc.cbio.oncokb.Constants.IN_FRAME_DELETION;

/**
 * Created by Hongxin Zhang on 7/20/17.
 */
public class GenomeNexusUtilsTest extends TestCase {
    public void testGetTranscriptConsequence() throws Exception {
        final String BRAF_V600E_37 = "7:g.140453136A>T";
        ReferenceGenome mskReferenceGenome = ReferenceGenome.GRCh37;
        TranscriptConsequenceSummary consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, BRAF_V600E_37, mskReferenceGenome);
        Gene gene = GeneUtils.getGeneByHugoSymbol("BRAF");
        assertEquals("Picked transcript gene symbol is not BRAF, but it should.",
            gene.getHugoSymbol(), consequence.getHugoGeneSymbol());
        assertEquals("Picked transcript hgvs p short is not p.V600E, but it should.",
            "p.V600E", consequence.getHgvspShort());
        assertEquals("Picked transcript protein start is not 600, but it should.",
            "600", Integer.toString(consequence.getProteinPosition().getStart()));
        assertEquals("Picked transcript protein end is not 600, but it should.",
            "600", Integer.toString(consequence.getProteinPosition().getEnd()));
        assertEquals("Picked transcript RefSeq is not the same with MSKIMPACT BRAF RefSeq, but it should.",
            gene.getGrch37RefSeq(), consequence.getRefSeq());
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT BRAF isoform, but it should.",
            gene.getGrch37Isoform(), consequence.getTranscriptId());

        // the same BRAF V600E GRCh37 change should not get annotated by GN in GRCh38
        TranscriptConsequenceSummary consequence38 = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, BRAF_V600E_37, ReferenceGenome.GRCh38);
        assertNotEquals("The consequence should not be the same", consequence, consequence38);


        final String BRAF_V600E_38 = "7:g.140753336A>T";
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, BRAF_V600E_38, ReferenceGenome.GRCh38);
        gene = GeneUtils.getGeneByHugoSymbol("BRAF");
        assertEquals("Picked transcript gene symbol is not BRAF, but it should.",
            gene.getHugoSymbol(), consequence.getHugoGeneSymbol());
        assertEquals("Picked transcript hgvs p short is not p.V600E, but it should.",
            "p.V600E", consequence.getHgvspShort());
        assertEquals("Picked transcript protein start is not 600, but it should.",
            "600", Integer.toString(consequence.getProteinPosition().getStart()));
        assertEquals("Picked transcript protein end is not 600, but it should.",
            "600", Integer.toString(consequence.getProteinPosition().getEnd()));


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
            VariantConsequenceUtils.findVariantConsequenceByTerm(FRAMESHIFT_VARIANT), VariantConsequenceUtils.findVariantConsequenceByTerm(consequence.getConsequenceTerms()));

        // BRAF V600E genomic location
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.GENOMIC_LOCATION, "7,140453136,140453136,A,T", mskReferenceGenome);
        gene = GeneUtils.getGeneByHugoSymbol("BRAF");
        assertEquals("Picked transcript gene symbol is not BRAF, but it should.",
            gene.getHugoSymbol(), consequence.getHugoGeneSymbol());
        assertEquals("Picked transcript hgvs p short is not p.V600E, but it should.",
            "p.V600E", consequence.getHgvspShort());
        assertEquals("Picked transcript protein start is not 600, but it should.",
            "600", Integer.toString(consequence.getProteinPosition().getStart()));
        assertEquals("Picked transcript protein end is not 600, but it should.",
            "600", Integer.toString(consequence.getProteinPosition().getEnd()));
        assertEquals("Picked transcript RefSeq is not the same with MSKIMPACT BRAF RefSeq, but it should.",
            gene.getGrch37RefSeq(), consequence.getRefSeq());
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT BRAF isoform, but it should.",
            gene.getGrch37Isoform(), consequence.getTranscriptId());


        // Potential multiple consequences
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.GENOMIC_LOCATION, "3,178917478,178917478,G,A", mskReferenceGenome);
        gene = GeneUtils.getGeneByHugoSymbol("PIK3CA");
        assertEquals("Picked transcript gene symbol is not PIK3CA, but it should.",
            gene.getHugoSymbol(), consequence.getHugoGeneSymbol());
        assertEquals("Picked transcript hgvs p short is not p.V600E, but it should.",
            "p.G118D", consequence.getHgvspShort());
        assertEquals("Picked transcript protein start is not 118, but it should.",
            "118", Integer.toString(consequence.getProteinPosition().getStart()));
        assertEquals("Picked transcript protein end is not 118, but it should.",
            "118", Integer.toString(consequence.getProteinPosition().getEnd()));
        assertEquals("Picked transcript RefSeq is not the same with MSKIMPACT PIK3CA RefSeq, but it should.",
            gene.getGrch37RefSeq(), consequence.getRefSeq());
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT BRAF isoform, but it should.",
            gene.getGrch37Isoform(), consequence.getTranscriptId());
        assertTrue("We no longer return multiple terms, only return the first term", !consequence.getConsequenceTerms().contains(","));

        // No consequence can be found for the following
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.GENOMIC_LOCATION, "9,136519547,136519547,C,C", ReferenceGenome.GRCh38);
        Assert.assertNull("No consequence should return for this genomic location", consequence);

        // the picked transcript consequence terms might not include the most_severe_consequence
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.GENOMIC_LOCATION, "3, 38182641, 38182641,T,C", mskReferenceGenome);
        gene = GeneUtils.getGeneByHugoSymbol("MYD88");
        assertEquals("Picked transcript gene symbol is not MYD88, but it should.",
            gene.getHugoSymbol(), consequence.getHugoGeneSymbol());
        assertEquals("Picked transcript hgvs p short is not p.L265P, but it should.",
            "p.L265P", consequence.getHgvspShort());
        assertEquals("Picked transcript protein start is not 265, but it should.",
            "265", Integer.toString(consequence.getProteinPosition().getStart()));
        assertEquals("Picked transcript protein end is not 265, but it should.",
            "265", Integer.toString(consequence.getProteinPosition().getEnd()));
        assertEquals("Picked transcript RefSeq is not the same with MSKIMPACT MYD88 RefSeq, but it should.",
            gene.getGrch37RefSeq(), consequence.getRefSeq());
        assertEquals("Picked transcript isoform is not the same with MSKIMPACT MYD88 isoform, but it should.",
            gene.getGrch37Isoform(), consequence.getTranscriptId());
        assertTrue("We no longer return multiple terms, only return the first term", !consequence.getConsequenceTerms().contains(","));
        assertEquals("The consequence term is not expected", "missense_variant", consequence.getConsequenceTerms());

        // Test few more cases that reported by user which are supposed to have annotation
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, "4:g.55593600_55593606delinsGTGG", mskReferenceGenome);
        assertEquals("Picked transcript gene symbol is not expected, but it should.", "3815", consequence.getEntrezGeneId());
        assertEquals("HGVSp short is not expected, but it should.", "p.Q556_K558delinsVE", consequence.getHgvspShort());
        assertEquals("Consequence is not expected, but it should.", IN_FRAME_DELETION, consequence.getConsequenceTerms());
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, "5:g.67589635_67589639delinsTA", mskReferenceGenome);
        assertEquals("Picked transcript gene symbol is not expected, but it should.", "5295", consequence.getEntrezGeneId());
        assertEquals("HGVSp short is not expected, but it should.", "p.L466_E468delinsFK", consequence.getHgvspShort());
        assertEquals("Consequence is not expected, but it should.", IN_FRAME_DELETION, consequence.getConsequenceTerms());

        //TERT promoter
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.GENOMIC_LOCATION, "5,1295167,1295168,TC,AATG", mskReferenceGenome);
        assertEquals("Picked transcript gene symbol is not expected, but it should.", "TERT", consequence.getHugoGeneSymbol());
        assertEquals("Consequence is not expected, but it should.", "upstream_gene_variant", consequence.getConsequenceTerms());
        // location within TERT gene
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, "5:g.1295167_1295168delinsAATG", mskReferenceGenome);
        assertEquals("Picked transcript gene symbol is not expected, but it should.", "TERT", consequence.getHugoGeneSymbol());
        assertEquals("Consequence is not expected, but it should.", "upstream_gene_variant", consequence.getConsequenceTerms());
        // location out of TERT gene on regulatory sequence
        consequence = GenomeNexusUtils.getTranscriptConsequence(GNVariantAnnotationType.HGVS_G, "5:g.1295228G>A", mskReferenceGenome);
        assertEquals("Picked transcript gene symbol is not expected, but it should.", "TERT", consequence.getHugoGeneSymbol());
        assertEquals("Consequence is not expected, but it should.", "upstream_gene_variant", consequence.getConsequenceTerms());
    }
}
