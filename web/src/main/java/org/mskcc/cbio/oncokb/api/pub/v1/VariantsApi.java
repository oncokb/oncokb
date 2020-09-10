package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
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

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get all annotated variants.", response = Alteration.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Alteration.class, responseContainer = "List")})
    @RequestMapping(value = "/variants",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Alteration>> variantsGet(
        @ApiParam(value = "The fields to be returned.") @RequestParam(value = "fields", required = false) String fields
    );


    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Search for variants.", response = Alteration.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Alteration.class, responseContainer = "List")})
    @RequestMapping(value = "/variants/lookup",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<Alteration>> variantsLookupGet(
        @ApiParam(value = "The entrez gene ID.") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "The gene symbol used in Human Genome Organisation.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "variant name.") @RequestParam(value = "variant", required = false) String variant
        , @ApiParam(value = "") @RequestParam(value = "variantType", required = false) String variantType
        , @ApiParam(value = "") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "") @RequestParam(value = "proteinStart", required = false) Integer proteinStart
        , @ApiParam(value = "") @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd
        , @ApiParam(value = "HGVS varaint. Its priority is higher than entrezGeneId/hugoSymbol + variant combination") @RequestParam(value = "hgvs", required = false) String hgvs
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "The fields to be returned.") @RequestParam(value = "fields", required = false) String fields
    );

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
    );

}
