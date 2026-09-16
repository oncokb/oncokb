package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.ValidationErrorType;

/**
 * These are reported together as the {@code errors} list on an annotation response.
 * See {@code docs/validation-errors.md}.
 */
public class ValidationError implements java.io.Serializable {
    @ApiModelProperty(value = "Type of validation error")
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
