package org.mskcc.cbio.oncokb.model;

import java.util.Set;

public class EvidenceContainer { // TODO: rename?
    public Set<Evidence> relevantEvidencesFilteredByTumorType;
    public Set<Evidence> relevantEvidencesUnfiltered;

    public EvidenceContainer(Set<Evidence> filtered, Set<Evidence> unfiltered) { // TODO: change both to list? Set?
        this.relevantEvidencesFilteredByTumorType = filtered;
        this.relevantEvidencesUnfiltered = unfiltered;
    }
}
