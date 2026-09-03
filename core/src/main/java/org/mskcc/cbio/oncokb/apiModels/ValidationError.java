package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.ValidationErrorType;

/**
 * One thing OncoKB found wrong with a query. {@link #type} is the reason to branch on and
 * {@link #message} says the same thing in words, for a client with nowhere better to get the
 * wording; the two always agree, so a client may use either or both.
 *
 * <p>These are reported together as the {@code errors} list on an annotation response, which is
 * empty whenever there is nothing wrong. The vocabulary is not tied to one kind of check - it
 * covers the alteration a query names today and is meant to grow to gene-level and other checks.
 * See {@code docs/validation-errors.md}.
 */
public class ValidationError {
    @ApiModelProperty(value = "What is wrong with the query.", allowableValues = "REFERENCE_ALLELE_MISMATCH, POSITION_OUT_OF_RANGE, REVERSED_POSITION_RANGE, MALFORMED_ALTERATION, AMBIGUOUS_FUSION_SEPARATOR")
    private ValidationErrorType type;

    @ApiModelProperty(value = "The same reason in words, naming what was queried and what OncoKB has instead.", example = "BRAF A600E: The reference amino acid at position 600 is V instead of A on the OncoKB canonical transcript.")
    private String message;

    public ValidationError() {
    }

    public ValidationError(ValidationErrorType type, String message) {
        this.type = type;
        this.message = message;
    }

    public ValidationErrorType getType() {
        return type;
    }

    public void setType(ValidationErrorType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
