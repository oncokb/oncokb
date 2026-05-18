package org.mskcc.cbio.oncokb.model.genomeNexus;

import org.genome_nexus.client.TranscriptConsequenceSummary;
import org.mskcc.cbio.oncokb.model.Alteration;

/**
 * This class serves as a structured way to communicate the relationship between 
 * a genomic variant's transcript consequence summary from GN and its corresponding alteration in OncoKB.
 * It helps maintain traceability of how an alteration was derived.
 */
public class TranscriptSummaryAlterationResult {
    private TranscriptConsequenceSummary transcriptConsequenceSummary;  // the summary where transcriptId matches an oncokb canonical transcript
    private Alteration hgvspAlteration = new Alteration();  // the p. Alteration generated using the transcript summary; empty alteration by default
    private Alteration hgvscAlteration = new Alteration();  // the c. Alteration generated using the transcript summary; empty alteration by default
    private String message; // optional message for this annotation


    public TranscriptSummaryAlterationResult() { }

    public TranscriptSummaryAlterationResult(TranscriptConsequenceSummary transcriptConsequenceSummary) {
        this.transcriptConsequenceSummary = transcriptConsequenceSummary;
    }

    public TranscriptConsequenceSummary getTranscriptConsequenceSummary() {
        return this.transcriptConsequenceSummary;
    }

    public void setTranscriptConsequenceSummary(TranscriptConsequenceSummary transcriptConsequenceSummary) {
        this.transcriptConsequenceSummary = transcriptConsequenceSummary;
    }

    public Alteration getHgvspAlteration() {
        return this.hgvspAlteration;
    }

    public void setHgvspAlteration(Alteration hgvspAlteration) {
        this.hgvspAlteration = hgvspAlteration;
    }

    public Alteration getHgvscAlteration() {
        return this.hgvscAlteration;
    }

    public void setHgvscAlteration(Alteration hgvscAlteration) {
        this.hgvscAlteration = hgvscAlteration;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
