package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.apiModels.CuratedGene;
import org.mskcc.cbio.oncokb.apiModels.VariantOfUnknownSignificance;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.model.CancerGene;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

import static org.mskcc.cbio.oncokb.api.pub.v1.Constants.INCLUDE_EVIDENCE;
import static org.mskcc.cbio.oncokb.api.pub.v1.Constants.VERSION;

// It was tags=Utils. But temporally name it Cancer Genes so the Utils tag would not show up with empty content
@Api(tags = "Cancer Genes", description = "Cancer Genes")
public interface UtilsApi {
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get All Annotated Variants.", response = AnnotatedVariant.class, responseContainer = "List", tags = {"Variants"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AnnotatedVariant.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allAnnotatedVariants", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<AnnotatedVariant>> utilsAllAnnotatedVariantsGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    );

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get All Annotated Variants in text file.", tags = {"Variants"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allAnnotatedVariants.txt",
        produces = "text/plain; charset=UTF-8",
        method = RequestMethod.GET)
    ResponseEntity<String> utilsAllAnnotatedVariantsTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    );

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get All Variants of Unknown Significance.", response = VariantOfUnknownSignificance.class, responseContainer = "List", tags = {"Variants"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VariantOfUnknownSignificance.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allVariantsOfUnknownSignificance", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<VariantOfUnknownSignificance>> utilsAllVariantsOfUnknownSignificanceGet();

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get All Variants of Unknown Significance in text file.", tags = {"Variants"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allVariantsOfUnknownSignificance.txt",
        produces = "text/plain; charset=UTF-8",
        method = RequestMethod.GET)
    ResponseEntity<String> utilsAllVariantsOfUnknownSignificanceTxtGet();

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get All Actionable Variants.", response = ActionableGene.class, responseContainer = "List", tags = {"Variants"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ActionableGene.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allActionableVariants", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<ActionableGene>> utilsAllActionableVariantsGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    );


    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get All Actionable Variants in text file.", tags = {"Variants"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allActionableVariants.txt",
        produces = "text/plain; charset=UTF-8",
        method = RequestMethod.GET)
    ResponseEntity<String> utilsAllActionableVariantsTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    );

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get cancer gene list", tags = {"Cancer Genes"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/cancerGeneList",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<CancerGene>> utilsCancerGeneListGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) throws ApiException, IOException;

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get cancer gene list in text file.", tags = {"Cancer Genes"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/cancerGeneList.txt",
        produces = "text/plain; charset=UTF-8",
        method = RequestMethod.GET)
    ResponseEntity<String> utilsCancerGeneListTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
    ) throws ApiException, IOException;

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get list of genes OncoKB curated", tags = {"Cancer Genes"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allCuratedGenes",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<List<CuratedGene>> utilsAllCuratedGenesGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
        , @ApiParam(value = INCLUDE_EVIDENCE, defaultValue = "TRUE") @RequestParam(value = "includeEvidence", required = false, defaultValue = "TRUE") Boolean includeEvidence
    );

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get list of genes OncoKB curated in text file.", tags = {"Cancer Genes"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 503, message = "Service Unavailable")
    })
    @RequestMapping(value = "/utils/allCuratedGenes.txt",
        produces = "text/plain; charset=UTF-8",
        method = RequestMethod.GET)
    ResponseEntity<String> utilsAllCuratedGenesTxtGet(
        @ApiParam(value = VERSION) @RequestParam(value = "version", required = false) String version
        , @ApiParam(value = INCLUDE_EVIDENCE, defaultValue = "TRUE") @RequestParam(value = "includeEvidence", required = false, defaultValue = "TRUE") Boolean includeEvidence
    );
}
