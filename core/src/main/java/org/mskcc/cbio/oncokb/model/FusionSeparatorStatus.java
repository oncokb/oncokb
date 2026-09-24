package org.mskcc.cbio.oncokb.model;

public enum FusionSeparatorStatus {
    // The name carries no fusion separator to interpret, or the gene part is itself a curated HUGO
    // symbol that happens to contain a hyphen (H1-4 Fusion). Left as-is.
    NOT_APPLICABLE,

    // The name already uses the HGVS separator (BCR::ABL1 Fusion). Left as-is.
    HGVS,

    // The name used a single legacy hyphen and was rewritten to the HGVS separator
    // (BCR-ABL1 Fusion -> BCR::ABL1 Fusion). Annotation runs on the rewritten name.
    NORMALIZED,

    // The name used more than one hyphen, so the split is not read off the name alone. Rather than
    // search for it, the name is left untouched and the query is reported as invalid.
    AMBIGUOUS
}
