package org.mskcc.cbio.oncokb.apiModels;

import org.genome_nexus.client.EnsemblTranscript;

/**
 * Created by Hongxin Zhang on 7/15/20.
 */
public class TranscriptResult {
    String note;
    EnsemblTranscript grch37Transcript;
    EnsemblTranscript grch38Transcript;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public EnsemblTranscript getGrch37Transcript() {
        return grch37Transcript;
    }

    public void setGrch37Transcript(EnsemblTranscript grch37Transcript) {
        this.grch37Transcript = grch37Transcript;
    }

    public EnsemblTranscript getGrch38Transcript() {
        return grch38Transcript;
    }

    public void setGrch38Transcript(EnsemblTranscript grch38Transcript) {
        this.grch38Transcript = grch38Transcript;
    }
}
