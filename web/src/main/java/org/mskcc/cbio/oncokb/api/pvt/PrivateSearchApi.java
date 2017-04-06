package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * Created by Hongxin on 12/12/16.
 */

@Api(value = "/search", description = "The utils API")
public interface PrivateSearchApi {
    @ApiOperation(value = "", notes = "Get annotated variants information for specified gene.", response = BiologicalVariant.class, responseContainer = "Set", tags = "Variants")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/search/variants/biological",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Set<BiologicalVariant>> searchVariantsBiologicalGet(@ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    );


    @ApiOperation(value = "", notes = "Get list of variant clinical information for specified gene.", response = ClinicalVariant.class, responseContainer = "Set", tags = "Variants")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/search/variants/clinical",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Set<ClinicalVariant>> searchVariantsClinicalGet(@ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol

    );

    @ApiOperation(value = "", notes = "Search to find treatments.", response = Treatment.class, responseContainer = "Set", tags = "Treatments")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/search/treatments",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<Set<Treatment>> searchTreatmentsGet(
        @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.", required = true) @RequestParam(value = "gene", required = false) String queryGene,
        @ApiParam(value = "The level of evidence.", defaultValue = "false") @RequestParam(value = "level", required = false) String queryLevel
    );
}

