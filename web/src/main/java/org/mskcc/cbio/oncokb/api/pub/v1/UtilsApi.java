package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.apiModels.ApiErrorResp;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Hongxin on 10/28/16.
 */

@Api(value = "/utils", description = "The utils API")
public interface UtilsApi {
    @ApiOperation(value = "", notes = "Get All Annotated Variants.", response = AnnotatedVariant.class, responseContainer = "List", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AnnotatedVariant.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
    @RequestMapping(value = "/utils/allAnnotatedVariants", produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<ApiListResp> utilsAllAnnotatedVariantsGet();

    @ApiOperation(value = "", notes = "Get All Annotated Variants in text file.", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/utils/allAnnotatedVariants.txt",
        method = RequestMethod.GET)
    ResponseEntity<String> utilsAllAnnotatedVariantsTxtGet();

    @ApiOperation(value = "", notes = "Get All Actionable Genes.", response = ActionableGene.class, responseContainer = "List", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ActionableGene.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiErrorResp.class)})
    @RequestMapping(value = "/utils/allActionableGenes", produces = {"application/json", "text/tsv"},
        method = RequestMethod.GET)
    ResponseEntity<ApiListResp> utilsAllActionableGenesGet();


    @ApiOperation(value = "", notes = "Get All Annotated Variants in text file.", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/utils/allActionableGenes.txt",
        method = RequestMethod.GET)
    ResponseEntity<String> utilsAllActionableGenesTxtGet();
}
