package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.genome_nexus.ApiException;
import org.genome_nexus.client.EnsemblTranscript;
import org.mskcc.cbio.oncokb.apiModels.TranscriptMatchResult;
import org.mskcc.cbio.oncokb.apiModels.TranscriptPair;
import org.mskcc.cbio.oncokb.apiModels.TranscriptResult;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByGenomicChangeQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByHGVSgQuery;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpError;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.genomeNexusPreAnnotations.GenomeNexusAnnotatedVariantInfo;
import org.mskcc.cbio.oncokb.util.GenomeNexusUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import static org.mskcc.cbio.oncokb.util.GenomeNexusUtils.getCanonicalEnsemblTranscript;
import static org.mskcc.cbio.oncokb.util.GenomeNexusUtils.matchTranscript;

import java.util.ArrayList;
import java.util.List;

/**1
 * Controller to authenticate users.
 */
@RestController
@Api(tags = "Transcript", description = "The transcript API")
public class PrivateGenomeNexusController {

    @Autowired CacheFetcher cacheFetcher;

    @ApiOperation(value = "", notes = "Get transcript info in both GRCh37 and 38.", response = TranscriptResult.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/transcripts/{hugoSymbol}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<TranscriptResult> getTranscript(
        @PathVariable String hugoSymbol
    ) throws ApiException {
        TranscriptResult transcriptResult = new TranscriptResult();

        EnsemblTranscript grch37Transcript = getCanonicalEnsemblTranscript(hugoSymbol, ReferenceGenome.GRCh37);
        if (grch37Transcript != null) {
            TranscriptPair transcriptPair = new TranscriptPair();
            transcriptPair.setReferenceGenome(ReferenceGenome.GRCh37);
            transcriptPair.setTranscript(grch37Transcript.getTranscriptId());
            TranscriptMatchResult transcriptMatchResult = matchTranscript(transcriptPair, ReferenceGenome.GRCh38, hugoSymbol);

            transcriptResult.setGrch37Transcript(transcriptMatchResult.getOriginalEnsemblTranscript());
            transcriptResult.setGrch38Transcript(transcriptMatchResult.getTargetEnsemblTranscript());
            transcriptResult.setNote(transcriptMatchResult.getNote());
        }

        return new ResponseEntity<>(transcriptResult, HttpStatus.OK);
    }

    @ApiOperation(value = "", notes = "Fetch Genome Nexus variant info by HGVSg", response = GenomeNexusAnnotatedVariantInfo.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GenomeNexusAnnotatedVariantInfo.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/fetchGnVariants/byHGVSg",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<GenomeNexusAnnotatedVariantInfo>> fetchGenomeNexusVariantInfoByHGVSgPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVSgQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        List<GenomeNexusAnnotatedVariantInfo> result = new ArrayList<>();

        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        } else {
            for (AnnotateMutationByHGVSgQuery query : body) {
                GenomeNexusAnnotatedVariantInfo resp = GenomeNexusUtils.getAnnotatedVariantFromGenomeNexus(GNVariantAnnotationType.HGVS_G, query.getHgvsg(), query.getReferenceGenome());
                result.add(resp);
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "", notes = "Fetch Genome Nexus variant info by genomic change", response = GenomeNexusAnnotatedVariantInfo.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GenomeNexusAnnotatedVariantInfo.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/fetchGnVariants/byGenomicChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<GenomeNexusAnnotatedVariantInfo>> fetchGenomeNexusVariantInfoByGenomicChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByGenomicChangeQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        List<GenomeNexusAnnotatedVariantInfo> result = new ArrayList<>();

        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        } else {
            for (AnnotateMutationByGenomicChangeQuery query : body) {
                GenomeNexusAnnotatedVariantInfo resp = GenomeNexusUtils.getAnnotatedVariantFromGenomeNexus(GNVariantAnnotationType.GENOMIC_LOCATION, query.getGenomicLocation(), query.getReferenceGenome());
                result.add(resp);
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "", notes = "cache Genome Nexus variant info")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/cacheGnVariantInfo",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<Void> cacheGenomeNexusVariantInfoPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<GenomeNexusAnnotatedVariantInfo> body
    ) throws ApiException, org.genome_nexus.ApiException, IllegalStateException, ApiHttpErrorException {

        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        } else {
            for (GenomeNexusAnnotatedVariantInfo query : body) {
                cacheFetcher.cacheAlterationFromGenomeNexus(query);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
