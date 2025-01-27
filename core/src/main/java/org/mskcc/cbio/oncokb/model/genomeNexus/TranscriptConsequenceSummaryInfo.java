package org.mskcc.cbio.oncokb.model.genomeNexus;

import org.genome_nexus.client.TranscriptConsequenceSummary;

public class TranscriptConsequenceSummaryInfo {
    
    private TranscriptConsequenceSummary transcriptConsequenceSummary;

    private String missingCanonicalTranscriptMessage;


    public TranscriptConsequenceSummaryInfo(TranscriptConsequenceSummary transcriptConsequenceSummary, String missingCanonicalTranscriptMessage) {
        this.transcriptConsequenceSummary = transcriptConsequenceSummary;
        this.missingCanonicalTranscriptMessage = missingCanonicalTranscriptMessage;
    }


    public TranscriptConsequenceSummary getTranscriptConsequenceSummary() {
        return this.transcriptConsequenceSummary;
    }

    public void setTranscriptConsequenceSummary(TranscriptConsequenceSummary transcriptConsequenceSummary) {
        this.transcriptConsequenceSummary = transcriptConsequenceSummary;
    }

    public String getMissingCanonicalTranscriptMessage() {
        return this.missingCanonicalTranscriptMessage;
    }

    public void setMissingCanonicalTranscriptMessage(String missingCanonicalTranscriptMessage) {
        this.missingCanonicalTranscriptMessage = missingCanonicalTranscriptMessage;
    }

}
