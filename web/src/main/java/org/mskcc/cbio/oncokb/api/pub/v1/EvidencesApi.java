package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.apiModels.ApiErrorResp;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.ApiObjectResp;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Api(value = "evidences", description = "The evidences API")
public interface EvidencesApi {

//    @ApiOperation(value = "", notes = "Get specific evidence.", response = Evidence.class, tags = {"Evidences",})
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "OK", response = Evidence.class),
//        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ResponseEntity.class)})
//    @RequestMapping(value = "/evidences/{id}",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
//    ResponseEntity<ApiObjectResp> evidencesIdGet(
//        @ApiParam(value = "Unique identifier.", required = true) @PathVariable("id") Integer id
//    );


    @ApiOperation(value = "", notes = "Search evidences. Multi-queries are supported.", response = Evidence.class, responseContainer = "List", tags = {"Evidences", "Search",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Evidence.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
//    @RequestMapping(value = "/evidences/lookup",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<ApiListResp> evidencesLookupGet(
        @ApiParam(value = "The entrez gene ID. Use comma to seperate multi-queries.") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "The gene symbol used in Human Genome Organisation. Use comma to seperate multi-queries.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "Variant name. Use comma to seperate multi-queries.") @RequestParam(value = "variant", required = false) String variant
        , @ApiParam(value = "Tumor type name. OncoTree code is supported. Use comma to seperate multi-queries.") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = "Consequence. Use comma to seperate multi-queries. Possible value: feature_truncation, frameshift_variant, inframe_deletion, inframe_insertion, initiator_codon_variant, missense_variant, splice_region_variant, stop_gained, synonymous_variant") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "Protein Start. Use comma to seperate multi-queries.") @RequestParam(value = "proteinStart", required = false) String proteinStart
        , @ApiParam(value = "Protein End. Use comma to seperate multi-queries.") @RequestParam(value = "proteinEnd", required = false) String proteinEnd
        , @ApiParam(value = "Tumor type source. OncoTree tumor types are the default setting. We may have customized version, like Quest.", defaultValue = "oncotree") @RequestParam(value = "source", required = false, defaultValue = "oncotree") String source
        , @ApiParam(value = "Only show highest level evidences") @RequestParam(value = "highestLevelOnly", required = false, defaultValue = "FALSE") Boolean highestLevelOnly
        , @ApiParam(value = "Separate by comma. LEVEL_1, LEVEL_2A, LEVEL_2B, LEVEL_3A, LEVEL_3B, LEVEL_4, LEVEL_R1, LEVEL_R2, LEVEL_R3") @RequestParam(value = "levelOfEvidence", required = false) String levels
        , @ApiParam(value = "Separate by comma. Evidence type includes GENE_SUMMARY, GENE_BACKGROUND, MUTATION_SUMMARY, ONCOGENIC, MUTATION_EFFECT, VUS, PREVALENCE, PROGNOSTIC_IMPLICATION, TUMOR_TYPE_SUMMARY, NCCN_GUIDELINES, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, CLINICAL_TRIAL") @RequestParam(value = "evidenceTypes", required = false) String evidenceTypes
    );

    @ApiOperation(value = "", notes = "Search evidences.", response = EvidenceQueryRes.class, responseContainer = "List", tags = {"Evidences", "Search",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = EvidenceQueryRes.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
//    @RequestMapping(value = "/evidences/lookup",
//        consumes = {"application/json"},
//        produces = {"application/json"},
//        method = RequestMethod.POST)
    ResponseEntity<ApiListResp> evidencesLookupPost(@ApiParam(value = "List of queries. Please see swagger.json for request body format. Please use JSON string.", required = true) @RequestBody(required = true) EvidenceQueries body
    );
    
//    @ApiOperation(value = "", notes = "Get specific evidences.", response = Evidence.class, responseContainer = "List", tags = {"Evidences",})
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "OK", response = Evidence.class, responseContainer = "List"),
//        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
//    @RequestMapping(value = "/evidences",
//        consumes = {"application/json"},
//        produces = {"application/json"},
//        method = RequestMethod.POST)
//    ResponseEntity<ApiListResp> evidencesPost(@ApiParam(value = "List of unique identifier for each model. Separated by comma.", required = true) @RequestParam(value = "ids", required = true) String ids
//    );
}
