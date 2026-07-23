package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.ProteinChangeValidationStatus;
import org.mskcc.cbio.oncokb.model.VariantAnnotationMessageType;

/**
 * Outcome of validating a queried protein change against the OncoKB canonical protein sequence.
 * Present on a {@link SomaticVariantAnnotation} only when there is something to report; a query
 * that agrees with the canonical sequence leaves this null.
 *
 * <p>{@link #status} is the headline severity, {@link #messageType} the machine-readable reason,
 * and {@link #message} the human-readable detail. {@link #normalizedProteinChange} is set whenever
 * the queried string was rewritten before annotation, independent of the status.
 */
public class ProteinChangeValidation {
    private ProteinChangeValidationStatus status;
    private VariantAnnotationMessageType messageType;
    private String message;
    private String normalizedProteinChange;

    public ProteinChangeValidation() {
    }

    public ProteinChangeValidation(ProteinChangeValidationStatus status, VariantAnnotationMessageType messageType, String message) {
        this.status = status;
        this.messageType = messageType;
        this.message = message;
    }

    public ProteinChangeValidationStatus getStatus() {
        return status;
    }

    public void setStatus(ProteinChangeValidationStatus status) {
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
