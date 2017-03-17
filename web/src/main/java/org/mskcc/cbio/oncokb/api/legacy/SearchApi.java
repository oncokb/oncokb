package org.mskcc.cbio.oncokb.api.legacy;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/public-api/v1/search", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/search", description = "the search API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class SearchApi {
    final Set<EvidenceType> SUPPORTED_EVIDENCE_TYPES = new HashSet<EvidenceType>() {{
        add(EvidenceType.GENE_SUMMARY);
        add(EvidenceType.GENE_BACKGROUND);
    }};

    @ApiOperation(value = "", notes = "Get list of evidences.", response = Evidence.class, responseContainer = "Set")
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/evidences",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Set<Evidence>> searchEvidencesGet(
        @ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "") @RequestParam(value = "type", required = false) String type
    ) throws NotFoundException {
        //We only support GENE_SUMMARY and GENE_BACKGROUND at this moment for public API.
        HttpStatus status = HttpStatus.OK;
        Set<Evidence> evidences = new HashSet<>();

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            if (gene != null) {
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
            } else {
                status = HttpStatus.NO_CONTENT;
            }
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(evidences, status);
    }


    @ApiOperation(value = "", notes = "Search to find gene. Code 204 will be returned in the META if no gene matched.", response = Gene.class, responseContainer = "Set")
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/gene",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Set<Gene>> searchGeneHugoSymbolGet(
        @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.", required = true) @RequestParam(value = "query", required = false) String query,
        @ApiParam(value = "Find the exact match with query.", defaultValue = "false") @RequestParam(value = "exactMatch", required = false, defaultValue = "false") Boolean exactMatch
    ) throws NotFoundException {
        HttpStatus status = HttpStatus.OK;
        Set<Gene> genes = GeneUtils.searchGene(query);

        if (genes.size() == 0) {
            status = HttpStatus.NO_CONTENT;
        }

        return new ResponseEntity<>(genes, status);
    }

    @ApiOperation(value = "", notes = "Get annotated variants information for specified gene.", response = BiologicalVariant.class, responseContainer = "Set")
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/variants/biological",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Set<BiologicalVariant>> searchVariantsBiologicalGet(@ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    ) throws NotFoundException {
        Set<BiologicalVariant> variants = new HashSet<>();
        HttpStatus status = HttpStatus.OK;

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            variants = MainUtils.getBiologicalVariants(gene);
        }
        return new ResponseEntity<>(variants, status);
    }


    @ApiOperation(value = "", notes = "Get list of variant clinical information for specified gene.", response = ClinicalVariant.class, responseContainer = "Set")
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/variants/clinical",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Set<ClinicalVariant>> searchVariantsClinicalGet(@ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol

    ) throws NotFoundException {
        HttpStatus status = HttpStatus.OK;
        Set<ClinicalVariant> variants = new HashSet<>();

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            variants = MainUtils.getClinicalVariants(gene);
        }
        return new ResponseEntity<>(variants, status);
    }

    @ApiOperation(value = "", notes = "Search to find treatments.", response = Treatment.class, responseContainer = "Set")
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/treatments",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Set<Treatment>> searchTreatmentsGet(
        @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.", required = true) @RequestParam(value = "gene", required = false) String queryGene,
        @ApiParam(value = "The level of evidence.", defaultValue = "false") @RequestParam(value = "level", required = false) String queryLevel
    ) throws NotFoundException {
        HttpStatus status = HttpStatus.OK;
        Gene gene = GeneUtils.getGene(queryGene);
        Set<Treatment> treatments = new HashSet<>();

        if (gene == null && queryLevel == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            if (queryLevel == null) {
                treatments = TreatmentUtils.getTreatmentsByGene(gene);
            } else {
                LevelOfEvidence level = LevelOfEvidence.getByLevel(queryLevel);
                if (level == null) {
                    status = HttpStatus.BAD_REQUEST;
                } else if (!LevelUtils.getPublicLevels().contains(level)) {
                    status = HttpStatus.BAD_REQUEST;
                } else if (gene == null) {
                    treatments = TreatmentUtils.getTreatmentsByLevels(Collections.singleton(level));
                } else {
                    treatments = TreatmentUtils.getTreatmentsByGeneAndLevels(gene, Collections.singleton(level));
                }
            }
        }
        return new ResponseEntity<>(treatments, status);
    }

}
