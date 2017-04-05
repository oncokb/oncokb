package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.mskcc.cbio.oncokb.apiModels.ApiErrorResp;
import org.mskcc.cbio.oncokb.apiModels.ApiObjectResp;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Api(value = "levels", description = "The levels API")
public interface LevelsApi {

    @ApiOperation(value = "", notes = "Get all levels.", response = ApiObjectResp.class, tags = {"Levels",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApiObjectResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
//    @RequestMapping(value = "/levels",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<ApiObjectResp> levelsGet();


    @ApiOperation(value = "", notes = "Get all resistence levels.", response = ApiObjectResp.class, tags = {"Levels",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApiObjectResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
//    @RequestMapping(value = "/levels/resistence",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<ApiObjectResp> levelsResistenceGet();


    @ApiOperation(value = "", notes = "Get all sensitive levels.", response = ApiObjectResp.class, tags = {"Levels",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApiObjectResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
//    @RequestMapping(value = "/levels/sensitive",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<ApiObjectResp> levelsSensitiveGet();

}
