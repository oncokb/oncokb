package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.genome_nexus.ApiException;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.VariantSearchQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Api(tags = "Variants", description = "Endpoints related to OncoKB variants")
public interface VariantsApi {

    // We should remove it in the next release
    @Deprecated
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Search for matched variants.", response = Alteration.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Alteration.class, responseContainer = "List")})
//    @RequestMapping(value = "/variants/lookup",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<List<Alteration>> variantsLookupGet(
        @ApiParam(value = "The entrez gene ID.") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "The gene symbol used in Human Genome Organisation.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "variant name.") @RequestParam(value = "variant", required = false) String variant
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "The fields to be returned.") @RequestParam(value = "fields", required = false) String fields
    ) throws ApiException;

    // We cannot delete this method just yet since the curation platform is relaying on the endpoint
    @Deprecated
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Search for variants.", response = List.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class, responseContainer = "List")})
    @RequestMapping(value = "/variants/lookup",
        produces = {"application/json"},
        method = RequestMethod.POST)
    ResponseEntity<List<List<Alteration>>> variantsLookupPost(
        @ApiParam(value = "List of queries.", required = true) @RequestBody(required = true) List<VariantSearchQuery> body
        , @ApiParam(value = "The fields to be returned.") @RequestParam(value = "fields", required = false) String fields
    ) throws ApiException;

}
