package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.genome_nexus.ApiException;
import org.genome_nexus.client.EnsemblTranscript;
import org.mskcc.cbio.oncokb.apiModels.TranscriptMatchResult;
import org.mskcc.cbio.oncokb.apiModels.TranscriptPair;
import org.mskcc.cbio.oncokb.apiModels.TranscriptResult;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import static org.mskcc.cbio.oncokb.util.GenomeNexusUtils.getCanonicalEnsemblTranscript;
import static org.mskcc.cbio.oncokb.util.GenomeNexusUtils.matchTranscript;

/**1
 * Controller to authenticate users.
 */
@RestController
@Api(tags = "Transcript", description = "The transcript API")
public class PrivateGenomeNexusController {

    @ApiOperation(value = "", notes = "Get transcript info in both GRCh37 and 38.", response = TranscriptResult.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/transcripts/{hugoSymbol}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<TranscriptResult> getTranscript(
        @PathVariable String hugoSymbol
    ) throws ApiException {
        EnsemblTranscript grch37Transcript = getCanonicalEnsemblTranscript(hugoSymbol, ReferenceGenome.GRCh37);
        TranscriptPair transcriptPair = new TranscriptPair();
        transcriptPair.setReferenceGenome(ReferenceGenome.GRCh37);
        transcriptPair.setTranscript(grch37Transcript.getTranscriptId());
        TranscriptMatchResult transcriptMatchResult = matchTranscript(transcriptPair, ReferenceGenome.GRCh38, hugoSymbol);

        TranscriptResult transcriptResult = new TranscriptResult();
        transcriptResult.setGrch37Transcript(transcriptMatchResult.getOriginalEnsemblTranscript());
        transcriptResult.setGrch38Transcript(transcriptMatchResult.getTargetEnsemblTranscript());
        transcriptResult.setNote(transcriptMatchResult.getNote());

        return new ResponseEntity<>(transcriptResult, HttpStatus.OK);
    }


}
