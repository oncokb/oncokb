package org.mskcc.cbio.oncokb.api.legacy;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.Gene;
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

@Controller
@RequestMapping(value = "/public-api/v1/genes", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/genes", description = "the genes API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class GenesApi {


    @ApiOperation(value = "", notes = "Get list of current existed genes.", response = Gene.class, responseContainer = "Set")
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Set<Gene>> genesGet(@ApiParam(value = "The highest level of gene") @RequestParam(value = "level", required = false) String level


    ) throws NotFoundException {
        Set<Gene> genes = GeneUtils.getAllGenes();
        return new ResponseEntity<>(genes, HttpStatus.OK);
    }


    @ApiOperation(value = "", notes = "Get gene info.", response = Gene.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/{entrezGeneId}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Gene> genesHugoSymbolGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId

    ) throws NotFoundException {
        Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);
        return new ResponseEntity<>(gene, HttpStatus.OK);
    }

}
