package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.mskcc.cbio.oncokb.model.OncoKBVariant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;

/**
 * Created by Hongxin on 10/28/16.
 */

@Api(value = "classification", description = "The OncoKB Classification API")
public interface ClassificationApi {
    @ApiOperation(value = "", notes = "Get All OncoKB Variant Classification.", response = String.class, responseContainer = "Set", tags = "Classification")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "Set")})
    @RequestMapping(value = "/classification/variants", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Set<String>> classificationVariantsGet();

}
