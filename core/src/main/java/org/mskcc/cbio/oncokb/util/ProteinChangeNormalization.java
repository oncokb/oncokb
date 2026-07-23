package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.VariantAnnotationMessageType;

/**
 * A single, unambiguous rewrite applied to a queried protein change before annotation. Each value
 * pairs the machine-readable {@link VariantAnnotationMessageType} with the human-readable rationale,
 * so adding a normalization (e.g. collapsing an insertion that repeats its neighbour into a {@code dup})
 * is a localized change: add a value here and the rule that produces it in
 * {@link ProteinChangeValidator#normalize(String)}.
 */
public enum ProteinChangeNormalization {
    // A spelled-out deleted sequence was dropped: A237_G238delAG -> A237_G238del, and
    // A237_G238delAGinsCT -> A237_G238delinsCT. HGVS describes deletions by position only.
    DELETED_SEQUENCE_DROPPED(VariantAnnotationMessageType.NORMALIZED_DELETED_SEQUENCE) {
        @Override
        public String describe(String hugoSymbol, String original, String normalized) {
            return hugoSymbol + " " + original + " normalized to " + normalized
                + ": the deleted residues do not need to be listed.";
        }
    };

    private final VariantAnnotationMessageType messageType;

    ProteinChangeNormalization(VariantAnnotationMessageType messageType) {
        this.messageType = messageType;
    }

    public VariantAnnotationMessageType getMessageType() {
        return messageType;
    }

    /** Human-readable explanation of this rewrite, from the original query to the normalized form. */
    public abstract String describe(String hugoSymbol, String original, String normalized);
}
