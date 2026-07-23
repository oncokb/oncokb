package org.mskcc.cbio.oncokb.model;

/**
 * Severity of the outcome of validating a queried protein change against the OncoKB canonical
 * protein sequence. This is distinct from {@link VariantAnnotationMessageType}, which names the
 * specific reason; the status only tells the frontend how to treat the result.
 */
public enum ProteinChangeValidationStatus {
    // The query was non-standard but unambiguously rewritten (e.g. a spelled-out deletion sequence
    // was dropped) and annotation ran on the normalized form. Advisory only; annotation is valid.
    NORMALIZED,
    // The query disagrees with the canonical sequence (reference residue mismatch, out-of-range
    // position, reversed range, malformed form). The detail is carried in the message.
    INVALID,
    // The check could not be performed (transcript service disabled/unavailable, or the gene has no
    // canonical protein sequence).
    UNCHECKED
}
