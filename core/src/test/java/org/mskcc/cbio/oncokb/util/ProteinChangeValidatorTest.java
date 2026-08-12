package org.mskcc.cbio.oncokb.util;

import java.util.Collections;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class ProteinChangeValidatorTest {

    // BRAF-like sequence: 1-based positions -> M(1) A(2) A(3) L(4) S(5) G(6) V(7)
    private static final String SEQUENCE = "MAALSGV";

    @Test
    public void noMessageWhenReferenceResidueAgrees() {
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "V7E", SEQUENCE).isPresent());
    }

    @Test
    public void noMessageWhenSequenceUnknown() {
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "V7E", null).isPresent());
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "V7E", "").isPresent());
    }

    @Test
    public void skipsNonReferenceBearingAlterations() {
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "Amplification", SEQUENCE).isPresent());
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "Fusion", SEQUENCE).isPresent());
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "7del", SEQUENCE).isPresent());
    }

    @Test
    public void reportsSingleReferenceResidueMismatch() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "G7E", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF G7E: The reference amino acid at position 7 is V instead of G on the OncoKB canonical transcript.", message.get());
    }

    @Test
    public void reportsBothRangeBoundaryMismatches() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "V2_V3del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF V2_V3del: The reference amino acids at positions 2 and 3 are A and A instead of V and V on the OncoKB canonical transcript.",
            message.get());
    }

    @Test
    public void reportsOnlyTheWrongRangeBoundary() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "A2_V3del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF A2_V3del: The reference amino acid at position 3 is A instead of V on the OncoKB canonical transcript.", message.get());
    }

    @Test
    public void positionOutOfRangeOutranksResidueMismatch() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "Z99E", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF Z99E: position 99 exceeds the BRAF canonical protein length of 7.", message.get());
    }

    @Test
    public void stripsSpelledOutDeletedSequence() {
        ProteinChangeValidator.NormalizationResult result = ProteinChangeValidator.normalize("A2_A3delAA");
        Assert.assertEquals("A2_A3del", result.getProteinChange());
        Assert.assertEquals(
            Collections.singletonList(ProteinChangeNormalization.DELETED_SEQUENCE_DROPPED),
            result.getApplied());
    }

    @Test
    public void stripsSpelledOutDeletedSequenceFromDelins() {
        ProteinChangeValidator.NormalizationResult result = ProteinChangeValidator.normalize("A2_A3delAAinsGG");
        Assert.assertEquals("A2_A3delinsGG", result.getProteinChange());
        Assert.assertEquals(
            Collections.singletonList(ProteinChangeNormalization.DELETED_SEQUENCE_DROPPED),
            result.getApplied());
    }

    @Test
    public void stripsSpelledOutPointDeletion() {
        Assert.assertEquals("V7del", ProteinChangeValidator.normalize("V7delV").getProteinChange());
    }

    @Test
    public void leavesPositionOnlyDeletionUnchanged() {
        ProteinChangeValidator.NormalizationResult result = ProteinChangeValidator.normalize("A2_A3del");
        Assert.assertEquals("A2_A3del", result.getProteinChange());
        Assert.assertFalse(result.isNormalized());
    }

    @Test
    public void leavesStandardDelinsUnchanged() {
        Assert.assertFalse(ProteinChangeValidator.normalize("A2_A3delinsGG").isNormalized());
    }

    @Test
    public void leavesInsertionUnchanged() {
        Assert.assertFalse(ProteinChangeValidator.normalize("A2_A3insXYZ").isNormalized());
    }

    @Test
    public void normalizeHandlesNull() {
        ProteinChangeValidator.NormalizationResult result = ProteinChangeValidator.normalize(null);
        Assert.assertNull(result.getProteinChange());
        Assert.assertFalse(result.isNormalized());
    }

    @Test
    public void insertedResiduesAreNotValidatedAgainstCanonical() {
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "A2_A3insXYZ", SEQUENCE).isPresent());
    }

    @Test
    public void acceptsThreeLetterPointMutation() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "Gly7Glu", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF G7E: The reference amino acid at position 7 is V instead of G on the OncoKB canonical transcript.", message.get());
    }

    @Test
    public void acceptsThreeLetterRangeDeletion() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "Val2_Val3del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF V2_V3del: The reference amino acids at positions 2 and 3 are A and A instead of V and V on the OncoKB canonical transcript.",
            message.get());
    }

    @Test
    public void flagsMalformedMultiResidueRangeBoundary() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "VVV600_W604del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF VVV600_W604del: The reference amino acid at position 600 must be a single amino acid instead of VVV.",
            message.get());
    }

    @Test
    public void flagsWrongCaseThreeLetterRangeBoundary() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "VAL2_VAL3del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF VAL2_VAL3del: The reference amino acid at position 2 must be a single amino acid instead of VAL.",
            message.get());
    }

    @Test
    public void multiResiduePointReferenceIsInvalid() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "AL3L", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF AL3L: Not a valid protein change.", message.get());
    }

    @Test
    public void threeResiduePointReferenceIsInvalid() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "ALS3S", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF ALS3S: Not a valid protein change.", message.get());
    }

    @Test
    public void noMessageWhenFrameshiftReferenceResidueAgrees() {
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "V7fs", SEQUENCE).isPresent());
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "V7Efs*12", SEQUENCE).isPresent());
    }

    @Test
    public void reportsFrameshiftReferenceResidueMismatch() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "G7fs", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF G7fs: The reference amino acid at position 7 is V instead of G on the OncoKB canonical transcript.", message.get());
    }

    @Test
    public void reportsFrameshiftReferenceResidueMismatchWithExtension() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "G7Efs*12", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF G7Efs*12: The reference amino acid at position 7 is V instead of G on the OncoKB canonical transcript.", message.get());

        Assert.assertTrue(ProteinChangeValidator.validate("BRAF", "G7Efs*", SEQUENCE).isPresent());
        Assert.assertTrue(ProteinChangeValidator.validate("BRAF", "G7Efs*?", SEQUENCE).isPresent());
    }

    @Test
    public void acceptsThreeLetterFrameshift() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "Gly7GlufsTer12", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF G7Efs*12: The reference amino acid at position 7 is V instead of G on the OncoKB canonical transcript.", message.get());
    }

    @Test
    public void reportsFrameshiftPositionOutOfRange() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "G99fs", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF G99fs: position 99 exceeds the BRAF canonical protein length of 7.", message.get());
    }

    @Test
    public void skipsFrameshiftWithoutASingleReferenceResidue() {
        // None of these name a single reference residue to compare: the first states none, the second
        // states more than one position can account for, and the stop codon the third is anchored on is
        // not part of the canonical sequence.
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "7fs*4", SEQUENCE).isPresent());
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "GVX7fs", SEQUENCE).isPresent());
        Assert.assertFalse(ProteinChangeValidator.validate("BRAF", "*8Ffs*5", SEQUENCE).isPresent());
    }

    @Test
    public void reportsReversedRange() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "V7_G6del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals("BRAF V7_G6del: Start position 7 is greater than end position 6.", message.get());
    }

    @Test
    public void residueMismatchOutranksReversedRange() {
        Optional<String> message = ProteinChangeValidator.validate("BRAF", "A7_A6del", SEQUENCE);
        Assert.assertTrue(message.isPresent());
        Assert.assertEquals(
            "BRAF A7_A6del: The reference amino acids at positions 7 and 6 are V and G instead of A and A on the OncoKB canonical transcript.",
            message.get());
    }
}
