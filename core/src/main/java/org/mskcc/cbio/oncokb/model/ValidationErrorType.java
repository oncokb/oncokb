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
 * <p>Every value here describes a variant that cannot exist, so a query carrying any of them is left
 * unannotated at the variant level. That is an internal decision, not part of the API: a client sees
 * its effect in the response fields rather than in the error itself. If a value is ever added that
 * should be reported alongside a full annotation, the caller in
 * {@code IndicatorUtils.processQuerySomatic} needs to branch on the type rather than on any error
 * being present.
 *
 * <p>Something that could not be checked at all (no canonical sequence available, transcript
 * service down) is not an error here: it is annotated as usual and nothing is reported.
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

    // The query names a fusion with more than one hyphen, e.g. H1-4-H2BC5 Fusion. Which hyphen
    // separates the partners could be worked out by trying every split against the gene table, but
    // OncoKB does not: the name is asked for in the form that already says it, H1-4::H2BC5.
    AMBIGUOUS_FUSION_SEPARATOR;
}
