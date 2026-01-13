package org.mskcc.cbio.oncokb.api.pub.v1;

import static org.mskcc.cbio.oncokb.util.AnnotationSearchUtils.annotationSearch;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.genome_nexus.client.GenomicLocation;
import org.genome_nexus.client.TranscriptConsequenceSummary;
import org.genome_nexus.client.VariantAnnotation;
import org.mskcc.cbio.oncokb.apiModels.annotation.*;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.genomeNexus.TranscriptSummaryAlterationResult;
import org.mskcc.cbio.oncokb.service.JsonResultFactory;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
@Api(tags = "Annotations", description = "Providing annotation services")
@Controller
public class AnnotationsApiController {
    final String EVIDENCE_TYPES_DESCRIPTION = "DEPRECATED. We do not recommend using this parameter and it will eventually be removed.";

    @Autowired
    CacheFetcher cacheFetcher;

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
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            throw new ApiHttpErrorException("entrezGeneId \"" + entrezGeneId + "\"" + " and hugoSymbol \"" + hugoSymbol + "\" are not the same gene.", HttpStatus.BAD_REQUEST);
        } else {
            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);
            Query query = new Query(null, matchedRG, entrezGeneId, hugoSymbol, proteinChange, null, null, tumorType, consequence, proteinStart, proteinEnd, null, false, null, null);
            indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                query.getEntrezGeneId(),
                query.getHugoSymbol(),
                query.getAlteration(),
                null,
                query.getTumorType(),
                query.getConsequence(),
                query.getProteinStart(),
                query.getProteinEnd(),
                null,
                null,
                null,
                null,
                null,
                false,
                new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")),
                false
            );
        }

        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
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
        return new ResponseEntity<>(annotateMutationsByProteinChange(body), HttpStatus.OK);
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
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (StringUtils.isEmpty(genomicLocation)) {
            throw new ApiHttpErrorException("genomicLocation is missing.", HttpStatus.BAD_REQUEST);
        }

        ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);
        
        AnnotateMutationByGenomicChangeQuery query = new AnnotateMutationByGenomicChangeQuery();
        query.setGenomicLocation(genomicLocation);
        query.setReferenceGenome(matchedRG);
        query.setTumorType(tumorType);
        query.setGermline(false);
        query.setEvidenceTypes(new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")));

        indicatorQueryResp = annotateMutationsByGenomicChange(Collections.singletonList(query)).get(0);

        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
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
        return new ResponseEntity<>(annotateMutationsByGenomicChange(body), HttpStatus.OK);
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
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (StringUtils.isEmpty(hgvsg)) {
            throw new ApiHttpErrorException("hgvsg is missing.", HttpStatus.BAD_REQUEST);
        } else {
            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

            if (!AlterationUtils.isValidHgvsg(hgvsg)) {
                throw new ApiHttpErrorException("hgvsg is invalid.", HttpStatus.BAD_REQUEST);
            }
            
            AnnotateMutationByHGVSgQuery query = new AnnotateMutationByHGVSgQuery();
            query.setHgvsg(hgvsg);
            query.setReferenceGenome(matchedRG);
            query.setTumorType(tumorType);
            query.setGermline(false);
            query.setEvidenceTypes(new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")));

            indicatorQueryResp = annotateMutationsByHGVSg(Collections.singletonList(query)).get(0);
        }
        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
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
        return new ResponseEntity<>(annotateMutationsByHGVSg(body), HttpStatus.OK);
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
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (StringUtils.isEmpty(hgvsc)) {
            throw new ApiHttpErrorException("hgvsc is missing.", HttpStatus.BAD_REQUEST);
        } else {
            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

            if (!AlterationUtils.isValidHgvsc(hgvsc)) {
                throw new ApiHttpErrorException("hgvsc is invalid.", HttpStatus.BAD_REQUEST);
            }
            
            AnnotateMutationByHGVScQuery query = new AnnotateMutationByHGVScQuery();
            query.setHgvsc(hgvsc);
            query.setReferenceGenome(matchedRG);
            query.setTumorType(tumorType);
            query.setGermline(false);

            indicatorQueryResp = annotateMutationsByHGVSc(Collections.singletonList(query)).get(0);
        }
        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
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
        return new ResponseEntity<>(annotateMutationsByHGVSc(body), HttpStatus.OK);
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
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            throw new ApiHttpErrorException("entrezGeneId \"" + entrezGeneId + "\"" + " and hugoSymbol \"" + hugoSymbol + "\" are not the same gene.", HttpStatus.BAD_REQUEST);
        } else {
            if (copyNameAlterationType == null) {
                throw new ApiHttpErrorException("copyNameAlterationType is missing.", HttpStatus.BAD_REQUEST);
            }
            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);
            indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
                matchedRG,
                entrezGeneId,
                hugoSymbol,
                StringUtils.capitalize(copyNameAlterationType.name().toLowerCase()),
                null,
                tumorType,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")),
                false);
        }
        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
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
        return new ResponseEntity<>(annotateCopyNumberAlterations(body), HttpStatus.OK);
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
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (structuralVariantType == null) {
            throw new ApiHttpErrorException("structuralVariantType is missing.", HttpStatus.BAD_REQUEST);
        } else if (isFunctionalFusion == null) {
            throw new ApiHttpErrorException("isFunctionalFusion is missing.", HttpStatus.BAD_REQUEST);
        }
        if (entrezGeneIdA != null && hugoSymbolA != null && !GeneUtils.isSameGene(entrezGeneIdA, hugoSymbolA)) {
            throw new ApiHttpErrorException("entrezGeneIdA \"" + entrezGeneIdA + "\"" + " and hugoSymbolA \"" + hugoSymbolA + "\" are not the same gene.", HttpStatus.BAD_REQUEST);
        } else if (entrezGeneIdB != null && hugoSymbolB != null && !GeneUtils.isSameGene(entrezGeneIdB, hugoSymbolB)) {
            throw new ApiHttpErrorException("entrezGeneIdB \"" + entrezGeneIdB + "\"" + " and hugoSymbolB \"" + hugoSymbolB + "\" are not the same gene.", HttpStatus.BAD_REQUEST);
        } else {
            Gene geneA = new Gene();
            try {
                geneA = this.cacheFetcher.findGeneBySymbol(entrezGeneIdA == null ? hugoSymbolA : entrezGeneIdA.toString());
                if (geneA == null) {
                    geneA = new Gene();
                }
            } catch (ApiException e) {
            }
            if (geneA.getEntrezGeneId() == null && StringUtils.isEmpty(geneA.getHugoSymbol())) {
                geneA.setEntrezGeneId(entrezGeneIdA);
                geneA.setHugoSymbol(hugoSymbolA == null ? "" : hugoSymbolA);
            }
            Gene geneB = new Gene();
            try {
                geneB = this.cacheFetcher.findGeneBySymbol(entrezGeneIdB == null ? hugoSymbolB : entrezGeneIdB.toString());
                if (geneB == null) {
                    geneB = new Gene();
                }
            } catch (ApiException e) {
            }
            if (geneB.getEntrezGeneId() == null && StringUtils.isEmpty(geneB.getHugoSymbol())) {
                geneB.setEntrezGeneId(entrezGeneIdB);
                geneB.setHugoSymbol(hugoSymbolB == null ? "" : hugoSymbolB);
            }

            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

            String fusionName = FusionUtils.getFusionName(geneA, geneB);
            indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
                matchedRG, null, fusionName, null, AlterationType.STRUCTURAL_VARIANT.name(), tumorType, isFunctionalFusion ? "fusion" : null, null, null, structuralVariantType, null,
                null, null, null, false, new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")), false);
        }
        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
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
        return new ResponseEntity<>(annotateStructuralVariants(body), HttpStatus.OK);
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

        List<SampleQueryResp> annotatedSamples = new ArrayList<>();
        for (AnnotateSampleQuery query : body) {
            annotatedSamples.add(annotateSample(query));
        }

        return new ResponseEntity<>(annotatedSamples, HttpStatus.OK);
    }

    private SomaticIndicatorQueryResp getIndicatorQueryFromGenomicLocation(
        ReferenceGenome referenceGenome,
        TranscriptSummaryAlterationResult transcriptSummaryAlterationResult,
        String hgvs,
        String tumorType,
        Boolean germline,
        Set<EvidenceType> evidenceTypes
    ) {
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, transcriptSummaryAlterationResult, hgvs);

        SomaticIndicatorQueryResp indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
            referenceGenome,
            query.getEntrezGeneId(),
            query.getHugoSymbol(),
            query.getAlteration(),
            null,
            query.getTumorType(),
            query.getConsequence(),
            query.getProteinStart(),
            query.getProteinEnd(),
            null,
            query.getHgvs(),
            null,
            null,
            null,
            false,
            evidenceTypes,
            false
        );

        // Indicate which transcript was used to generate the annotation
        TranscriptConsequenceSummary summary = transcriptSummaryAlterationResult.getTranscriptConsequenceSummary();
        addTranscriptAndExonToResponse(indicatorQueryResp, summary);
        indicatorQueryResp.getQuery().setHgvsInfo(transcriptSummaryAlterationResult.getMessage());
        return indicatorQueryResp;
    }

    private SomaticIndicatorQueryResp getIndicatorQueryFromHGVS(
        ReferenceGenome referenceGenome,
        TranscriptSummaryAlterationResult transcriptSummaryAlterationResult,
        String hgvs,
        String tumorType,
        Boolean germline,
        Set<EvidenceType> evidenceTypes
    ) {
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, transcriptSummaryAlterationResult, hgvs);

        SomaticIndicatorQueryResp indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
            referenceGenome,
            query.getEntrezGeneId(),
            query.getHugoSymbol(),
            query.getAlteration(),
            null,
            query.getTumorType(),
            query.getConsequence(),
            query.getProteinStart(),
            query.getProteinEnd(),
            null,
            query.getHgvs(),
            null,
            null,
            null,
            false,
            evidenceTypes,
            false
        );

        // Indicate which transcript was used to generate the annotation
        TranscriptConsequenceSummary summary = transcriptSummaryAlterationResult.getTranscriptConsequenceSummary();
        addTranscriptAndExonToResponse(indicatorQueryResp, summary);
        indicatorQueryResp.getQuery().setHgvsInfo(transcriptSummaryAlterationResult.getMessage());
        return indicatorQueryResp;
    }

    private SampleQueryResp annotateSample(AnnotateSampleQuery sample) throws ApiException, org.genome_nexus.ApiException {
        SampleQueryResp annotatedSample = new SampleQueryResp();
        annotatedSample.setId(sample.getId());

        List<SomaticIndicatorQueryResp> structuralVariants = new ArrayList<>();
        List<SomaticIndicatorQueryResp> copyNumberAlterations = new ArrayList<>();
        List<SomaticIndicatorQueryResp> genomicChange = new ArrayList<>();
        List<SomaticIndicatorQueryResp> cDnaChange = new ArrayList<>();
        List<SomaticIndicatorQueryResp> proteinChange = new ArrayList<>();
        List<SomaticIndicatorQueryResp> hgvsg = new ArrayList<>();

        String tumorType = sample.getTumorType();
        annotatedSample.setTumorType(tumorType);
        if (sample.getStructuralVariants() != null) {
            setTumorTypeForQueries(sample.getStructuralVariants(), tumorType);
            structuralVariants = annotateStructuralVariants(sample.getStructuralVariants());
        }
        if (sample.getCopyNumberAlterations() != null) {
            setTumorTypeForQueries(sample.getCopyNumberAlterations(), tumorType);
            copyNumberAlterations = annotateCopyNumberAlterations(sample.getCopyNumberAlterations());
        }
        if (sample.getMutations() != null) {
            if (sample.getMutations().getGenomicChange() != null) {
                setTumorTypeForQueries(sample.getMutations().getGenomicChange(), tumorType);
                genomicChange = annotateMutationsByGenomicChange(sample.getMutations().getGenomicChange());
            }
            if (sample.getMutations().getProteinChange() != null) {
                setTumorTypeForQueries(sample.getMutations().getProteinChange(), tumorType);
                proteinChange = annotateMutationsByProteinChange(sample.getMutations().getProteinChange());
            }
            if (sample.getMutations().getHgvsg() != null) {
                setTumorTypeForQueries(sample.getMutations().getHgvsg(), tumorType);
                hgvsg = annotateMutationsByHGVSg(sample.getMutations().getHgvsg());
            }
            if (sample.getMutations().getcDnaChange() != null) {
                setTumorTypeForQueries(sample.getMutations().getcDnaChange(), tumorType);
                cDnaChange = annotateMutationsByHGVSg(sample.getMutations().getcDnaChange());
            }
        }

        annotatedSample.setStructuralVariants(structuralVariants);
        annotatedSample.setCopyNumberAlterations(copyNumberAlterations);
        annotatedSample.setMutations(Stream.of(genomicChange, cDnaChange, proteinChange, hgvsg)
            .flatMap(Collection::stream)
            .collect(Collectors.toList())
        );
        return annotatedSample;
    }

    private <T extends AnnotationQuery> void setTumorTypeForQueries(List<T> queries, String tumorType) {
        if (Strings.isEmpty(tumorType)) {
            return;
        }

        for (AnnotationQuery query : queries) {
            query.setTumorType(tumorType);
        }
    }

    private List<SomaticIndicatorQueryResp> annotateStructuralVariants(List<AnnotateStructuralVariantQuery> structuralVariants) {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        for (AnnotateStructuralVariantQuery query : structuralVariants) {
            Gene geneA = new Gene();
            if (query.getGeneA() != null) {
                try {
                    geneA = this.cacheFetcher.findGeneBySymbol(
                        query.getGeneA().getEntrezGeneId() != null ?
                            query.getGeneA().getEntrezGeneId().toString() :
                            query.getGeneA().getHugoSymbol()
                    );
                    if (geneA == null) {
                        geneA = new Gene();
                    }
                } catch (ApiException e) {
                }
            }
            if (StringUtils.isEmpty(geneA.getHugoSymbol()) && geneA.getEntrezGeneId() == null && query.getGeneA() != null) {
                geneA.setHugoSymbol(query.getGeneA().getHugoSymbol() == null ? "" : query.getGeneA().getHugoSymbol());
                geneA.setEntrezGeneId(query.getGeneA().getEntrezGeneId());
            }

            Gene geneB = new Gene();
            if (query.getGeneB() != null) {
                try {
                    geneB = this.cacheFetcher.findGeneBySymbol(
                        query.getGeneB().getEntrezGeneId() != null ?
                            query.getGeneB().getEntrezGeneId().toString() :
                            query.getGeneB().getHugoSymbol()
                    );
                    if (geneB == null) {
                        geneB = new Gene();
                    }
                } catch (ApiException e) {
                }
            }
            if (StringUtils.isEmpty(geneB.getHugoSymbol()) && geneB.getEntrezGeneId() == null && query.getGeneB() != null) {
                geneB.setHugoSymbol(query.getGeneB().getHugoSymbol() == null ? "" : query.getGeneB().getHugoSymbol());
                geneB.setEntrezGeneId(query.getGeneB().getEntrezGeneId());
            }

            String fusionName = FusionUtils.getFusionName(geneA, geneB);

            SomaticIndicatorQueryResp resp = this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                null,
                fusionName,
                null,
                AlterationType.STRUCTURAL_VARIANT.name(),
                query.getTumorType(),
                query.getFunctionalFusion() ? "fusion" : "",
                null,
                null,
                query.getStructuralVariantType(),
                null,
                null,
                null,
                null,
                false, 
                query.getEvidenceTypes(), 
                false
            );
            resp.getQuery().setId(query.getId());
            result.add(resp);
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateCopyNumberAlterations(List<AnnotateCopyNumberAlterationQuery> copyNumberAlterations) {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        for (AnnotateCopyNumberAlterationQuery query : copyNumberAlterations) {
            Gene gene = new Gene();
            if (query.getGene() != null) {
                try {
                    gene = this.cacheFetcher.findGeneBySymbol(
                        query.getGene().getEntrezGeneId() != null ?
                            query.getGene().getEntrezGeneId().toString() :
                            query.getGene().getHugoSymbol()
                    );
                    if (gene == null) {
                        gene = new Gene();
                        gene.setEntrezGeneId(query.getGene().getEntrezGeneId());
                        gene.setHugoSymbol(query.getGene().getHugoSymbol());
                    }
                } catch (ApiException e) {
                }
            }
            SomaticIndicatorQueryResp resp = this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                gene.getEntrezGeneId(),
                gene.getHugoSymbol(),
                StringUtils.capitalize(query.getCopyNameAlterationType().name().toLowerCase()),
                null,
                query.getTumorType(),
                null, 
                null, 
                null, 
                null,
                null, 
                null,
                null,
                null,
                false, 
                query.getEvidenceTypes(),
                false
            );
            resp.getQuery().setId(query.getId());
            result.add(resp);
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByGenomicChange(List<AnnotateMutationByGenomicChangeQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByGenomicChangeQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByGenomicChangeQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByGenomicChangeQuery query = mutations.get(i);
            ReferenceGenome referenceGenome = query.getReferenceGenome();
            if (referenceGenome == null) {
                query.setReferenceGenome(ReferenceGenome.GRCh37);
            }
            if (referenceGenome == ReferenceGenome.GRCh38) {
                grch38Map.put(i, grch38Queries.size());
                grch38Queries.add(query);
            } else {
                grch37Map.put(i, grch37Queries.size());
                grch37Queries.add(query);
            }
        }

        List<SomaticIndicatorQueryResp> grch37Alts = annotateMutationsByGenomicChange(ReferenceGenome.GRCh37, grch37Queries);
        List<SomaticIndicatorQueryResp> grch38Alts = annotateMutationsByGenomicChange(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByGenomicChangeQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByGenomicChange(ReferenceGenome referenceGenome, List<AnnotateMutationByGenomicChangeQuery> queries) throws ApiException, org.genome_nexus.ApiException {
        List<GenomicLocation> queriesToGN = new ArrayList<>();
        Map<String, Integer> queryIndexMap = new HashMap<>();
        for (AnnotateMutationByGenomicChangeQuery query : queries) {
            GenomicLocation genomicLocation = GenomeNexusUtils.convertGenomicLocation(query.getGenomicLocation());
            if (this.cacheFetcher.genomicLocationShouldBeAnnotated(genomicLocation, referenceGenome)) {
                if (!queryIndexMap.containsKey(query.getGenomicLocation())) {
                    queryIndexMap.put(query.getGenomicLocation(), queriesToGN.size());
                    queriesToGN.add(genomicLocation);
                }
            }
        }

        List<org.genome_nexus.client.VariantAnnotation> variantAnnotations = GenomeNexusUtils.getGenomicLocationVariantsAnnotation(queriesToGN, referenceGenome);
        if(variantAnnotations.size() != queriesToGN.size()){
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }
        
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<Alteration> allAlterations =  AlterationUtils.getAllAlterations();
        for (AnnotateMutationByGenomicChangeQuery query : queries) {
            SomaticIndicatorQueryResp indicatorQueryResp = null;
            if (queryIndexMap.containsKey(query.getGenomicLocation())) {
                VariantAnnotation variantAnnotation = variantAnnotations.get(queryIndexMap.get(query.getGenomicLocation()));
                List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                    Collections.singletonList(variantAnnotation), 
                    referenceGenome
                );
                TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty() 
                    ? new TranscriptSummaryAlterationResult() 
                    : annotatedAlteration.get(0);
                indicatorQueryResp = getIndicatorQueryForCuratedHgvs(
                    query, 
                    query.isGermline(),
                    variantAnnotation.getHgvsg(), 
                    selectedAnnotatedAlteration, 
                    referenceGenome, 
                    allAlterations
                );
                
                if (indicatorQueryResp == null && !query.isGermline()) {
                    indicatorQueryResp = this.getIndicatorQueryFromGenomicLocation(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        query.getGenomicLocation(),
                        query.getTumorType(),
                        query.getGermline(),
                        new HashSet<>(query.getEvidenceTypes())
                    );
                    indicatorQueryResp.getQuery().setHgvsInfo(selectedAnnotatedAlteration.getMessage());   
                }
            } 

            if (indicatorQueryResp == null) {
                indicatorQueryResp = this.getIndicatorQueryFromGenomicLocation(
                    query.getReferenceGenome(),
                    new TranscriptSummaryAlterationResult(),
                    query.getGenomicLocation(),
                    query.getTumorType(),
                    query.getGermline(),
                    query.getEvidenceTypes()
                );
            }

            indicatorQueryResp.getQuery().setId(query.getId());
            result.add(indicatorQueryResp);
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByProteinChange(List<AnnotateMutationByProteinChangeQuery> mutations) {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        for (AnnotateMutationByProteinChangeQuery query : mutations) {
            SomaticIndicatorQueryResp resp = this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                query.getGene() == null ? null : query.getGene().getEntrezGeneId(),
                query.getGene() == null ? null : query.getGene().getHugoSymbol(),
                query.getAlteration(),
                null,
                query.getTumorType(),
                query.getConsequence(),
                query.getProteinStart(),
                query.getProteinEnd(),
                null,
                null,
                null,
                null,
                null,
                false,
                query.getEvidenceTypes(),
                false
            );
            resp.getQuery().setId(query.getId());
            result.add(resp);
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByHGVSg(List<AnnotateMutationByHGVSgQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByHGVSgQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByHGVSgQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVSgQuery query = mutations.get(i);
            ReferenceGenome referenceGenome = query.getReferenceGenome();
            if (referenceGenome == null) {
                referenceGenome = ReferenceGenome.GRCh37;
            }
            query.setReferenceGenome(referenceGenome);
            if (referenceGenome == ReferenceGenome.GRCh38) {
                grch38Map.put(i, grch38Queries.size());
                grch38Queries.add(query);
            } else {
                grch37Map.put(i, grch37Queries.size());
                grch37Queries.add(query);
            }
        }

        List<SomaticIndicatorQueryResp> grch37Alts = annotateMutationsByHGVSg(ReferenceGenome.GRCh37, grch37Queries);
        List<SomaticIndicatorQueryResp> grch38Alts = annotateMutationsByHGVSg(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVSgQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByHGVSg(ReferenceGenome referenceGenome, List<AnnotateMutationByHGVSgQuery> queries) throws ApiException, org.genome_nexus.ApiException {
        List<String> queriesToGN = new ArrayList<>();
        Map<String, Integer> queryIndexMap = new HashMap<>();
        for (AnnotateMutationByHGVSgQuery query : queries) {
            String hgvsg = query.getHgvsg();
            if (this.cacheFetcher.hgvsgShouldBeAnnotated(hgvsg, referenceGenome)) {
                if (!queryIndexMap.containsKey(query.getHgvsg())) {
                    queryIndexMap.put(hgvsg, queriesToGN.size());
                    queriesToGN.add(hgvsg);
                }
            }
        }

        List<org.genome_nexus.client.VariantAnnotation> variantAnnotations = GenomeNexusUtils.getHgvsVariantsAnnotation(queriesToGN, referenceGenome);
        if(variantAnnotations.size() != queriesToGN.size()){
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }
        
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<Alteration> allAlterations = AlterationUtils.getAllAlterations();
        for (AnnotateMutationByHGVSgQuery query : queries) {
            SomaticIndicatorQueryResp indicatorQueryResp = null;
            if (queryIndexMap.containsKey(query.getHgvsg())) {
                VariantAnnotation variantAnnotation = variantAnnotations.get(queryIndexMap.get(query.getHgvsg()));
                List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                    Collections.singletonList(variantAnnotation), 
                    referenceGenome
                );
                TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty() 
                    ? new TranscriptSummaryAlterationResult() 
                    : annotatedAlteration.get(0);
                indicatorQueryResp = getIndicatorQueryForCuratedHgvs(
                    query, 
                    query.isGermline(),
                    variantAnnotation.getHgvsg(), 
                    selectedAnnotatedAlteration, 
                    referenceGenome, 
                    allAlterations
                );

                if (indicatorQueryResp == null && !query.isGermline()) {
                    indicatorQueryResp = this.getIndicatorQueryFromHGVS(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        variantAnnotation.getHgvsg(),
                        query.getTumorType(),
                        query.getGermline(),
                        new HashSet<>(query.getEvidenceTypes())
                    );
                    indicatorQueryResp.getQuery().setHgvsInfo(selectedAnnotatedAlteration.getMessage());
                }
            } 
            
            if (indicatorQueryResp == null) {
                indicatorQueryResp = this.getIndicatorQueryFromHGVS(
                    query.getReferenceGenome(),
                    new TranscriptSummaryAlterationResult(),
                    query.getHgvsg(),
                    query.getTumorType(),
                    query.getGermline(),
                    query.getEvidenceTypes()
                );
            }
            indicatorQueryResp.getQuery().setId(query.getId());
            result.add(indicatorQueryResp);
        }
        return result;
    }

    private SomaticIndicatorQueryResp getIndicatorQueryForCuratedHgvs(
        AnnotationQuery query,
        Boolean germline,
        String hgvsg,
        TranscriptSummaryAlterationResult selectedAnnotatedAlteration,
        ReferenceGenome referenceGenome, 
        List<Alteration> allAlterations
    ) throws org.genome_nexus.ApiException {
        Alteration alteration = AlterationUtils.findAlterationWithGeneticType(
            referenceGenome,
            hgvsg,
            allAlterations,
            germline
        );
        if (alteration != null && alteration.getForGermline() == germline) {
            return this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                null,
                alteration.getGene().getHugoSymbol(),
                hgvsg,
                null,
                query.getTumorType(),
                null,
                null,
                null,
                null,
                hgvsg,
                null,
                null,
                null,
                false,
                query.getEvidenceTypes(),
                false
            );
        }

        alteration = null;
        String hgvsc = null;
        if (selectedAnnotatedAlteration.getTranscriptConsequenceSummary() != null) {
            hgvsc = selectedAnnotatedAlteration.getTranscriptConsequenceSummary().getHgvsc();
        }
        if (StringUtils.isNotEmpty(hgvsc)) {
            alteration = null;
            String[] hgvscParts = hgvsc.split(":");
            if (hgvscParts.length == 2) {
                hgvsc = hgvscParts[1];
                alteration = AlterationUtils.findAlterationWithGeneticType(
                    referenceGenome,
                    hgvsc,
                    allAlterations,
                    germline
                );
            }
        }
        if (alteration != null && alteration.getForGermline() == germline) {
            return this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                null,
                alteration.getGene().getHugoSymbol(),
                hgvsc,
                null,
                query.getTumorType(),
                null,
                null,
                null,
                null,
                hgvsg,
                null,
                null,
                null,
                false,
                query.getEvidenceTypes(),
                false
            );
        }

        return null;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByHGVSc(List<AnnotateMutationByHGVScQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByHGVScQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByHGVScQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVScQuery query = mutations.get(i);
            ReferenceGenome referenceGenome = query.getReferenceGenome();
            if (referenceGenome == null) {
                referenceGenome = ReferenceGenome.GRCh37;
            }
            query.setReferenceGenome(referenceGenome);
            if (referenceGenome == ReferenceGenome.GRCh38) {
                grch38Map.put(i, grch38Queries.size());
                grch38Queries.add(query);
            } else {
                grch37Map.put(i, grch37Queries.size());
                grch37Queries.add(query);
            }
        }

        List<SomaticIndicatorQueryResp> grch37Alts = annotateMutationsByHGVSc(ReferenceGenome.GRCh37, grch37Queries);
        List<SomaticIndicatorQueryResp> grch38Alts = annotateMutationsByHGVSc(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVScQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByHGVSc(ReferenceGenome referenceGenome, List<AnnotateMutationByHGVScQuery> queries) throws ApiException, org.genome_nexus.ApiException {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();

        List<String> queriesToGN = new ArrayList<>();
        Map<String, Integer> queryToGNIndexMap = new HashMap<>();
        AnnotateMutationByHGVScQuery[] resultIndexToQuery = new AnnotateMutationByHGVScQuery[queries.size()];

        List<Alteration> allAlterations = AlterationUtils.getAllAlterations();

        for (int i = 0; i < queries.size(); i++) {
            AnnotateMutationByHGVScQuery query = queries.get(i);
            String hgvsc = query.getHgvsc();

            if (this.cacheFetcher.hgvscShouldBeAnnotated(hgvsc)) {
                Alteration alteration = AlterationUtils.findAlterationWithGeneticType(
                    referenceGenome,
                    query.getAlteration(),
                    allAlterations,
                    query.isGermline()
                );

                if (query.isGermline() || (alteration != null && alteration.getForGermline() == false)) {
                    SomaticIndicatorQueryResp resp = this.cacheFetcher.processQuerySomatic(
                        query.getReferenceGenome(),
                        null,
                        query.getGene(),
                        query.getAlteration(),
                        null,
                        query.getTumorType(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        query.getEvidenceTypes(),
                        false
                    );
                    resp.getQuery().setId(query.getId());
                    result.add(resp);
                } else {
                    if (!queryToGNIndexMap.containsKey(query.getHgvsc())) {
                        queryToGNIndexMap.put(hgvsc, queriesToGN.size());
                        Gene gene = GeneUtils.getGeneByAlias(query.getGene());
                        if (gene != null) {
                            queriesToGN.add(gene.getHugoSymbol() + ":" + query.getAlteration());
                        } else {
                            queriesToGN.add(hgvsc);
                        }
                    }
                    resultIndexToQuery[result.size()] = query;
                    result.add(null);
                }
            } else {
                SomaticIndicatorQueryResp resp = this.getIndicatorQueryFromHGVS(
                    query.getReferenceGenome(),
                    new TranscriptSummaryAlterationResult(),
                    query.getHgvsc(),
                    query.getTumorType(),
                    query.getGermline(),
                    query.getEvidenceTypes()
                );
                resp.getQuery().setId(query.getId());
                result.add(resp);
            }
        }

        List<org.genome_nexus.client.VariantAnnotation> variantAnnotations = GenomeNexusUtils.getHgvsVariantsAnnotation(queriesToGN, referenceGenome);
        if(variantAnnotations.size() != queriesToGN.size()){
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }

        for (int i = 0; i < result.size(); i++) {
            SomaticIndicatorQueryResp indicatorQueryResp = null;
            if (result.get(i) == null) {
                AnnotateMutationByHGVScQuery query = resultIndexToQuery[i];
                if (queryToGNIndexMap.containsKey(query.getHgvsc())) {
                    VariantAnnotation variantAnnotation = variantAnnotations.get(queryToGNIndexMap.get(query.getHgvsc()));
                    List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                        Collections.singletonList(variantAnnotation), 
                        referenceGenome
                    );
                    TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty() 
                        ? new TranscriptSummaryAlterationResult() 
                        : annotatedAlteration.get(0);
                    indicatorQueryResp = this.getIndicatorQueryFromHGVS(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        variantAnnotation.getHgvsg(),
                        query.getTumorType(),
                        query.getGermline(),
                        new HashSet<>(query.getEvidenceTypes())
                    );
                    indicatorQueryResp.getQuery().setHgvsInfo(selectedAnnotatedAlteration.getMessage());
                    indicatorQueryResp.getQuery().setId(query.getId());
                }
                result.set(i, indicatorQueryResp);
            }
        }
        return result;
    }

    private void addTranscriptAndExonToResponse(SomaticIndicatorQueryResp response, TranscriptConsequenceSummary summary) {
        if (summary != null) {
            if (StringUtils.isNotEmpty(summary.getTranscriptId())) {
                response.getQuery().setCanonicalTranscript(summary.getTranscriptId());
            }
            if (StringUtils.isNotEmpty(summary.getExon())) {
                response.setExon(StringUtils.substringBefore(summary.getExon(), "/"));
            }
        }
    }
}
