package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.apiModels.ApiErrorResp;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.model.Drug;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-14T18:47:53.991Z")

@Api(value = "drugs", description = "the drugs API")
public interface DrugsApi {

    @ApiOperation(value = "", notes = "Get all curated drugs.", response = Drug.class, responseContainer = "List", tags = {"Drugs",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Drug.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
//    @RequestMapping(value = "/drugs",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<ApiListResp> drugsGet();


    @ApiOperation(value = "", notes = "Search drugs.", response = Drug.class, responseContainer = "List", tags = {"Drugs", "Search",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Drug.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
//    @RequestMapping(value = "/drugs/lookup",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<ApiListResp> drugsLookupGet(
        @ApiParam(value = "Drug Name") @RequestParam(value = "name", required = false) String name
//        , @ApiParam(value = "") @RequestParam(value = "fdaApproved", required = false) String fdaApproved
        , @ApiParam(value = "ATC Code") @RequestParam(value = "atcCode", required = false) String atcCode
        , @ApiParam(value = "Drug Synonyms") @RequestParam(value = "synonym", required = false) String synonym
        , @ApiParam(value = "Exactly Match", required = true) @RequestParam(value = "exactMatch", required = true, defaultValue = "true") Boolean exactMatch
    );
}
