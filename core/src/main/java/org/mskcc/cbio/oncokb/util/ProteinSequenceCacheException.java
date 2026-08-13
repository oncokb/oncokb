package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.ReferenceGenome;

/**
 * Thrown when the canonical protein sequences could not be loaded into {@link CacheUtils}.
 *
 * <p>Unlike the other caches, a partial protein sequence cache is not safe to run with: protein change
 * validation would quietly stop checking the genes whose sequences went missing, so this is raised
 * separately from every other startup failure to let {@link CacheUtils} abort startup rather than log
 * and continue.
 */
public class ProteinSequenceCacheException extends RuntimeException {

    public ProteinSequenceCacheException(ReferenceGenome referenceGenome, Throwable cause) {
        super("Unable to cache the canonical protein sequences for " + referenceGenome, cause);
    }
}
