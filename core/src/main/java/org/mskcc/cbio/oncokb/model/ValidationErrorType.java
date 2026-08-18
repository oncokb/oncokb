package org.mskcc.cbio.oncokb.model;

/**
 * What exactly is wrong with a query, as reported in the {@code errors} list of an annotation
 * response. This is one flat vocabulary rather than one enum per kind of check: the values below
 * all come from validating the alteration a query names, and gene-level or other checks append
 * their own values here instead of introducing a parallel vocabulary.
 *
 * <p>This is the machine-readable half of {@link org.mskcc.cbio.oncokb.apiModels.ValidationError};
 * the wording lives alongside it. See {@code docs/validation-errors.md} for what each value means
 * and how to present it.
 *
 * <p>{@link #blocksAnnotation()} says whether the problem is bad enough that the variant is left
 * unannotated. It is an internal decision, not part of the API: it is not serialized, and a client
 * sees its effect in the response fields rather than in the error itself.
 *
 * <p>Something that could not be checked at all (no canonical sequence available, transcript
 * service down) is not an error here: it is annotated as usual and nothing is reported.
 */
public enum ValidationErrorType {
    // The reference allele the query names is not what the OncoKB canonical sequence has at that
    // position, so the query describes a variant that cannot exist. e.g. BRAF A600E, where the
    // canonical residue at position 600 is V.
    REFERENCE_ALLELE_MISMATCH(true),

    // The position the query names is past the end of the OncoKB canonical sequence.
    POSITION_OUT_OF_RANGE(true),

    // The query names a range whose start position is after its end position.
    REVERSED_POSITION_RANGE(true),

    // The query is not a well-formed alteration of its kind, independent of the canonical sequence.
    // e.g. a single position carrying more than one reference residue (AL3L, VVV600_W604del).
    MALFORMED_ALTERATION(true);

    private final boolean blocksAnnotation;

    ValidationErrorType(boolean blocksAnnotation) {
        this.blocksAnnotation = blocksAnnotation;
    }

    /**
     * Whether an error of this type describes a variant that cannot exist, so that the variant-level
     * annotation is skipped and only what OncoKB knows about the gene is returned. An error that does
     * not block is reported alongside a full annotation.
     */
    public boolean blocksAnnotation() {
        return blocksAnnotation;
    }
}
