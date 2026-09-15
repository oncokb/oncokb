package org.mskcc.cbio.oncokb.util;

import junit.framework.TestCase;
import org.mskcc.cbio.oncokb.apiModels.ValidationError;
import org.mskcc.cbio.oncokb.model.ValidationErrorType;

import java.util.List;

public class FusionValidationUtilsTest extends TestCase {

    public void testAmbiguousFusionNameIsReported() {
        List<ValidationError> errors = FusionValidationUtils.getFusionValidationErrors("A-B-C Fusion");
        assertEquals(1, errors.size());
        assertEquals(ValidationErrorType.AMBIGUOUS_FUSION_SEPARATOR, errors.get(0).getType());
        assertTrue("The message should name what was queried",
            errors.get(0).getMessage().startsWith("A-B-C Fusion "));
        assertTrue("The message should point at the HGVS separator",
            errors.get(0).getMessage().contains("::"));
    }

    public void testNothingIsReportedForNamesThatCanBeInterpreted() {
        // Already HGVS
        assertTrue(FusionValidationUtils.getFusionValidationErrors("BCR::ABL1 Fusion").isEmpty());
        assertTrue(FusionValidationUtils.getFusionValidationErrors("H1-4::H2BC5 Fusion").isEmpty());
        // A single hyphen is unambiguous, and was rewritten before annotation
        assertTrue(FusionValidationUtils.getFusionValidationErrors("BCR-ABL1 Fusion").isEmpty());
        // More than one hyphen, but only one split into two known genes
        assertTrue(FusionValidationUtils.getFusionValidationErrors("H1-4-H2BC5 Fusion").isEmpty());
        // Not a fusion name at all
        assertTrue(FusionValidationUtils.getFusionValidationErrors("V600E").isEmpty());
        assertTrue(FusionValidationUtils.getFusionValidationErrors("").isEmpty());
        assertTrue(FusionValidationUtils.getFusionValidationErrors(null).isEmpty());
    }
}
