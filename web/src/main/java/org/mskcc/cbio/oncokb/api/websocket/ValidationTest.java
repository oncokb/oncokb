package org.mskcc.cbio.oncokb.api.websocket;

/**
 * Created by Hongxin Zhang on 3/6/20.
 */
public enum ValidationTest {
    MISSING_CLINICAL_INFO_VARIANTS(""),
    MISSING_BIOLOGICAL_INFO_VARIANTS("")
    ;

    ValidationTest(String name) {
        this.name = name;
    }

    String name;

    public String getName() {
        return name;
    }
}
