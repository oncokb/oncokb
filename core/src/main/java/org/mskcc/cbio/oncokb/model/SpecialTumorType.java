

package org.mskcc.cbio.oncokb.model;

/**
 * @author Hongxin
 */
public enum SpecialTumorType {
    ALL_TUMORS("All Tumors"),
    ALL_LIQUID_TUMORS("All Liquid Tumors"),
    ALL_SOLID_TUMORS("All Solid Tumors"),
    GERMLINE_DISPOSITION("Germline Disposition")
    , OTHER_TUMOR_TYPES("Other Tumor Types")
    , OTHER_SOLID_TUMOR_TYPES("Other Solid Tumor Types")
    , OTHER_LIQUID_TUMOR_TYPES("Other Liquid Tumor Types")
    ;

    private SpecialTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    private final String tumorType;

    public String getTumorType() {
        return tumorType;
    }
}
