package org.mskcc.cbio.oncokb.model;

import java.util.List;

public class EvidenceListContainer { // TODO: rename?
    public List<Evidence> relevantEvidencesFilteredByTumorType;
    public List<Evidence> relevantEvidencesUnfiltered;

    public EvidenceListContainer(List<Evidence> filtered, List<Evidence> unfiltered) { // TODO: change both to list? Set?
        this.relevantEvidencesFilteredByTumorType = filtered;
        this.relevantEvidencesUnfiltered = unfiltered;
    }
}
