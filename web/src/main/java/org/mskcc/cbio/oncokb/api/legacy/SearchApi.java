package org.mskcc.cbio.oncokb.api.legacy;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.response.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

//@Controller
//@RequestMapping(value = "/public-api/v1/search", produces = {APPLICATION_JSON_VALUE})
//@Api(value = "/search", description = "the search API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class SearchApi {
    final Set<EvidenceType> SUPPORTED_EVIDENCE_TYPES = new HashSet<EvidenceType>() {{
        add(EvidenceType.GENE_SUMMARY);
        add(EvidenceType.GENE_BACKGROUND);
    }};

    @ApiOperation(value = "", notes = "Get list of evidences.", response = ApiSearchEvidences.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
//    @RequestMapping(value = "/evidences",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
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
            if (gene != null) {
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
            } else {
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
//    @RequestMapping(value = "/gene",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    public ResponseEntity<ApiGenes> searchGeneHugoSymbolGet(
        @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.", required = true) @RequestParam(value = "query", required = false) String query,
        @ApiParam(value = "Find the exact match with query.", defaultValue = "false") @RequestParam(value = "exactMatch", required = false, defaultValue = "false") Boolean exactMatch
    ) throws NotFoundException {
        ApiGenes instance = new ApiGenes();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;
        Set<Gene> genes = GeneUtils.searchGene(query);

        if (genes.size() == 0) {
            status = HttpStatus.NO_CONTENT;
        }

        meta.setCode(status.value());
        instance.setRespMeta(meta);
        instance.setData(genes);
        return new ResponseEntity<ApiGenes>(instance, status);
    }

    @ApiOperation(value = "", notes = "Get annotated variants information for specified gene.", response = ApiSearchVariantsBiological.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
//    @RequestMapping(value = "/variants/biological",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    public ResponseEntity<ApiSearchVariantsBiological> searchVariantsBiologicalGet(@ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    ) throws NotFoundException {
        ApiSearchVariantsBiological instance = new ApiSearchVariantsBiological();
        Set<BiologicalVariant> variants = new HashSet<>();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            variants = MainUtils.getBiologicalVariants(gene);
        }
        instance.setData(variants);
        meta.setCode(status.value());
        instance.setMeta(meta);
        return new ResponseEntity<ApiSearchVariantsBiological>(instance, status);
    }


    @ApiOperation(value = "", notes = "Get list of variant clinical information for specified gene.", response = ApiSearchVariantsClinical.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
//    @RequestMapping(value = "/variants/clinical",
//        produces = {"application/json"},
//        method = RequestMethod.GET)
    public ResponseEntity<ApiSearchVariantsClinical> searchVariantsClinicalGet(@ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol

    ) throws NotFoundException {
        ApiSearchVariantsClinical instance = new ApiSearchVariantsClinical();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;
        Set<ClinicalVariant> variants = new HashSet<>();

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            variants = MainUtils.getClinicalVariants(gene);
        }

        instance.setData(variants);
        meta.setCode(status.value());
        instance.setMeta(meta);
        return new ResponseEntity<ApiSearchVariantsClinical>(instance, status);
    }

    @ApiOperation(value = "", notes = "Search to find treatments.", response = ApiGenes.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/treatments",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiTreatments> searchTreatmentsGet(
        @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.", required = true) @RequestParam(value = "gene", required = false) String queryGene,
        @ApiParam(value = "The level of evidence.", defaultValue = "false") @RequestParam(value = "level", required = false) String queryLevel
    ) throws NotFoundException {
        ApiTreatments instance = new ApiTreatments();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;
        String errorMessage = null;
        Gene gene = GeneUtils.getGene(queryGene);

        if (gene == null && queryLevel == null) {
            status = HttpStatus.BAD_REQUEST;
            errorMessage = "Getting all treatments is not supported at this moment.";
        } else {
            if (queryLevel == null) {
                instance.setData(TreatmentUtils.getTreatmentsByGene(gene));
            }else {
                LevelOfEvidence level = LevelOfEvidence.getByLevel(queryLevel);
                if (level == null) {
                    status = HttpStatus.BAD_REQUEST;
                    errorMessage = "The level is invalid.";
                } else if (!LevelUtils.getPublicLevels().contains(level)) {
                    status = HttpStatus.BAD_REQUEST;
                    errorMessage = "The level is not supported at this moment.";
                } else if (gene == null){
                    instance.setData(TreatmentUtils.getTreatmentsByLevels(Collections.singleton(level)));
                }else {
                    instance.setData(TreatmentUtils.getTreatmentsByGeneAndLevels(gene, Collections.singleton(level)));
                }
            }
        }

        meta.setError_message(errorMessage);
        meta.setCode(status.value());
        instance.setRespMeta(meta);
        return new ResponseEntity<ApiTreatments>(instance, status);
    }

}
