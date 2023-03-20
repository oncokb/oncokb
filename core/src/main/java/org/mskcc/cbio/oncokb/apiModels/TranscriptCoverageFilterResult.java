package org.mskcc.cbio.oncokb.apiModels;

public class TranscriptCoverageFilterResult {
    String variant;
    Boolean isCovered;

    public TranscriptCoverageFilterResult(String variant, Boolean isCovered) {
        this.variant = variant;
        this.isCovered = isCovered;
    }

    public String getVariant() {
        return this.variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public Boolean getIsCovered() {
        return this.isCovered;
    }

    public void setIsCovered(Boolean isCovered) {
        this.isCovered = isCovered;
    }
}
