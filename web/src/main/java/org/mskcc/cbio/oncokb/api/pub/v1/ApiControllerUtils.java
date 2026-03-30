package org.mskcc.cbio.oncokb.api.pub.v1;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.springframework.http.HttpStatus;

final class ApiControllerUtils {
    private ApiControllerUtils() {
    }

    static ReferenceGenome resolveMatchedRG(String referenceGenome) throws ApiHttpErrorException {
        ReferenceGenome matchedRG = null;
        if (!StringUtils.isEmpty(referenceGenome)) {
            matchedRG = MainUtils.searchEnum(ReferenceGenome.class, referenceGenome);
            if (matchedRG == null) {
                throw new ApiHttpErrorException(
                    "referenceGenome \"" + referenceGenome + "\" is an invalid Reference Genome value.",
                    HttpStatus.BAD_REQUEST
                );
            }
        }
        return matchedRG;
    }

}
