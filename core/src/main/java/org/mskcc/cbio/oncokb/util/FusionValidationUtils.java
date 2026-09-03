package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.ValidationError;
import org.mskcc.cbio.oncokb.model.ValidationErrorType;
import org.mskcc.cbio.oncokb.util.FusionUtils.FusionNameNormalization;

import java.util.Collections;
import java.util.List;

/**
 * Reports a queried fusion name that {@link FusionUtils#normalizeSeparator(String)} declined to
 * interpret. A name written with a single legacy hyphen is rewritten onto the HGVS separator on the
 * way in, in {@code Query.enrich()}, and annotated in full, so nothing is reported for it; only a
 * name with more than one hyphen, which is not annotated, is.
 *
 * <p>The wording lives here rather than at either call site because the public annotation endpoints
 * report it through the {@code errors} list and the private {@code /utils/variantAnnotation} through
 * its own validation object, and the two must say the same thing.
 */
public final class FusionValidationUtils {

    private FusionValidationUtils() {}

    /**
     * The reasons the queried fusion name cannot be interpreted, empty when there is nothing to
     * report. Returned as a list so it composes with the other checks an annotated query collects;
     * this one reports at most one.
     */
    public static List<ValidationError> getFusionValidationErrors(String alteration) {
        FusionNameNormalization normalization = FusionUtils.normalizeSeparator(alteration);
        if (!normalization.isAmbiguous()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new ValidationError(
            ValidationErrorType.AMBIGUOUS_FUSION_SEPARATOR, describeAmbiguous(alteration)));
    }

    /** Why a multi-hyphen fusion name was not annotated, and what to send instead. */
    public static String describeAmbiguous(String alteration) {
        return StringUtils.trimToEmpty(alteration)
            + " does not follow HGNC fusion nomenclature. Use " + FusionUtils.FUSION_SEPARATOR
            + " to separate fusion gene partners, especially since HGNC gene symbols may contain"
            + " hyphens.";
    }
}
