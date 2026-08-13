package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.AlterationValidationError;
import org.mskcc.cbio.oncokb.apiModels.ProteinChangeValidation;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ProteinChangeValidationStatus;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.VariantAnnotationMessageType;
import org.oncokb.oncokb_transcript.ApiException;

import java.util.Optional;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Obtains the OncoKB canonical protein sequence for a gene and reports what {@link ProteinChangeValidator}
 * makes of a queried protein change against it. {@link ProteinChangeValidator} itself is pure, so this is
 * where the sequence lookup and its failure modes live, shared by every endpoint that reports validation.
 */
public final class ProteinChangeValidationUtils {

    private ProteinChangeValidationUtils() {}

    /**
     * The annotation-path entry point: the reason the queried protein change cannot exist on the OncoKB
     * canonical sequence, or null when there is nothing to report.
     *
     * <p>Unlike {@link #validate}, this only consults the protein-sequence cache warmed at startup and never
     * reaches for the transcript service, so it is safe to call on every annotated query. A gene with no
     * cached sequence — including the case where the cache never loaded — is simply left unchecked, which
     * annotates as usual rather than failing the query.
     */
    public static AlterationValidationError getAlterationValidationError(ReferenceGenome referenceGenome, Gene gene, String proteinChange) {
        if (gene == null || gene.getEntrezGeneId() == null || !CacheUtils.isProteinSequenceCached()) {
            return null;
        }
        String canonicalSequence = CacheUtils.getProteinSequence(
            referenceGenome == null ? DEFAULT_REFERENCE_GENOME : referenceGenome, gene.getEntrezGeneId());
        if (StringUtils.isEmpty(canonicalSequence)) {
            return null;
        }
        return ProteinChangeValidator.validate(gene.getHugoSymbol(), proteinChange, canonicalSequence).orElse(null);
    }

    /**
     * Returns an INVALID validation when the query disagrees with the canonical sequence, an UNCHECKED one
     * when the sequence could not be obtained, and null when the query agrees or is not a reference-bearing
     * protein change. Callers that rewrite the query before annotating layer their own NORMALIZED status on
     * top of this result.
     */
    public static ProteinChangeValidation validate(CacheFetcher cacheFetcher, ReferenceGenome referenceGenome, Gene gene, String proteinChange) {
        if (gene == null) {
            return null;
        }
        if (!cacheFetcher.isTranscriptServiceEnabled()) {
            return unchecked(VariantAnnotationMessageType.TRANSCRIPT_SERVICE_DISABLED);
        }
        String canonicalSequence;
        try {
            canonicalSequence = cacheFetcher.getCanonicalProteinSequence(referenceGenome, gene);
        } catch (ApiException e) {
            return unchecked(VariantAnnotationMessageType.TRANSCRIPT_SERVICE_UNAVAILABLE);
        }
        if (StringUtils.isEmpty(canonicalSequence)) {
            return unchecked(VariantAnnotationMessageType.NO_PROTEIN_SEQUENCE);
        }

        Optional<AlterationValidationError> invalid =
            ProteinChangeValidator.validate(gene.getHugoSymbol(), proteinChange, canonicalSequence);
        return invalid
            .map(error -> new ProteinChangeValidation(ProteinChangeValidationStatus.INVALID,
                VariantAnnotationMessageType.INVALID_PROTEIN_CHANGE, error.getMessage()))
            .orElse(null);
    }

    private static ProteinChangeValidation unchecked(VariantAnnotationMessageType messageType) {
        return new ProteinChangeValidation(ProteinChangeValidationStatus.UNCHECKED, messageType, null);
    }
}
