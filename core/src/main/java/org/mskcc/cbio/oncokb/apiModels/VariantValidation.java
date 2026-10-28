package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.VariantValidationStatus;
import org.mskcc.cbio.oncokb.model.VariantAnnotationMessageType;

/**
 * Outcome of validating a queried variant: a protein change against the OncoKB canonical protein
 * sequence, or the separator of a fusion name. Present on a {@link SomaticVariantAnnotation} only when
 * there is something to report; a query that passes leaves this null.
 */
public class VariantValidation {
    private VariantValidationStatus status;
    private VariantAnnotationMessageType messageType;
    private String message;
    private String normalizedProteinChange;

    public VariantValidation() {
    }

    public VariantValidation(VariantValidationStatus status, VariantAnnotationMessageType messageType, String message) {
        this.status = status;
        this.messageType = messageType;
        this.message = message;
    }

    public VariantValidationStatus getStatus() {
        return status;
    }

    public void setStatus(VariantValidationStatus status) {
        this.status = status;
    }

    public VariantAnnotationMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(VariantAnnotationMessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNormalizedProteinChange() {
        return normalizedProteinChange;
    }

    public void setNormalizedProteinChange(String normalizedProteinChange) {
        this.normalizedProteinChange = normalizedProteinChange;
    }
}
