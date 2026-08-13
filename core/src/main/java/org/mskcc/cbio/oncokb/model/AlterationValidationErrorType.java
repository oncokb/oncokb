package org.mskcc.cbio.oncokb.model;

/**
 * What exactly is wrong with an alteration that was rejected as impossible against the OncoKB
 * canonical sequence. The values are deliberately phrased for any alteration a query can carry
 * - a protein change today, an HGVS expression tomorrow - rather than for protein changes alone,
 * so a new query type can reuse them instead of introducing a parallel vocabulary.
 *
 * <p>This is the machine-readable half of {@link org.mskcc.cbio.oncokb.apiModels.AlterationValidationError};
 * the wording lives alongside it. See {@code docs/alteration-validation-errors.md} for what each
 * value means and how to present it.
 *
 * <p>An alteration that could not be checked at all (no canonical sequence available, transcript
 * service down) is not an error here: it is annotated as usual and nothing is reported.
 */
public enum AlterationValidationErrorType {
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
    MALFORMED_ALTERATION
}
