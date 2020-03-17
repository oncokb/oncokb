package org.mskcc.cbio.oncokb.api.websocket;

/**
 * Created by Hongxin Zhang on 3/6/20.
 */
public enum ValidationTest {
    MISSING_TREATMENT_INFO("Whether treatment missing information"),
    MISSING_BIOLOGICAL_ALTERATION_INFO("Whether biological alteration missing information"),
    MISSING_GENE_INFO("Whether gene missing summary or background"),
    INCORRECT_EVIDENCE_DESCRIPTION_FORMAT("Whether evidence description has wrong format content"),
    INCORRECT_ALTERATION_NAME_FORMAT("Whether alteration is named appropriately"),
    ;

    ValidationTest(String name) {
        this.name = name;
    }

    String name;

    public String getName() {
        return name;
    }
}
