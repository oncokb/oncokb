package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.Alteration;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

/**
 * Created by Hongxin on 12/23/16.
 */

@RunWith(Parameterized.class)
public class AlterationUtilsTest {
    private String proteinChange;
    private String proteinStart;
    private String proteinEnd;
    private String refResidues;
    private String variantResidues;
    private String consequence;

    public AlterationUtilsTest(String proteinChange, String proteinStart, String proteinEnd, String refResidues, String variantResidues, String consequence) {
        this.proteinChange = proteinChange;
        this.proteinStart = proteinStart;
        this.proteinEnd = proteinEnd;
        this.refResidues = refResidues;
        this.variantResidues = variantResidues;
        this.consequence = consequence;
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() {
        return Arrays.asList(
            new String[][]{
                // any
                {"449_514mut", "449", "514", null, null, "any"},

                // Missense variant
                {"V600E", "600", "600", "V", "E", "missense_variant"},
                {"F53_Q53delinsL", "53", "53", null, null, "missense_variant"},
                {"D842_I843delinsIM", "842", "843", null, null, "missense_variant"},

                // feature_truncating variant
                {"D286_L292trunc", "286", "292", null, null, "feature_truncation"},
                {"Truncating Mutations", "-1", "100000", null, null, "feature_truncation"},

                // frameshift event
                {"N457Mfs*22", "457", "457", "N", null, "frameshift_variant"},

                // inframe event
                {"T417_D419delinsI", "417", "419", null, null, "inframe_deletion"},
                {"E102_I103del", "102", "103", null, null, "inframe_deletion"},
                {"V600delinsYM", "600", "600", null, null, "inframe_insertion"},
                {"I744_K745delinsKIPVAI", "744", "745", null, null, "inframe_insertion"},
                {"762_823ins", "762", "823", null, null, "inframe_insertion"},

                // initiator_codon_variant,
                {"M1I", "1", "1", "M", "I", "initiator_codon_variant"},

                // NA
                {"BCR-ABL1 Fusion", "-1", "100000", null, null, "NA"},

                // Splice
                {"X405_splice", "405", "405", null, null, "splice_region_variant"},
                {"405_splice", "405", "405", null, null, "splice_region_variant"},
                {"405splice", "405", "405", null, null, "splice_region_variant"},
                {"X405_A500splice", "405", "500", null, null, "splice_region_variant"},
                {"X405_A500_splice", "405", "500", null, null, "splice_region_variant"},
                {"405_500_splice", "405", "500", null, null, "splice_region_variant"},
                {"405_500splice", "405", "500", null, null, "splice_region_variant"},

                // Stop gained
                {"R2109*", "2109", "2109", "R", "*", "stop_gained"},

                // Synonymous Variant
                {"G500G", "500", "500", "G", "G", "synonymous_variant"},

            });
    }

    private Boolean checkNull(String expected, String result) {
        if (expected == null && result == null) {
            return true;
        } else if (expected != null && result != null) {
            return expected.equals(result);
        }
        return false;
    }

    @Test
    public void testAnnotateAlteration() throws Exception {
        // Particularly test consequence
        Alteration alt = new Alteration();
        AlterationUtils.annotateAlteration(alt, proteinChange);

        String _proteinStart = null;
        String _proteinEnd = null;
        String _consequence = null;

        if (alt.getProteinStart() != null) {
            _proteinStart = alt.getProteinStart().toString();
        }
        if (alt.getProteinStart() != null) {
            _proteinEnd = alt.getProteinEnd().toString();
        }
        if (alt.getConsequence() != null) {
            _consequence = alt.getConsequence().getTerm();
        }

        assertTrue(proteinChange + ": Protein start should be " + proteinStart + ", but got: " + _proteinStart, checkNull(proteinStart, _proteinStart));
        assertTrue(proteinChange + ": Protein end should be " + proteinEnd + ", but got: " + _proteinEnd, checkNull(proteinEnd, _proteinEnd));
        assertTrue(proteinChange + ": Ref residues should be " + refResidues + ", but got: " + alt.getRefResidues(), checkNull(refResidues, alt.getRefResidues()));
        assertTrue(proteinChange + ": Ref residues should be " + variantResidues + ", but got: " + alt.getVariantResidues(), checkNull(variantResidues, alt.getVariantResidues()));
        assertTrue(proteinChange + ": Consequence should be " + consequence + ", but got: " + _consequence, checkNull(consequence, _consequence));
    }

}
