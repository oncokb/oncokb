package org.mskcc.cbio.oncokb.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.response.ApiGenes;
import org.mskcc.cbio.oncokb.response.ApiSearchEvidences;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/search", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/search", description = "the search API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class SearchApi {


    @ApiOperation(value = "", notes = "Get list of evidences.", response = ApiSearchEvidences.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/evidences/",
        produces = {"application/json"},
        consumes = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiSearchEvidences> searchEvidencesGet(
        @ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "") @RequestParam(value = "type", required = false) List<String> type
    ) throws NotFoundException {
        // do some magic!
        return new ResponseEntity<ApiSearchEvidences>(HttpStatus.OK);
    }


    @ApiOperation(value = "", notes = "Search to find gene. Code 204 will be returned in the META if no gene matched.", response = ApiGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/gene/{hugoSymbol}",
        produces = {"application/json"},
        consumes = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiGenes> searchGeneHugoSymbolGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("hugoSymbol") String hugoSymbol,
        @ApiParam(value = "Find the exact match with query.", defaultValue = "false") @RequestParam(value = "exactMatch", required = false, defaultValue = "false") Boolean exactMatch


    ) throws NotFoundException {
        // do some magic!
        return new ResponseEntity<ApiGenes>(HttpStatus.OK);
    }
}
