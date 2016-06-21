package org.mskcc.cbio.oncokb.api.legacy;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.RespMeta;
import org.mskcc.cbio.oncokb.response.ApiGene;
import org.mskcc.cbio.oncokb.response.ApiGenes;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

//@Controller
//@RequestMapping(value = "/public-api/v1/genes", produces = {APPLICATION_JSON_VALUE})
//@Api(value = "/genes", description = "the genes API")
//@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class GenesApi {


    @ApiOperation(value = "", notes = "Get list of current existed genes.", response = ApiGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
//    @RequestMapping(value = "",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    public ResponseEntity<ApiGenes> genesGet(@ApiParam(value = "The highest level of gene") @RequestParam(value = "level", required = false) String level


    ) throws NotFoundException {

        ApiGenes instance = new ApiGenes();

        Set<Gene> genes = GeneUtils.getAllGenes();
        instance.setData(genes);

        RespMeta meta = new RespMeta();
        meta.setCode(HttpStatus.OK.value());
        instance.setRespMeta(meta);

        return new ResponseEntity<ApiGenes>(instance, HttpStatus.OK);
    }


    @ApiOperation(value = "", notes = "Get gene info.", response = ApiGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
//    @RequestMapping(value = "/{entrezGeneId}",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    public ResponseEntity<ApiGene> genesHugoSymbolGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId

    ) throws NotFoundException {
        ApiGene apiGene = new ApiGene();

        Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);
        apiGene.setData(gene);

        RespMeta meta = new RespMeta();
        meta.setCode(HttpStatus.OK.value());
        apiGene.setRespMeta(meta);

        return new ResponseEntity<ApiGene>(apiGene, HttpStatus.OK);
    }

}
