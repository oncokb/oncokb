package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationPositionBoundary;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mskcc.cbio.oncokb.Constants.*;

/**
 * Created by Hongxin on 12/23/16.
 */

@RunWith(Parameterized.class)
public class AnnotateAlterationTest {
    private String proteinChange;
    private String proteinStart;
    private String proteinEnd;
    private String expectedProteinStart;
    private String expectedProteinEnd;
    private String expectedRefResidues;
    private String expectedVariantResidues;
    private String expectedConsequence;

    public AnnotateAlterationTest(String proteinChange, String proteinStart, String proteinEnd, String expectedProteinStart, String expectedProteinEnd, String expectedRefResidues, String expectedVariantResidues, String expectedConsequence) {
        this.proteinChange = proteinChange;
        this.proteinStart = proteinStart;
        this.proteinEnd = proteinEnd;
        this.expectedProteinStart = expectedProteinStart;
        this.expectedProteinEnd = expectedProteinEnd;
        this.expectedRefResidues = expectedRefResidues;
        this.expectedVariantResidues = expectedVariantResidues;
        this.expectedConsequence = expectedConsequence;
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() {
        return Arrays.asList(
            new String[][]{
                // any
                {"449_514mut", "449", "514", "449", "514", null, null, "any"},
                {"1003mut", "1003", "1003", "1003", "1003", null, null, "any"},
                {"Y1003mut", "1003", "1003", "1003", "1003", "Y", null, "any"},

                // Missense variant
                {"V600E", "600", "600", "600", "600", "V", "E", MISSENSE_VARIANT},
                {"F53_Q53delinsL", "53", "53", "53", "53", null, null, MISSENSE_VARIANT},
                {"D842_I843delinsIM", "842", "843", "842", "843", null, null, MISSENSE_VARIANT},
                {"IK744KI", "744", "745", "744", "745", "IK", "KI", MISSENSE_VARIANT},

                // feature_truncating variant
                {"D286_L292trunc", "286", "292", "286", "292", null, null, "feature_truncation"},
                {"Truncating Mutations", Integer.toString(AlterationPositionBoundary.START.getValue()), Integer.toString(AlterationPositionBoundary.END.getValue()), Integer.toString(AlterationPositionBoundary.START.getValue()), Integer.toString(AlterationPositionBoundary.END.getValue()), null, null, "feature_truncation"},

                // frameshift event
                {"N457Mfs*22", "457", "457", "457", "457", "N", null, FRAMESHIFT_VARIANT},
                {"*1069Ffs*5", "1069", "1069", "1069", "1069", "*", null, FRAMESHIFT_VARIANT},
                {"I327Rfs*", "327", "327", "327", "327", "I", null, FRAMESHIFT_VARIANT},

                // inframe event
                {"T417_D419delinsI", "417", "419", "417", "419", null, null, IN_FRAME_DELETION},
                {"V600delinsYM", "600", "600", "600", "600", "V", null, IN_FRAME_INSERTION},
                {"I744_K745delinsKIPVAI", "744", "745", "744", "745", null, null, IN_FRAME_INSERTION},
                {"762_823ins", "762", "823", "762", "823", null, null, IN_FRAME_INSERTION},
                {"V561_I562insER", "561", "562", "561", "562", null, null, IN_FRAME_INSERTION},
                {"R78_G79ins23", "78", "79", "78", "79", null, null, IN_FRAME_INSERTION},
                {"IK744KIPVAI", "744", "745", "744", "745", "IK", "KIPVAI", IN_FRAME_INSERTION},
                {"IKG744KIPVAI", "744", "746", "744", "746", "IKG", "KIPVAI", IN_FRAME_INSERTION},

                {"E102_I103del", "102", "103", "102", "103", null, null, IN_FRAME_DELETION},
                {"IK744K", "744", "745", "744", "745", "IK", "K", IN_FRAME_DELETION},
                {"V559_E561del", "559", "561", "559", "561", null, null, IN_FRAME_DELETION},
                {"G12delG", "12", "12", "12", "12", "G", null, IN_FRAME_DELETION},
                {"G106_R108delGNR", "106", "108", "106", "108", null, null, IN_FRAME_DELETION},

                {"P68_C77dup", "68", "77", "68", "77", null, null, IN_FRAME_INSERTION},

                // start_lost,
                {"M1I", "1", "1", "1", "1", "M", "I", "start_lost"},
                {"M1?", "1", "1", "1", "1", "M", "?", "start_lost"},

                // NA
                {"BCR-ABL1 Fusion", Integer.toString(AlterationPositionBoundary.START.getValue()), Integer.toString(AlterationPositionBoundary.END.getValue()), Integer.toString(AlterationPositionBoundary.START.getValue()), Integer.toString(AlterationPositionBoundary.END.getValue()), null, null, "NA"},
                {"Oncogenic Mutations", Integer.toString(AlterationPositionBoundary.START.getValue()), Integer.toString(AlterationPositionBoundary.END.getValue()), Integer.toString(AlterationPositionBoundary.START.getValue()), Integer.toString(AlterationPositionBoundary.END.getValue()), null, null, "NA"},
                {"V600", "600", "600", "600", "600", "V", null, "NA"},

                // Splice
                {"X405_splice", "405", "405", "405", "405", null, null, "splice_region_variant"},
                {"405_splice", "405", "405", "405", "405", null, null, "splice_region_variant"},
                {"405splice", "405", "405", "405", "405", null, null, "splice_region_variant"},
                {"X405_A500splice", "405", "500", "405", "500", null, null, "splice_region_variant"},
                {"X405_A500_splice", "405", "500", "405", "500", null, null, "splice_region_variant"},
                {"405_500_splice", "405", "500", "405", "500", null, null, "splice_region_variant"},
                {"405_500splice", "405", "500", "405", "500", null, null, "splice_region_variant"},

                // stop retained variant
                {"*1136=", "1136", "1136", "1136", "1136", "*", "*", "stop_retained_variant"},

                // silent mutation
                {"L838=", "838", "838", "838", "838", "L", "L", "synonymous_variant"},

                // Stop gained
                {"R2109*", "2109", "2109", "2109", "2109", "R", "*", "stop_gained"},

                // Stop lost, tests are from https://varnomen.hgvs.org/recommendations/protein/variant/extension/
                {"*959Qext*14", "959", "959", "959", "959", "*", null, "stop_lost"},
                {"*110Gext*17", "110", "110", "110", "110", "*", null, "stop_lost"},
                {"*315TextALGT*", "315", "315", "315", "315", "*", null, "stop_lost"},
                {"*327Aext*?", "327", "327", "327", "327", "*", null, "stop_lost"},
                {"*151*", "151", "151", "151", "151", "*", "*", "stop_lost"},
                {"327Aext*?", "327", "327", "327", "327", null, null, "stop_lost"},

                // Synonymous Variant
                {"G500G", "500", "500", "500", "500", "G", "G", "synonymous_variant"},

                // Overwrite protein start, protein end
                {"V105_E109delinsG", "109", "109", "105", "109", null, null, IN_FRAME_DELETION},
                {"P191del", "191", "192", "191", "191", "P", null, IN_FRAME_DELETION},
                // Made up case
                {"Oncogenic Mutations", "10", "50", "10", "50", null, null, "NA"},
            });
    }

    private Boolean checkNull(String expecteded, String result) {
        if (expecteded == null && result == null) {
            return true;
        } else if (expecteded != null && result != null) {
            return expecteded.equals(result);
        }
        return false;
    }

    @Test
    public void testAnnotateAlteration() throws Exception {
        // This test mainly test when protein change/start/end are specified
        // Please see AnnotateAlterationParameterizedTest for testing combinations
        // Particularly test expectedConsequence
        Alteration alt = new Alteration();
        alt.setProteinStart(Integer.parseInt(proteinStart));
        alt.setProteinEnd(Integer.parseInt(proteinEnd));
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

        assertTrue(proteinChange + ": Protein start should be " + expectedProteinStart + ", but got: " + _proteinStart, checkNull(expectedProteinStart, _proteinStart));
        assertTrue(proteinChange + ": Protein end should be " + expectedProteinEnd + ", but got: " + _proteinEnd, checkNull(expectedProteinEnd, _proteinEnd));
        assertTrue(proteinChange + ": Ref residues should be " + expectedRefResidues + ", but got: " + alt.getRefResidues(), checkNull(expectedRefResidues, alt.getRefResidues()));
        assertTrue(proteinChange + ": Ref residues should be " + expectedVariantResidues + ", but got: " + alt.getVariantResidues(), checkNull(expectedVariantResidues, alt.getVariantResidues()));
        assertTrue(proteinChange + ": Consequence should be " + expectedConsequence + ", but got: " + _consequence, checkNull(expectedConsequence, _consequence));
    }

}
