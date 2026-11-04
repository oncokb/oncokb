package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByGenomicChangeQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByHGVScQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByHGVSgQuery;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpError;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.mskcc.cbio.oncokb.model.GermlineIndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.InheritanceMechanism;
import org.mskcc.cbio.oncokb.service.GermlineAnnotationService;
import org.oncokb.oncokb_transcript.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Api(tags = "Annotations for Germline", description = "Germline endpoints")
@Controller
public class GermlineAnnotationsApiController {
    private static final Logger LOG = LoggerFactory.getLogger(GermlineAnnotationsApiController.class);

    @Autowired
    GermlineAnnotationService germlineAnnotationService;

    // Annotate mutations by genomic change
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by genomic change.", response = GermlineIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byGenomicChange",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<GermlineIndicatorQueryResp> annotateMutationsByGenomicChangeGet(
        @ApiParam(value = "Genomic location following TCGA MAF format. Example: 7,140453136,140453136,A,T", required = true) @RequestParam(value = "genomicLocation", required = true) String genomicLocation
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        return new ResponseEntity<>(germlineAnnotationService.annotateMutationByGenomicChange(genomicLocation, referenceGenome, tumorType), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by genomic change.", response = GermlineIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byGenomicChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<GermlineIndicatorQueryResp>> annotateMutationsByGenomicChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByGenomicChangeQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/germline/mutations/byGenomicChange annotations count: {}", body.size());
        return new ResponseEntity<>(germlineAnnotationService.annotateMutationsByGenomicChange(body), HttpStatus.OK);
    }

    // Annotate mutations by HGVSg
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by HGVSg.", response = GermlineIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byHGVSg",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<GermlineIndicatorQueryResp> annotateMutationsByHGVSgGet(
        @ApiParam(value = "HGVS genomic format following HGVS nomenclature. Example: 7:g.140453136A>T", required = true) @RequestParam(value = "hgvsg", required = true) String hgvsg
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        return new ResponseEntity<>(germlineAnnotationService.annotateMutationByHGVSg(hgvsg, referenceGenome, tumorType), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by HGVSg.", response = GermlineIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byHGVSg",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<GermlineIndicatorQueryResp>> annotateMutationsByHGVSgPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVSgQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/germline/mutations/byHGVSg annotations count: {}", body.size());
        return new ResponseEntity<>(germlineAnnotationService.annotateMutationsByHGVSg(body), HttpStatus.OK);
    }

    // Annotate mutations by HGVSc
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by HGVSc.", response = GermlineIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byHGVSc",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<GermlineIndicatorQueryResp> annotateMutationsByHGVScGet(
        @ApiParam(value = "HGVS cDNA format following HGVS nomenclature. Example: EGFR:c.2369C>T", required = true) @RequestParam(value = "hgvsc", required = true) String hgvsc
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = "List of inheritance mechanisms used to filter the returned genomic indicators. The special value CARRIER is matched against the genomic indicator name rather than the inheritance mechanism. Example: AUTOSOMAL_DOMINANT,CARRIER") @RequestParam(value = "inheritanceMechanisms", required = false) List<InheritanceMechanism> inheritanceMechanisms
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        return new ResponseEntity<>(germlineAnnotationService.annotateMutationByHGVSc(hgvsc, referenceGenome, tumorType, inheritanceMechanisms), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by HGVSc.", response = GermlineIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byHGVSc",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<GermlineIndicatorQueryResp>> annotateMutationsByHGVScPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVScQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/germline/mutations/byHGVSc annotations count: {}", body.size());
        return new ResponseEntity<>(germlineAnnotationService.annotateMutationsByHGVSc(body), HttpStatus.OK);
    }
}
