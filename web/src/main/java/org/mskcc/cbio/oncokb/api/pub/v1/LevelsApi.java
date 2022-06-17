package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Api(tags = "Levels", description = "OncoKB Levels")
public interface LevelsApi {

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get all levels.", response = Map.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Map.class)})
    @RequestMapping(value = "/levels",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Map<LevelOfEvidence, String>> levelsGet();


    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get all resistance levels.", response = Map.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Map.class)})
    @RequestMapping(value = "/levels/resistance",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Map<LevelOfEvidence, String>> levelsResistanceGet();


    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get all sensitive levels.", response = Map.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Map.class)})
    @RequestMapping(value = "/levels/sensitive",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Map<LevelOfEvidence, String>> levelsSensitiveGet();

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get all prognostic levels.", response = Map.class, tags = {"Levels",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Map.class)})
    @RequestMapping(value = "/levels/prognostic",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Map<LevelOfEvidence, String>> levelsPrognosticGet();

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get all diagnostic levels.", response = Map.class, tags = {"Levels",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Map.class)})
    @RequestMapping(value = "/levels/diagnostic",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Map<LevelOfEvidence, String>> levelsDiagnosticGet();

}
