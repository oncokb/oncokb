package org.mskcc.cbio.oncokb.model;

/**
 * The reason behind a {@link VariantValidationStatus} when a queried variant is validated, e.g. a
 * protein change against the OncoKB canonical protein sequence or the separator of a fusion name. {@code message} is null for the transcript service and
 * missing sequence types; the frontend supplies that wording.
 */
public enum VariantAnnotationMessageType {
    // The query is not a valid protein change against the canonical sequence (reference residue
    // mismatch, out-of-range position, malformed form, etc.); the detail is carried in the message field.
    INVALID_PROTEIN_CHANGE,
    // The query spelled out the deleted residues (e.g. A237_G238delAG), which HGVS does not use;
    // the deleted sequence was dropped and annotation ran on the normalized form (A237_G238del).
    NORMALIZED_DELETED_SEQUENCE,
    // The transcript service returned no canonical protein sequence for this gene.
    NO_PROTEIN_SEQUENCE,
    // The transcript service is disabled, so the required validation step could not run.
    TRANSCRIPT_SERVICE_DISABLED,
    // The transcript service failed (likely intermittent); the query should be retried.
    TRANSCRIPT_SERVICE_UNAVAILABLE,
    // The query named a fusion with more than one hyphen that no single split turns into two known genes.
    // The detail is carried in the message field.
    AMBIGUOUS_FUSION_SEPARATOR
}
