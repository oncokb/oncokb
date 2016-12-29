package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.apiModels.ApiErrorResp;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Hongxin on 12/12/16.
 */

@Api(value = "/utils", description = "The utils API")
public interface PrivateUtilsApi {
    @ApiOperation(value = "", notes = "Get All Suggested Variants.", response = String.class, responseContainer = "List", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AnnotatedVariant.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
    @RequestMapping(value = "/utils/suggestedVariants", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<ApiListResp> utilsSuggestedVariantsGet();
}
