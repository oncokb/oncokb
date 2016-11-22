package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.springframework.http.HttpStatus;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class MetaUtils {
    public static Meta getOKMeta() {
        Meta meta = new Meta();
        meta.setCode(HttpStatus.OK.value());
        return meta;
    }

    public static Meta getBadRequestMeta(String errorMessage) {
        if (errorMessage == null) {
            errorMessage = "Bad request.";
        }
        Meta meta = new Meta();
        meta.setCode(HttpStatus.BAD_REQUEST.value());
        meta.setErrorMessage(errorMessage);
        return meta;
    }
}
