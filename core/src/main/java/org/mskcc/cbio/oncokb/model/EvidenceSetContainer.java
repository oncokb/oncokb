package org.mskcc.cbio.oncokb.model;

import java.util.Set;

public class EvidenceSetContainer {
    public Set<Evidence> relevantEvidencesFilteredByTumorType;
    public Set<Evidence> relevantEvidencesUnfiltered;

    public EvidenceSetContainer(Set<Evidence> filtered, Set<Evidence> unfiltered) {
        this.relevantEvidencesFilteredByTumorType = filtered;
        this.relevantEvidencesUnfiltered = unfiltered;
    }
}
