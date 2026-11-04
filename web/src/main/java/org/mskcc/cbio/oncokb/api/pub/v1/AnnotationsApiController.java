package org.mskcc.cbio.oncokb.api.pub.v1;

import static org.mskcc.cbio.oncokb.util.AnnotationSearchUtils.annotationSearch;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mskcc.cbio.oncokb.apiModels.annotation.*;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.service.SomaticAnnotationService;
import org.mskcc.cbio.oncokb.util.*;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpError;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;

import java.util.*;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
@Api(tags = "Annotations for Somatic", description = "Somatic endpoints")
@Controller
public class AnnotationsApiController {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationsApiController.class);

    final String EVIDENCE_TYPES_DESCRIPTION = "DEPRECATED. We do not recommend using this parameter and it will eventually be removed.";

    @Autowired
    SomaticAnnotationService somaticAnnotationService;

    // Annotate mutations by protein change
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by protein change.", response = SomaticIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/mutations/byProteinChange",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<SomaticIndicatorQueryResp> annotateMutationsByProteinChangeGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation. Example: BRAF") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Higher priority than hugoSymbol). Example: 673") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Protein Change. Example: V600E") @RequestParam(value = "alteration", required = false) String proteinChange
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "Consequence. Example: missense_variant", allowableValues = "feature_truncation, frameshift_variant, inframe_deletion, inframe_insertion, start_lost, missense_variant, splice_region_variant, stop_gained, synonymous_variant, intron_variant") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "Protein Start. Example: 600") @RequestParam(value = "proteinStart", required = false) Integer proteinStart
        , @ApiParam(value = "Protein End. Example: 600") @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) throws ApiHttpErrorException {
        return new ResponseEntity<>(somaticAnnotationService.annotateMutationByProteinChange(hugoSymbol, entrezGeneId, proteinChange, referenceGenome, consequence, proteinStart, proteinEnd, tumorType, evidenceTypes), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by protein change.", response = SomaticIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/mutations/byProteinChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<SomaticIndicatorQueryResp>> annotateMutationsByProteinChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByProteinChangeQuery> body
    ) throws ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/mutations/byProteinChange annotations count: {}", body.size());
        return new ResponseEntity<>(somaticAnnotationService.annotateMutationsByProteinChange(body), HttpStatus.OK);
    }

    // Annotate mutations by genomic change
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by genomic change.", response = SomaticIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/mutations/byGenomicChange",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<SomaticIndicatorQueryResp> annotateMutationsByGenomicChangeGet(
        @ApiParam(value = "Genomic location following TCGA MAF format. Example: 7,140453136,140453136,A,T", required = true) @RequestParam(value = "genomicLocation", required = true) String genomicLocation
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        // , @ApiParam(value = "Whether is germline variant", required = false) @RequestParam(value = "germline", defaultValue = "FALSE", required = false) Boolean germline
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        return new ResponseEntity<>(somaticAnnotationService.annotateMutationByGenomicChange(genomicLocation, referenceGenome, tumorType, evidenceTypes), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by genomic change.", response = SomaticIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/mutations/byGenomicChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<SomaticIndicatorQueryResp>> annotateMutationsByGenomicChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByGenomicChangeQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/mutations/byGenomicChange annotations count: {}", body.size());
        return new ResponseEntity<>(somaticAnnotationService.annotateMutationsByGenomicChange(body), HttpStatus.OK);
    }

    // Annotate mutations by HGVSg
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by HGVSg.", response = SomaticIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/mutations/byHGVSg",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<SomaticIndicatorQueryResp> annotateMutationsByHGVSgGet(
        @ApiParam(value = "HGVS genomic format following HGVS nomenclature. Example: 7:g.140453136A>T", required = true) @RequestParam(value = "hgvsg", required = true) String hgvsg
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        // , @ApiParam(value = "Whether is germline variant", required = false) @RequestParam(value = "germline", defaultValue = "FALSE", required = false) Boolean germline
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        return new ResponseEntity<>(somaticAnnotationService.annotateMutationByHGVSg(hgvsg, referenceGenome, tumorType, evidenceTypes), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by HGVSg.", response = SomaticIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/mutations/byHGVSg",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<SomaticIndicatorQueryResp>> annotateMutationsByHGVSgPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVSgQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/mutations/byHGVSg annotations count: {}", body.size());
        return new ResponseEntity<>(somaticAnnotationService.annotateMutationsByHGVSg(body), HttpStatus.OK);
    }

    // Annotate mutations by HGVSc
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by HGVSc.", response = SomaticIndicatorQueryResp.class, hidden = true)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/mutations/byHGVSc",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<SomaticIndicatorQueryResp> annotateMutationsByHGVScGet(
        @ApiParam(value = "HGVS cDNA format following HGVS nomenclature. Example: EGFR:c.2369C>T", required = true) @RequestParam(value = "hgvsc", required = true) String hgvsc
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        // , @ApiParam(value = "Whether is germline variant", required = false) @RequestParam(value = "germline", defaultValue = "FALSE", required = false) Boolean germline
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        return new ResponseEntity<>(somaticAnnotationService.annotateMutationByHGVSc(hgvsc, referenceGenome, tumorType), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by HGVSc.", response = SomaticIndicatorQueryResp.class, responseContainer = "List", hidden = true)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/mutations/byHGVSc",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<SomaticIndicatorQueryResp>> annotateMutationsByHGVScPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVScQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/mutations/byHGVSc annotations count: {}", body.size());
        return new ResponseEntity<>(somaticAnnotationService.annotateMutationsByHGVSc(body), HttpStatus.OK);
    }

    // Annotate copy number alterations
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate copy number alteration.", response = SomaticIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/copyNumberAlterations",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<SomaticIndicatorQueryResp> annotateCopyNumberAlterationsGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation. Example: BRAF") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Higher priority than hugoSymbol). Example: 673") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Copy number alteration type", required = true) @RequestParam(value = "copyNameAlterationType", required = true) CopyNumberAlterationType copyNameAlterationType
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        // , @ApiParam(value = "Whether is germline variant", required = false) @RequestParam(value = "germline", defaultValue = "FALSE", required = false) Boolean germline
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) throws ApiHttpErrorException {
        return new ResponseEntity<>(somaticAnnotationService.annotateCopyNumberAlteration(hugoSymbol, entrezGeneId, copyNameAlterationType, referenceGenome, tumorType, evidenceTypes), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate copy number alterations.", response = SomaticIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/copyNumberAlterations",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<SomaticIndicatorQueryResp>> annotateCopyNumberAlterationsPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateCopyNumberAlterationQuery> body
    ) throws ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/copyNumberAlterations annotations count: {}", body.size());
        return new ResponseEntity<>(somaticAnnotationService.annotateCopyNumberAlterations(body), HttpStatus.OK);
    }

    // Annotate structural variants
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate structural variant.", response = SomaticIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/structuralVariants",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<SomaticIndicatorQueryResp> annotateStructuralVariantsGet(
        @ApiParam(value = "The gene symbol A used in Human Genome Organisation. Example: ABL1") @RequestParam(value = "hugoSymbolA", required = false) String hugoSymbolA
        , @ApiParam(value = "The entrez gene ID A. (Higher priority than hugoSymbolA) Example: 25") @RequestParam(value = "entrezGeneIdA", required = false) Integer entrezGeneIdA
        , @ApiParam(value = "The gene symbol B used in Human Genome Organisation.Example: BCR ") @RequestParam(value = "hugoSymbolB", required = false) String hugoSymbolB
        , @ApiParam(value = "The entrez gene ID B. (Higher priority than hugoSymbolB) Example: 613") @RequestParam(value = "entrezGeneIdB", required = false) Integer entrezGeneIdB
        , @ApiParam(value = "Structural variant type", required = true) @RequestParam(value = "structuralVariantType", required = true) StructuralVariantType structuralVariantType
        , @ApiParam(value = "Whether is functional fusion", required = true) @RequestParam(value = "isFunctionalFusion", defaultValue = "FALSE", required = true) Boolean isFunctionalFusion
        // , @ApiParam(value = "Whether is germline variant", required = false) @RequestParam(value = "germline", defaultValue = "FALSE", required = false) Boolean germline
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) throws ApiHttpErrorException {
        return new ResponseEntity<>(somaticAnnotationService.annotateStructuralVariant(hugoSymbolA, entrezGeneIdA, hugoSymbolB, entrezGeneIdB, structuralVariantType, isFunctionalFusion, referenceGenome, tumorType, evidenceTypes), HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate structural variants.", response = SomaticIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SomaticIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/structuralVariants",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<SomaticIndicatorQueryResp>> annotateStructuralVariantsPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody(required = true) List<AnnotateStructuralVariantQuery> body
    ) throws ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/structuralVariants annotations count: {}", body.size());
        return new ResponseEntity<>(somaticAnnotationService.annotateStructuralVariants(body), HttpStatus.OK);
    }

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get annotations based on search", response = SomaticAnnotationSearchResult.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/annotation/search",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<LinkedHashSet<SomaticAnnotationSearchResult>> annotationSearchGet(
        @ApiParam(value = "The search query, it could be hugoSymbol, variant or cancer type. At least two characters. Maximum two keywords are supported, separated by space", required = true) @RequestParam(value = "query") String query,
        @ApiParam(value = "The limit of returned result.") @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit
    ) {
        final int DEFAULT_LIMIT = 10;
        final int QUERY_MIN_LENGTH = 2;
        Set<SomaticAnnotationSearchResult> result = new TreeSet<>();
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (query != null && query.length() >= QUERY_MIN_LENGTH) {
            result = annotationSearch(query);
        }

        LinkedHashSet<SomaticAnnotationSearchResult> orderedResult = new LinkedHashSet<>();
        orderedResult.addAll(result);

        return new ResponseEntity<>(MainUtils.getLimit(orderedResult, limit), HttpStatus.OK);
    }

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate samples.", response = SampleQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SampleQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/sample",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<SampleQueryResp>> annotateSamplePost(
        @ApiParam(value = "Sample query. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateSampleQuery> body
    ) throws ApiHttpErrorException, ApiException, org.genome_nexus.ApiException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        LOG.info("POST /annotate/sample annotations count: {}", body.size());

        List<SampleQueryResp> annotatedSamples = new ArrayList<>();
        for (AnnotateSampleQuery query : body) {
            annotatedSamples.add(somaticAnnotationService.annotateSample(query));
        }

        return new ResponseEntity<>(annotatedSamples, HttpStatus.OK);
    }
}
