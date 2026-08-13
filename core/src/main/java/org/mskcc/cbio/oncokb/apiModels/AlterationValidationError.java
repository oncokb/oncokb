package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.AlterationValidationErrorType;

/**
 * Why a queried alteration was rejected as impossible against the OncoKB canonical sequence, so that
 * the query was annotated at the gene level only. {@link #type} is the reason to branch on and
 * {@link #message} says the same thing in words, for a client with nowhere better to get the wording;
 * the two always agree, so a client may use either or both.
 *
 * <p>Present only on a rejected query - an annotated variant leaves it null. See
 * {@code docs/alteration-validation-errors.md}.
 */
public class AlterationValidationError {
    @ApiModelProperty(value = "The reason the alteration was rejected.", allowableValues = "REFERENCE_ALLELE_MISMATCH, POSITION_OUT_OF_RANGE, REVERSED_POSITION_RANGE, MALFORMED_ALTERATION")
    private AlterationValidationErrorType type;

    @ApiModelProperty(value = "The same reason in words, naming the queried alteration and what the OncoKB canonical sequence has instead.", example = "BRAF A600E: The reference amino acid at position 600 is V instead of A on the OncoKB canonical transcript.")
    private String message;

    public AlterationValidationError() {
    }

    public AlterationValidationError(AlterationValidationErrorType type, String message) {
        this.type = type;
        this.message = message;
    }

    public AlterationValidationErrorType getType() {
        return type;
    }

    public void setType(AlterationValidationErrorType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
