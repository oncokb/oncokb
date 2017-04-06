package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.model.CancerGene;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
/**
 * Created by Hongxin on 10/28/16.
 */

@Api(value = "/utils", description = "The utils API")
public interface UtilsApi {
    @ApiOperation(value = "", notes = "Get All Annotated Variants.", response = AnnotatedVariant.class, responseContainer = "List", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AnnotatedVariant.class, responseContainer = "List")})
//    @RequestMapping(value = "/utils/allAnnotatedVariants", produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<List<AnnotatedVariant>> utilsAllAnnotatedVariantsGet();

    @ApiOperation(value = "", notes = "Get All Annotated Variants in text file.", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
//    @RequestMapping(value = "/utils/allAnnotatedVariants.txt",
//        method = RequestMethod.GET)
    ResponseEntity<String> utilsAllAnnotatedVariantsTxtGet();

    @ApiOperation(value = "", notes = "Get All Actionable Variants.", response = ActionableGene.class, responseContainer = "List", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ActionableGene.class, responseContainer = "List")})
//    @RequestMapping(value = "/utils/allActionableVariants", produces = {"application/json"},
//        method = RequestMethod.GET)
    ResponseEntity<List<ActionableGene>> utilsAllActionableVariantsGet();


    @ApiOperation(value = "", notes = "Get All Actionable Variants in text file.", tags = "Utils")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
//    @RequestMapping(value = "/utils/allActionableVariants.txt",
//        method = RequestMethod.GET)
    ResponseEntity<String> utilsAllActionableVariantsTxtGet();

    @ApiOperation(value = "", notes = "Get cancer gene list in text file.", tags = "Utils")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/utils/cancerGeneList",
            method = RequestMethod.GET)
    ResponseEntity<List<CancerGene>> utilsCancerGeneListGet();
}
