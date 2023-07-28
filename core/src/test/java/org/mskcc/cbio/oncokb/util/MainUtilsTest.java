package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.MutationEffect;

import static org.mskcc.cbio.oncokb.util.MainUtils.replaceLast;
import static org.mskcc.cbio.oncokb.util.MainUtils.toLowerCaseExceptAllCaps;

/**
 * Created by Hongxin Zhang on 3/1/18.
 */
public class MainUtilsTest extends TestCase {
    public void testSetToAlternativeAlleleMutationEffect() throws Exception {
        IndicatorQueryMutationEffect indicatorQueryMutationEffect = new IndicatorQueryMutationEffect();

        // Neutral
        indicatorQueryMutationEffect.setMutationEffect(null);
        indicatorQueryMutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(indicatorQueryMutationEffect);
        assertEquals("The null should be returned.", null, indicatorQueryMutationEffect.getMutationEffect());

        // Neutral
        indicatorQueryMutationEffect.setMutationEffect(MutationEffect.NEUTRAL);
        indicatorQueryMutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(indicatorQueryMutationEffect);
        assertEquals("The neutral should not be propagated.", null, indicatorQueryMutationEffect.getMutationEffect());

        // Likely Neutral
        indicatorQueryMutationEffect.setMutationEffect(MutationEffect.LIKELY_NEUTRAL);
        indicatorQueryMutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(indicatorQueryMutationEffect);
        assertEquals("The likely neutral should not be propagated.", null, indicatorQueryMutationEffect.getMutationEffect());

        // Inconclusive
        indicatorQueryMutationEffect.setMutationEffect(MutationEffect.INCONCLUSIVE);
        indicatorQueryMutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(indicatorQueryMutationEffect);
        assertEquals("The inconclusive should not be propagated.", null, indicatorQueryMutationEffect.getMutationEffect());

        // Gain-of-function
        indicatorQueryMutationEffect.setMutationEffect(MutationEffect.GAIN_OF_FUNCTION);
        indicatorQueryMutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(indicatorQueryMutationEffect);
        assertEquals("The Gain-of-function should be propagated to likely gain-of-function.", MutationEffect.LIKELY_GAIN_OF_FUNCTION, indicatorQueryMutationEffect.getMutationEffect());

        // Likely Gain-of-function
        indicatorQueryMutationEffect.setMutationEffect(MutationEffect.LIKELY_GAIN_OF_FUNCTION);
        indicatorQueryMutationEffect = MainUtils.setToAlternativeAlleleMutationEffect(indicatorQueryMutationEffect);
        assertEquals("The Likely Gain-of-function should be propagated to likely gain-of-function.", MutationEffect.LIKELY_GAIN_OF_FUNCTION, indicatorQueryMutationEffect.getMutationEffect());

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
        assertEquals("test", toLowerCaseExceptAllCaps("test"));
        assertEquals("test", toLowerCaseExceptAllCaps("Test"));
        assertEquals("test", toLowerCaseExceptAllCaps("TesT"));
        assertEquals("TEST", toLowerCaseExceptAllCaps("TEST"));
        assertEquals("TEST-A", toLowerCaseExceptAllCaps("TEST-A"));
        assertEquals("test-A", toLowerCaseExceptAllCaps("Test-A"));
        assertEquals("test_a", toLowerCaseExceptAllCaps("Test_A"));
        assertEquals("test A", toLowerCaseExceptAllCaps("TesT A"));
        assertEquals("TEST A", toLowerCaseExceptAllCaps("TEST A"));
        assertEquals("", toLowerCaseExceptAllCaps(""));
        assertEquals("_", toLowerCaseExceptAllCaps("_"));
        assertEquals("1", toLowerCaseExceptAllCaps("1"));
        assertEquals("-", toLowerCaseExceptAllCaps("-"));
        assertEquals("t", toLowerCaseExceptAllCaps("t"));
        assertEquals("T", toLowerCaseExceptAllCaps("T"));
        assertEquals("?", toLowerCaseExceptAllCaps("?"));
    }
}
