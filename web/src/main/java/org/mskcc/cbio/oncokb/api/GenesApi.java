package org.mskcc.cbio.oncokb.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.RespMeta;
import org.mskcc.cbio.oncokb.model.ShortGene;
import org.mskcc.cbio.oncokb.response.ApiGenes;
import org.mskcc.cbio.oncokb.util.ShortGeneUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/api/public/v1/genes", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/genes", description = "the genes API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class GenesApi {


    @ApiOperation(value = "", notes = "Get list of current existed genes.", response = ApiGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiGenes> genesGet(@ApiParam(value = "The highest level of gene") @RequestParam(value = "level", required = false) String level


    ) throws NotFoundException {

        ApiGenes instance = new ApiGenes();

        Set<ShortGene> genes = ShortGeneUtils.getAllShortGenes();
        instance.setData(genes);

        RespMeta respMeta = new RespMeta();
        respMeta.setCode(200);
        instance.setRespMeta(respMeta);

        return new ResponseEntity<ApiGenes>(instance, HttpStatus.OK);
    }


    @ApiOperation(value = "", notes = "Get gene info.", response = ApiGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/{hugoSymbol}",
        produces = {"application/json"},
        consumes = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiGenes> genesHugoSymbolGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("hugoSymbol") String hugoSymbol

    )
        throws NotFoundException {
        // do some magic!
        return new ResponseEntity<ApiGenes>(HttpStatus.OK);
    }


}
