package org.mskcc.cbio.oncokb.apiModels;

import org.oncokb.oncokb_transcript.client.TranscriptComparisonResultVM;

/**
 * Created by Hongxin Zhang on 3/9/21.
 */
public class TranscriptUpdateValidationVM {
    TranscriptComparisonResultVM grch37;
    TranscriptComparisonResultVM grch38;

    public TranscriptComparisonResultVM getGrch37() {
        return grch37;
    }

    public void setGrch37(TranscriptComparisonResultVM grch37) {
        this.grch37 = grch37;
    }

    public TranscriptComparisonResultVM getGrch38() {
        return grch38;
    }

    public void setGrch38(TranscriptComparisonResultVM grch38) {
        this.grch38 = grch38;
    }
}
