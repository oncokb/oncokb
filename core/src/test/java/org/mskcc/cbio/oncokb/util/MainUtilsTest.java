package org.mskcc.cbio.oncokb.util;

import static org.mskcc.cbio.oncokb.util.MainUtils.*;

import org.cbioportal.genome_nexus.model.GenomicLocation;
import org.junit.Test;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.AnnotationSearchQueryType;
import org.mskcc.cbio.oncokb.model.MutationEffect;

/**
 * Created by Hongxin Zhang on 3/1/18.
 */
public class MainUtilsTest extends TestCase {
    public void testSetToAlternativeAlleleMutationEffect() throws Exception {
        MutationEffect mutationEffect = null;

        // Neutral
        mutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(null);
        assertEquals("The null should be returned.", null, mutationEffect);

        // Neutral
        mutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(MutationEffect.NEUTRAL);
        assertEquals("The neutral should not be propagated.", null, mutationEffect);

        // Likely Neutral
        mutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(MutationEffect.LIKELY_NEUTRAL);
        assertEquals("The likely neutral should not be propagated.", null, mutationEffect);

        // Inconclusive
        mutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(MutationEffect.INCONCLUSIVE);
        assertEquals("The inconclusive should not be propagated.", null, mutationEffect);

        // Gain-of-function
        mutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(MutationEffect.GAIN_OF_FUNCTION);
        assertEquals("The Gain-of-function should be propagated to likely gain-of-function.", MutationEffect.LIKELY_GAIN_OF_FUNCTION, mutationEffect);

        // Likely Gain-of-function
        mutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(MutationEffect.LIKELY_GAIN_OF_FUNCTION);
        assertEquals("The Likely Gain-of-function should be propagated to likely gain-of-function.", MutationEffect.LIKELY_GAIN_OF_FUNCTION, mutationEffect);

    }

    public void testIsEGFRTruncatingVariants() throws Exception {
        assertTrue(MainUtils.isEGFRTruncatingVariants("vIVa"));
        assertTrue(MainUtils.isEGFRTruncatingVariants("vIVb"));
        assertTrue(MainUtils.isEGFRTruncatingVariants("vIVc"));
        assertTrue(MainUtils.isEGFRTruncatingVariants("vII"));
        assertTrue(MainUtils.isEGFRTruncatingVariants("vIII"));
        assertTrue(MainUtils.isEGFRTruncatingVariants("vV"));
        assertFalse(MainUtils.isEGFRTruncatingVariants("vIIIa"));
        assertFalse(MainUtils.isEGFRTruncatingVariants("vIVd"));
        assertFalse(MainUtils.isEGFRTruncatingVariants("vVi"));
        assertFalse(MainUtils.isEGFRTruncatingVariants("test"));
        assertFalse(MainUtils.isEGFRTruncatingVariants("EGFRvIVa"));
    }

    public void testReplaceLast() {
        assertEquals("A", replaceLast("A", "and", ","));
        assertEquals("A,B", replaceLast("AandB", "and", ","));
        assertEquals("A,B,", replaceLast("A,Band", "and", ","));
        assertEquals("A,B,D", replaceLast("A,BandD", "and", ","));
    }

    public void testToLowerCaseExceptAllCaps() {
        assertEquals("test", lowerCaseAlterationName("test"));
        assertEquals("test", lowerCaseAlterationName("Test"));
        assertEquals("test", lowerCaseAlterationName("TesT"));
        assertEquals("TEST", lowerCaseAlterationName("TEST"));
        assertEquals("TEST-A", lowerCaseAlterationName("TEST-A"));
        assertEquals("test-A", lowerCaseAlterationName("Test-A"));
        assertEquals("test_a", lowerCaseAlterationName("Test_A"));
        assertEquals("test A", lowerCaseAlterationName("TesT A"));
        assertEquals("TEST A", lowerCaseAlterationName("TEST A"));
        assertEquals("TEst", lowerCaseAlterationName("TEst"));
        assertEquals("TEst-test", lowerCaseAlterationName("TEst-tEst"));
        assertEquals("teST", lowerCaseAlterationName("teST"));
        assertEquals("test", lowerCaseAlterationName("tesT"));
        assertEquals("", lowerCaseAlterationName(""));
        assertEquals("_", lowerCaseAlterationName("_"));
        assertEquals("1", lowerCaseAlterationName("1"));
        assertEquals("-", lowerCaseAlterationName("-"));
        assertEquals("t", lowerCaseAlterationName("t"));
        assertEquals("T", lowerCaseAlterationName("T"));
        assertEquals("?", lowerCaseAlterationName("?"));
    }

    public void testCompareAnnotationSearchQueryType() {
        assertTrue(compareAnnotationSearchQueryType(AnnotationSearchQueryType.GENE, AnnotationSearchQueryType.VARIANT, true) < 0);
        assertTrue(compareAnnotationSearchQueryType(AnnotationSearchQueryType.GENE, AnnotationSearchQueryType.CANCER_TYPE, true) < 0);
        assertTrue(compareAnnotationSearchQueryType(AnnotationSearchQueryType.GENE, AnnotationSearchQueryType.DRUG, true) < 0);
        assertTrue(compareAnnotationSearchQueryType(AnnotationSearchQueryType.VARIANT, AnnotationSearchQueryType.CANCER_TYPE, true) < 0);
        assertTrue(compareAnnotationSearchQueryType(AnnotationSearchQueryType.GENE, null, true) < 0);
        assertTrue(compareAnnotationSearchQueryType(null, AnnotationSearchQueryType.GENE, true) > 0);
        assertTrue(compareAnnotationSearchQueryType(AnnotationSearchQueryType.GENE, AnnotationSearchQueryType.GENE, true) == 0);
        assertTrue(compareAnnotationSearchQueryType(null, null, true) == 0);
        assertTrue(compareAnnotationSearchQueryType(AnnotationSearchQueryType.VARIANT, AnnotationSearchQueryType.GENE, true) > 0);
        assertTrue(compareAnnotationSearchQueryType(AnnotationSearchQueryType.VARIANT, AnnotationSearchQueryType.GENE, false) < 0);
    }

    public void testParseChromosomeAndRangeFromHGVSg() {
        GenomicLocation parsedGl = parseChromosomeAndRangeFromHGVSg("7:g.100A>T");
        assertEquals("7", parsedGl.getChromosome());
        assertEquals(new Integer(100), parsedGl.getStart());
        assertEquals(new Integer(100), parsedGl.getEnd());
        

        parsedGl = parseChromosomeAndRangeFromHGVSg("X:g.100_105del");
        assertEquals("X", parsedGl.getChromosome());
        assertEquals(new Integer(100), parsedGl.getStart());
        assertEquals(new Integer(105), parsedGl.getEnd());

        assertEquals(null, parseChromosomeAndRangeFromHGVSg(""));
        assertEquals(null, parseChromosomeAndRangeFromHGVSg(":g.100A>T"));
        assertEquals(null, parseChromosomeAndRangeFromHGVSg("7:g."));
        assertEquals(null, parseChromosomeAndRangeFromHGVSg("7:g.A"));
        assertEquals(null, parseChromosomeAndRangeFromHGVSg("7:g.A_B"));
        assertEquals(null, parseChromosomeAndRangeFromHGVSg("7:g.100_A"));
        assertEquals(null, parseChromosomeAndRangeFromHGVSg("7:g.A_100"));
    }

    public void testFindDigitEndIndex() {
        assertEquals(-1, findDigitEndIndex("100T", -1));
        assertEquals(-1, findDigitEndIndex("100-", 3));
        assertEquals(-1, findDigitEndIndex("abc123def", 0));

        assertEquals(3, findDigitEndIndex("100T", 0));
        assertEquals(3, findDigitEndIndex("100-", 0));
        assertEquals(6, findDigitEndIndex("abc123def", 3));
        assertEquals(6, findDigitEndIndex("abc123def", 5));
    }
}
