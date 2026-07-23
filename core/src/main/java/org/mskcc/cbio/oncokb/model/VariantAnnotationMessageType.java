package org.mskcc.cbio.oncokb.model;

/**
 * Categorizes why a protein change could not be confirmed against the OncoKB canonical protein
 * sequence. The message text itself is left to the frontend; only the type is returned.
 */
public enum VariantAnnotationMessageType {
    // The query is not a valid protein change against the canonical sequence (reference residue
    // mismatch, out-of-range position, malformed form, etc.); the detail is carried in the message field.
    INVALID_PROTEIN_CHANGE,
    // The transcript service returned no canonical protein sequence for this gene.
    NO_PROTEIN_SEQUENCE,
    // The transcript service is disabled, so the required validation step could not run.
    TRANSCRIPT_SERVICE_DISABLED,
    // The transcript service failed (likely intermittent); the query should be retried.
    TRANSCRIPT_SERVICE_UNAVAILABLE
}
