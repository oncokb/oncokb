package org.mskcc.cbio.oncokb.model;

/**
 * See {@code docs/validation-errors.md} for detailed information
 */
public enum ValidationErrorType {
    // The reference allele the query names is not what the OncoKB canonical sequence has at that
    // position, so the query describes a variant that cannot exist. e.g. BRAF A600E, where the
    // canonical residue at position 600 is V.
    REFERENCE_ALLELE_MISMATCH,

    // The position the query names is past the end of the OncoKB canonical sequence.
    POSITION_OUT_OF_RANGE,

    // The query names a range whose start position is after its end position.
    REVERSED_POSITION_RANGE,

    // The query is not a well-formed alteration of its kind, independent of the canonical sequence.
    // e.g. a single position carrying more than one reference residue (AL3L, VVV600_W604del).
    MALFORMED_ALTERATION,

    // The query names a fusion with more than one hyphen, e.g. H1-4-H2BC5 Fusion.
    AMBIGUOUS_FUSION_SEPARATOR;
}
