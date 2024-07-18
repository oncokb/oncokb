package org.mskcc.cbio.oncokb.util;

import org.junit.Assert;
import org.junit.Test;

public class AminoAcidConverterUtilsTest {

    @Test
    public void testResolveHgvspShortFromHgvspWithNullInput() {
        Assert.assertNull("Expected null for null input", AminoAcidConverterUtils.resolveHgvspShortFromHgvsp(null));
    }

    @Test
    public void testResolveHgvspShortFromHgvspWithNoConversion() {
        String input = "p.V600E";
        String expected = "p.V600E";
        Assert.assertEquals("Expected same string for input without three-letter codes", expected, AminoAcidConverterUtils.resolveHgvspShortFromHgvsp(input));
    }

    @Test
    public void testResolveHgvspShortFromHgvspWithSingleConversion() {
        String input = "p.Val600Glu";
        String expected = "p.V600E";
        Assert.assertEquals("Expected conversion from three-letter to one-letter amino acid codes", expected, AminoAcidConverterUtils.resolveHgvspShortFromHgvsp(input));
    }

    @Test
    public void testResolveHgvspShortFromHgvspWithMultipleConversions() {
        String input = "p.Arg143Gln/Val600Glu";
        String expected = "p.R143Q/V600E";
        Assert.assertEquals("Expected conversion for multiple three-letter codes", expected, AminoAcidConverterUtils.resolveHgvspShortFromHgvsp(input));
    }

    @Test
    public void testResolveHgvspShortFromHgvspWithComplexString() {
        String input = "p.(Gly12Ser/Val600Glu)";
        String expected = "p.(G12S/V600E)";
        Assert.assertEquals("Expected conversion within complex string", expected, AminoAcidConverterUtils.resolveHgvspShortFromHgvsp(input));
    }
}
