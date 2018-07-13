package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.mskcc.cbio.oncokb.model.OncoKBInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Hongxin Zhang on 7/13/18.
 */

@Api(value = "/info", description = "This includes all OncoKB general information")
public interface InfoApi {
    @ApiOperation(value = "", response = OncoKBInfo.class, tags = "Info")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OncoKBInfo.class)})
    @RequestMapping(value = "/info", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<OncoKBInfo> infoGet();
}
