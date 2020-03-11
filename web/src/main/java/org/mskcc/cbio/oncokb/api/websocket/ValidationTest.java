package org.mskcc.cbio.oncokb.api.websocket;

/**
 * Created by Hongxin Zhang on 3/6/20.
 */
public enum ValidationTest {
    MISSING_CLINICAL_ALTERATION_INFO("Whether clinical alteration missing information"),
    MISSING_BIOLOGICAL_ALTERATION_INFO("Whether biological alteration missing information"),
    MISSING_GENE_INFO("Whether gene missing summary or background")
    ;

    ValidationTest(String name) {
        this.name = name;
    }

    String name;

    public String getName() {
        return name;
    }
}
