package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin Zhang on 9/27/18.
 */
public enum WorkSheetEntryEnum {

    GENE_SUMMARY_BACKGROUND(0),
    EMPTY_CLINICAL(1),
    EMPTY_BIOLOGICAL(2),
    LATEST_ACTIONABLE_GENES(3),
    PUBLISHED_ACTIONABLE_GENES(4),
    TUMOR_SUMMARIES(5);

    private int index;

    WorkSheetEntryEnum(int index) {
        this.index = index;
    }

    public int index() {
        return this.index;
    }
}
