

package org.mskcc.cbio.oncokb.model;

import java.util.HashMap;
import java.util.Map;

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


    private static final Map<String, SpecialTumorType> map = new HashMap<>();

    static {
        for (SpecialTumorType specialTumorType : SpecialTumorType.values()) {
            map.put(specialTumorType.getTumorType().toLowerCase(), specialTumorType);
        }
    }

    public static SpecialTumorType getByTumorType(String tumorType) {
        if (tumorType == null) return null;
        return map.get(tumorType.toLowerCase());
    }
}
