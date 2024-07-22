package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.model.VariantConsequence;

import static org.mskcc.cbio.oncokb.Constants.FRAMESHIFT_VARIANT;
import static org.mskcc.cbio.oncokb.Constants.PROTEIN_ALTERING_VARIANT;
import static org.mskcc.cbio.oncokb.util.VariantConsequenceUtils.consequenceResolver;

public class VariantConsequenceUtilsTest extends TestCase {

    public void testConsequenceResolver() {
        // Test consequence without variant class
        assertConsequenceResolver(consequenceResolver(null), "");
        assertConsequenceResolver(consequenceResolver(""), "");
        assertConsequenceResolver(consequenceResolver("splice_donor_variant"), "splice_donor_variant");
        assertConsequenceResolver(consequenceResolver(PROTEIN_ALTERING_VARIANT), "");
        assertConsequenceResolver(consequenceResolver("splice_donor_variant," + PROTEIN_ALTERING_VARIANT), "splice_donor_variant");
        assertConsequenceResolver(consequenceResolver("splice_acceptor_variant," + PROTEIN_ALTERING_VARIANT), "splice_acceptor_variant");

        // Test consequence with variant class
        assertConsequenceResolver(consequenceResolver(null, ""), "");
        assertConsequenceResolver(consequenceResolver("", ""), "");
        assertConsequenceResolver(consequenceResolver(null, "Splice_Site"), "splice_region_variant");
        assertConsequenceResolver(consequenceResolver("", "Splice_Site"), "splice_region_variant");
        assertConsequenceResolver(consequenceResolver("splice_donor_variant", "Splice_Site"), "splice_donor_variant");
        assertConsequenceResolver(consequenceResolver(PROTEIN_ALTERING_VARIANT, "Splice_Site"), "splice_region_variant");
        assertConsequenceResolver(consequenceResolver("splice_donor_variant," + PROTEIN_ALTERING_VARIANT, "Splice_Site"), "splice_donor_variant");
        assertConsequenceResolver(consequenceResolver("splice_acceptor_variant," + ",splice_donor_variant" + PROTEIN_ALTERING_VARIANT, "Splice_Site"), "splice_acceptor_variant");

        // Test real cases
        assertConsequenceResolver(consequenceResolver("coding_sequence_variant,5_prime_UTR_variant", "In_Frame_Del"), "inframe_deletion");
        assertConsequenceResolver(consequenceResolver("coding_sequence_variant,5_prime_UTR_variant", "Frame_Shift_Del"), FRAMESHIFT_VARIANT);
        assertConsequenceResolver(consequenceResolver("", "In_Frame_Del"), "inframe_deletion");
        assertConsequenceResolver(consequenceResolver(PROTEIN_ALTERING_VARIANT, "In_Frame_Del"), "inframe_deletion");

    }

    private void assertConsequenceResolver(VariantConsequence consequence, String expectedTerm) {
        String consequenceTerm = consequence == null ? "" : consequence.getTerm();
        assertEquals(expectedTerm, consequenceTerm);
    }


    public void testTrimConsequenceTerms() {
        // we do not have a mapping for test. We should default to the one that we do. In this case, intron_variant
        String consequenceTerms = "intron_variant";
        String variantConsequence = VariantConsequenceUtils.pickConsequenceTerm(consequenceTerms);
        assertEquals("intron_variant", variantConsequence);

        consequenceTerms = "splice_region_variant,intron_variant";
        variantConsequence = VariantConsequenceUtils.pickConsequenceTerm(consequenceTerms);
        assertEquals("splice_region_variant", variantConsequence);

        consequenceTerms = "intron_variant , intron_variant";
        variantConsequence = VariantConsequenceUtils.pickConsequenceTerm(consequenceTerms);
        assertEquals("intron_variant", variantConsequence);

        consequenceTerms = "intron_variant , intron_variant,splice_region_variant";
        variantConsequence = VariantConsequenceUtils.pickConsequenceTerm(consequenceTerms);
        assertEquals("intron_variant", variantConsequence);

        consequenceTerms = " , intron_variant";
        variantConsequence = VariantConsequenceUtils.pickConsequenceTerm(consequenceTerms);
        assertEquals("intron_variant", variantConsequence);

        consequenceTerms = ", intron_variant";
        variantConsequence = VariantConsequenceUtils.pickConsequenceTerm(consequenceTerms);
        assertEquals("intron_variant", variantConsequence);

        consequenceTerms = null;
        variantConsequence = VariantConsequenceUtils.pickConsequenceTerm(consequenceTerms);
        assertEquals("", variantConsequence);
    }
}
