package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.GeneEvidence;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Api(value = "genes", description = "The genes API")
public interface GenesApi {

    @ApiOperation(value = "", notes = "Get list of evidences for specific gene.", response = GeneEvidence.class, responseContainer = "List", tags = {"Evidences"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GeneEvidence.class, responseContainer = "List")})
//    @RequestMapping(value = "/genes/{entrezGeneId}/evidences",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<List<GeneEvidence>> genesEntrezGeneIdEvidencesGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
        , @ApiParam(value = "Separate by comma. Evidence type includes GENE_SUMMARY, GENE_BACKGROUND, MUTATION_SUMMARY, ONCOGENIC, MUTATION_EFFECT, VUS, PREVALENCE, PROGNOSTIC_IMPLICATION, DIAGNOSTIC_IMPLICATION, TUMOR_TYPE_SUMMARY, NCCN_GUIDELINES, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, CLINICAL_TRIAL") @RequestParam(value = "evidenceTypes", required = false) String evidenceTypes
    );


    @ApiOperation(value = "", notes = "Get specific gene information.", response = Gene.class, tags = {"Genes",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Gene.class)})
//    @RequestMapping(value = "/genes/{entrezGeneId}",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<Gene> genesEntrezGeneIdGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
    );


    @ApiOperation(value = "", notes = "Get list of variants for specific gene.", response = Alteration.class, responseContainer = "List", tags = {"Variants"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Alteration.class, responseContainer = "List")})
//    @RequestMapping(value = "/genes/{entrezGeneId}/variants",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<List<Alteration>> genesEntrezGeneIdVariantsGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
    );


    @ApiOperation(value = "", notes = "Get list of currently curated genes.", response = Gene.class, responseContainer = "List", tags = {"Genes",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Gene.class, responseContainer = "List")})
//    @RequestMapping(value = "/genes",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<List<Gene>> genesGet();


    @ApiOperation(value = "", notes = "Search gene.", response = Gene.class, responseContainer = "List", tags = {"Genes", "Search",})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Gene.class, responseContainer = "List")})
//    @RequestMapping(value = "/genes/lookup",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<List<Gene>> genesLookupGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation. (Deprecated, use query instead)") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Deprecated, use query instead)") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.") @RequestParam(value = "query", required = false) String query
    );

}
