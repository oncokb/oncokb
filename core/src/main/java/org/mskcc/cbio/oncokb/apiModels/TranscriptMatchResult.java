package org.mskcc.cbio.oncokb.apiModels;

import org.genome_nexus.client.EnsemblTranscript;

/**
 * Created by Hongxin Zhang on 7/15/20.
 */
public class TranscriptMatchResult {
    String note;
    EnsemblTranscript originalEnsemblTranscript;
    EnsemblTranscript targetEnsemblTranscript;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public EnsemblTranscript getOriginalEnsemblTranscript() {
        return originalEnsemblTranscript;
    }

    public void setOriginalEnsemblTranscript(EnsemblTranscript originalEnsemblTranscript) {
        this.originalEnsemblTranscript = originalEnsemblTranscript;
    }

    public EnsemblTranscript getTargetEnsemblTranscript() {
        return targetEnsemblTranscript;
    }

    public void setTargetEnsemblTranscript(EnsemblTranscript targetEnsemblTranscript) {
        this.targetEnsemblTranscript = targetEnsemblTranscript;
    }
}
