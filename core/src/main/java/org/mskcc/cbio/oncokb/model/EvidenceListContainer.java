package org.mskcc.cbio.oncokb.model;

import java.util.List;

public class EvidenceListContainer {
    public List<Evidence> relevantEvidencesFilteredByTumorType;
    public List<Evidence> relevantEvidencesUnfiltered;

    public EvidenceListContainer(List<Evidence> filtered, List<Evidence> unfiltered) {
        this.relevantEvidencesFilteredByTumorType = filtered;
        this.relevantEvidencesUnfiltered = unfiltered;
    }
}
