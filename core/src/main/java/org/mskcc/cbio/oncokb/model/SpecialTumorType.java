

package org.mskcc.cbio.oncokb.model;

/**
 * @author Hongxin
 */
public enum SpecialTumorType {
    ALL_TUMORS("All Tumors"),
    ALL_LIQUID_TUMORS("All Liquid Tumors"),
    ALL_SOLID_TUMORS("All Solid Tumors"),
    ALL_PEDIATRIC_TUMORS("All Pediatric Tumors"),
    GERMLINE_DISPOSITION("Germline Disposition"),
    OTHER_TUMOR_TYPES("Other Tumor Types");

    private SpecialTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    private final String tumorType;

    public String getTumorType() {
        return tumorType;
    }
}
