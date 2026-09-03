package org.mskcc.cbio.oncokb.model;

/**
 * The specific reason behind a {@link ProteinChangeValidationStatus} when a queried protein change
 * is validated against the OncoKB canonical protein sequence. The message text itself is left to
 * the frontend for the non-detail types; only the type is returned.
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
    // The query named a fusion with more than one hyphen; rather than guess the split, OncoKB asks
    // for the HGVS separator. The detail is carried in the message field.
    AMBIGUOUS_FUSION_SEPARATOR
}
