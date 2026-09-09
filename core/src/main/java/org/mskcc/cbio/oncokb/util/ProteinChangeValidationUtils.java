package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.ValidationError;
import org.mskcc.cbio.oncokb.apiModels.ProteinChangeValidation;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ProteinChangeValidationStatus;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.VariantAnnotationMessageType;
import org.oncokb.oncokb_transcript.ApiException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;

/**
 * Obtains the OncoKB canonical protein sequence for a gene and reports what {@link ProteinChangeValidator}
 * makes of a queried protein change against it. {@link ProteinChangeValidator} itself is pure, so this is
 * where the sequence lookup and its failure modes live, shared by every endpoint that reports validation.
 *
 * <p>An alteration OncoKB itself curates is never checked, whatever it looks like. See
 * {@link #isCurated(ReferenceGenome, Gene, String)}.
 */
public final class ProteinChangeValidationUtils {

    private ProteinChangeValidationUtils() {}

    /**
     * The annotation-path entry point: the reasons the queried protein change cannot exist on the OncoKB
     * canonical sequence, empty when there is nothing to report. Returned as a list so it composes with the
     * other checks an annotated query collects; the protein-change check itself reports at most one.
     *
     * <p>Unlike {@link #validate}, this only consults the protein-sequence cache warmed at startup and never
     * reaches for the transcript service, so it is safe to call on every annotated query. A gene with no
     * cached sequence — including the case where the cache never loaded — is simply left unchecked, which
     * annotates as usual rather than failing the query. So is an alteration OncoKB curates.
     */
    public static List<ValidationError> getProteinChangeValidationErrors(ReferenceGenome referenceGenome, Gene gene, String proteinChange) {
        if (gene == null || gene.getEntrezGeneId() == null || !CacheUtils.isProteinSequenceCached()) {
            return Collections.emptyList();
        }
        if (isCurated(referenceGenome, gene, proteinChange)) {
            return Collections.emptyList();
        }
        String canonicalSequence = CacheUtils.getProteinSequence(
            referenceGenome == null ? DEFAULT_REFERENCE_GENOME : referenceGenome, gene.getEntrezGeneId());
        if (StringUtils.isEmpty(canonicalSequence)) {
            return Collections.emptyList();
        }
        return ProteinChangeValidator.validate(gene.getHugoSymbol(), proteinChange, canonicalSequence)
            .map(Collections::singletonList)
            .orElse(Collections.emptyList());
    }

    /**
     * Returns an INVALID validation when the query disagrees with the canonical sequence, an UNCHECKED one
     * when the sequence could not be obtained, and null when the query agrees, is curated, or is not a
     * reference-bearing protein change. Callers that rewrite the query before annotating layer their own
     * NORMALIZED status on top of this result.
     */
    public static ProteinChangeValidation validate(CacheFetcher cacheFetcher, ReferenceGenome referenceGenome, Gene gene, String proteinChange) {
        if (gene == null) {
            return null;
        }
        if (isCurated(referenceGenome, gene, proteinChange)) {
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

        Optional<ValidationError> invalid =
            ProteinChangeValidator.validate(gene.getHugoSymbol(), proteinChange, canonicalSequence);
        return invalid
            .map(error -> new ProteinChangeValidation(ProteinChangeValidationStatus.INVALID,
                VariantAnnotationMessageType.INVALID_PROTEIN_CHANGE, error.getMessage()))
            .orElse(null);
    }

    private static boolean isCurated(ReferenceGenome referenceGenome, Gene gene, String proteinChange) {
        if (StringUtils.isEmpty(proteinChange)) {
            return false;
        }
        Alteration curated = AlterationUtils.findAlterationWithGeneticType(
            referenceGenome, gene, proteinChange, AlterationUtils.getAllAlterations(referenceGenome, gene), null);
        return curated != null;
    }

    private static ProteinChangeValidation unchecked(VariantAnnotationMessageType messageType) {
        return new ProteinChangeValidation(ProteinChangeValidationStatus.UNCHECKED, messageType, null);
    }
}
