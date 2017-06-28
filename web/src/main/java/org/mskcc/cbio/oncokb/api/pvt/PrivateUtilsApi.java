package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Hongxin on 12/12/16.
 */

@Api(value = "/utils", description = "The utils API")
public interface PrivateUtilsApi {
    @ApiOperation(value = "", notes = "Get All Suggested Variants.", response = String.class, responseContainer = "List", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AnnotatedVariant.class, responseContainer = "List")})
    @RequestMapping(value = "/utils/suggestedVariants", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<String>> utilsSuggestedVariantsGet();

    @ApiOperation(value = "", notes = "Determine whether variant is hotspot mutation.", response = Boolean.class, tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class)})
    @RequestMapping(value = "/utils/isHotspot", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Boolean> utilsHotspotMutationGet(
        @ApiParam(value = "Gene hugo symbol") @RequestParam(value = "hugoSymbol") String hugoSymbol
        , @ApiParam(value = "Variant name") @RequestParam(value = "variant") String variant
    );

    @ApiOperation(value = "", notes = "Get gene related numbers", response = GeneNumber.class, tags = "Numbers")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/utils/numbers/gene/{hugoSymbol}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<GeneNumber> utilsNumbersGeneGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("hugoSymbol") String hugoSymbol
    );

    @ApiOperation(value = "", notes = "Get gene related numbers of all genes. This is for main page word cloud.", response = GeneNumber.class, responseContainer = "Set", tags = "Numbers")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/utils/numbers/genes/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Set<GeneNumber>> utilsNumbersGenesGet();

    @ApiOperation(value = "", notes = "Get numbers served for the main page dashboard.", response = MainNumber.class, tags = "Numbers")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/utils/numbers/main/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<MainNumber> utilsNumbersMainGet();

    @ApiOperation(value = "", notes = "Get gene related numbers of all genes. This is for main page word cloud.", response = LevelNumber.class, responseContainer = "Set", tags = "Numbers")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/utils/numbers/levels/",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Set<LevelNumber>> utilsNumbersLevelsGet();

    @ApiOperation(value = "", notes = "Check if clinical trials are valid or not by nctId.", response = Map.class, tags = "Utils")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/utils/validation/trials",
            produces = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<Map<String, Boolean>> validateTrials(@ApiParam(value = "NCT ID list") @RequestParam(value = "nctIds") List<String> nctIds) throws ParserConfigurationException, SAXException, IOException;

}

