package org.mskcc.cbio.oncokb.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.RespMeta;
import org.mskcc.cbio.oncokb.response.ApiGenes;
import org.mskcc.cbio.oncokb.response.ApiSearchEvidences;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/api/public/v1/search", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/search", description = "the search API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class SearchApi {
    final Set<EvidenceType> SUPPORTED_EVIDENCE_TYPES = new HashSet<EvidenceType>(){{
        add(EvidenceType.GENE_SUMMARY);
        add(EvidenceType.GENE_BACKGROUND);
    }};

    @ApiOperation(value = "", notes = "Get list of evidences.", response = ApiSearchEvidences.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/evidences",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiSearchEvidences> searchEvidencesGet(
        @ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "") @RequestParam(value = "type", required = false) String type
    ) throws NotFoundException {
        //We only support GENE_SUMMARY and GENE_BACKGROUND at this moment for public API.
        ApiSearchEvidences apiSearchEvidences = new ApiSearchEvidences();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;
        
        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            if(gene != null) {
                Set<Evidence> evidences = new HashSet<>();
                Set<EvidenceType> types = new HashSet<>();

                if (type != null) {
                    for (String evidenceType : type.split(",")) {
                        EvidenceType et = EvidenceType.valueOf(evidenceType);
                        if (SUPPORTED_EVIDENCE_TYPES.contains(et)) {
                            types.add(et);
                        }
                    }
                }

                Map<Gene, Set<Evidence>> result = EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), types);
                if (result != null && result.containsKey(gene)) {
                    evidences = result.get(gene);
                }

                apiSearchEvidences.setData(evidences);
            }else {
                status = HttpStatus.NO_CONTENT;
            }
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        meta.setCode(status.value());
        meta.setError_message(status.getReasonPhrase());

        apiSearchEvidences.setRespMeta(meta);
        return new ResponseEntity<ApiSearchEvidences>(apiSearchEvidences, status);
    }


    @ApiOperation(value = "", notes = "Search to find gene. Code 204 will be returned in the META if no gene matched.", response = ApiGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/gene/{hugoSymbol}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiGenes> searchGeneHugoSymbolGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation.", required = true) @PathVariable("hugoSymbol") String hugoSymbol,
        @ApiParam(value = "Find the exact match with query.", defaultValue = "false") @RequestParam(value = "exactMatch", required = false, defaultValue = "false") Boolean exactMatch


    ) throws NotFoundException {
        // do some magic!
        return new ResponseEntity<ApiGenes>(HttpStatus.OK);
    }
}
