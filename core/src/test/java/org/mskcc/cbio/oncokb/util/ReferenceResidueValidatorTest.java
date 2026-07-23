package org.mskcc.cbio.oncokb.util;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class ReferenceResidueValidatorTest {

    // BRAF-like sequence: 1-based positions -> M(1) A(2) A(3) L(4) S(5) G(6) V(7)
    private static final String SEQUENCE = "MAALSGV";

    @Test
    public void noMessageWhenReferenceResidueAgrees() {
        Assert.assertFalse(ReferenceResidueValidator.validate("BRAF", "V7E", SEQUENCE).isPresent());
    }

    @Test
    public void noMessageWhenSequenceUnknown() {
        Assert.assertFalse(ReferenceResidueValidator.validate("BRAF", "V7E", null).isPresent());
        Assert.assertFalse(ReferenceResidueValidator.validate("BRAF", "V7E", "").isPresent());
    }

    @Test
    public void skipsNonReferenceBearingAlterations() {
        Assert.assertFalse(ReferenceResidueValidator.validate("BRAF", "Amplification", SEQUENCE).isPresent());
        Assert.assertFalse(ReferenceResidueValidator.validate("BRAF", "Fusion", SEQUENCE).isPresent());
        Assert.assertFalse(ReferenceResidueValidator.validate("BRAF", "7del", SEQUENCE).isPresent());
    }

    @Test
    public void reportsSingleReferenceResidueMismatch() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "G7E", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF G7E: reference amino acid at position 7 is V, not G.", message.get());
    }

    @Test
    public void reportsBothRangeBoundaryMismatches() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "V2_V3del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF V2_V3del: reference amino acids at positions 2 and 3 are A and A, not V and V.",
            message.get());
    }

    @Test
    public void reportsOnlyTheWrongRangeBoundary() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "A2_V3del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF A2_V3del: reference amino acid at position 3 is A, not V.", message.get());
    }

    @Test
    public void positionOutOfRangeOutranksResidueMismatch() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "Z99E", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF Z99E: position 99 exceeds the BRAF canonical protein length of 7.", message.get());
    }

    @Test
    public void validatesSpelledOutDeletionContent() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "A2_A3delAL", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF A2_A3delAL: the deleted sequence AL does not match the canonical residues AA at positions 2-3.",
            message.get());
    }

    @Test
    public void validatesSpelledOutDeletionLength() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "A2_A3delAAL", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF A2_A3delAAL: positions 2-3 span 2 residues, but the specified deleted sequence AAL has 3 residue(s).",
            message.get());
    }

    @Test
    public void spelledOutDeletionThatAgreesProducesNoMessage() {
        Assert.assertFalse(ReferenceResidueValidator.validate("BRAF", "A2_A3delAA", SEQUENCE).isPresent());
    }

    @Test
    public void validatesDeletionContentAcrossTheWholeSpanNotJustBoundaries() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "A2_S5delAXLS", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF A2_S5delAXLS: the deleted sequence AXLS does not match the canonical residues AALS at positions 2-5.",
            message.get());
    }

    @Test
    public void insertedResiduesAreNotValidatedAgainstCanonical() {
        Assert.assertFalse(ReferenceResidueValidator.validate("BRAF", "A2_A3insXYZ", SEQUENCE).isPresent());
    }

    @Test
    public void acceptsThreeLetterPointMutation() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "Gly7Glu", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF G7E: reference amino acid at position 7 is V, not G.", message.get());
    }

    @Test
    public void acceptsThreeLetterRangeDeletion() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "Val2_Val3del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF V2_V3del: reference amino acids at positions 2 and 3 are A and A, not V and V.",
            message.get());
    }

    @Test
    public void flagsMalformedMultiResidueRangeBoundary() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "VVV600_W604del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF VVV600_W604del: the reference amino acid at position 600 must be a single amino acid, but is VVV.",
            message.get());
    }

    @Test
    public void flagsWrongCaseThreeLetterRangeBoundary() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "VAL2_VAL3del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF VAL2_VAL3del: the reference amino acid at position 2 must be a single amino acid, but is VAL.",
            message.get());
    }

    @Test
    public void multiResiduePointReferenceIsInvalid() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "AL3L", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF AL3L: not a valid protein change.", message.get());
    }

    @Test
    public void threeResiduePointReferenceIsInvalid() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "ALS3S", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF ALS3S: not a valid protein change.", message.get());
    }

    @Test
    public void reportsReversedRange() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "V7_G6del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF V7_G6del: start position 7 is greater than end position 6.", message.get());
    }

    @Test
    public void residueMismatchOutranksReversedRange() {
        Optional<String> message = ReferenceResidueValidator.validate("BRAF", "A7_A6del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF A7_A6del: reference amino acids at positions 7 and 6 are V and G, not A and A.",
            message.get());
    }
}
