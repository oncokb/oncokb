package org.mskcc.cbio.oncokb.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.response.ApiGenes;
import org.mskcc.cbio.oncokb.response.ApiSearchEvidences;
import org.mskcc.cbio.oncokb.response.ApiSearchVariantsBiological;
import org.mskcc.cbio.oncokb.response.ApiSearchVariantsClinical;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
    final Set<EvidenceType> SUPPORTED_EVIDENCE_TYPES = new HashSet<EvidenceType>() {{
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
    @RequestMapping(value = "/gene",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiGenes> searchGeneHugoSymbolGet(
        @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.", required = true) @RequestParam(value = "query", required = false) String query,
        @ApiParam(value = "Find the exact match with query.", defaultValue = "false") @RequestParam(value = "exactMatch", required = false, defaultValue = "false") Boolean exactMatch
    ) throws NotFoundException {
        ApiGenes instance = new ApiGenes();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;
        Set<ShortGene> genes = GeneUtils.searchShortGene(query);

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
    @RequestMapping(value = "/variants/biological",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiSearchVariantsBiological> searchVariantsBiologicalGet(@ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    ) throws NotFoundException {
        ApiSearchVariantsBiological instance = new ApiSearchVariantsBiological();
        Set<BiologicalVariant> variants = new HashSet<>();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            if (gene != null) {
                Long oldTime = new Date().getTime();
                Set<Alteration> alterations = new HashSet<>(AlterationUtils.getAllAlterations(gene));
                Map<Alteration, List<Alteration>> relevantAlterations = new HashMap<>();
                
                alterations = AlterationUtils.excludeVUS(gene, alterations);
                alterations = AlterationUtils.excludeGeneralAlterations(alterations);
                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all alterations for " + hugoSymbol);

                Set<EvidenceType> evidenceTypes = new HashSet<EvidenceType>() {{
                    add(EvidenceType.MUTATION_EFFECT);
                    add(EvidenceType.ONCOGENIC);
                }};
                Map<Alteration, Map<EvidenceType, Set<Evidence>>> evidences = new HashMap<>();

                for (Alteration alteration : alterations) {
                    Map<EvidenceType, Set<Evidence>> map = new HashMap<>();
                    map.put(EvidenceType.ONCOGENIC, new HashSet<Evidence>());
                    map.put(EvidenceType.MUTATION_EFFECT, new HashSet<Evidence>());
                    evidences.put(alteration, map);
                    relevantAlterations.put(alteration, AlterationUtils.getRelevantAlterations(gene, alteration.getAlteration(), alteration.getConsequence() != null ? alteration.getConsequence().getTerm() : null, alteration.getProteinStart(), alteration.getProteinEnd()));
                }
                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Initialize evidences.");

                Map<Gene, Set<Evidence>> geneEvidences =
                    EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypes);
                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all gene evidences.");

                for (Evidence evidence : geneEvidences.get(gene)) {
                    for (Map.Entry<Alteration, List<Alteration>> entry : relevantAlterations.entrySet()) {
                        if (CollectionUtils.intersection(entry.getValue(), evidence.getAlterations()).size() > 0) {
                            evidences.get(entry.getKey()).get(evidence.getEvidenceType()).add(evidence);
                        }
                    }
                }
                
                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Seperate evidences.");

                for (Map.Entry<Alteration, Map<EvidenceType, Set<Evidence>>> entry : evidences.entrySet()) {
                    Alteration alteration = entry.getKey();
                    Map<EvidenceType, Set<Evidence>> map = entry.getValue();

                    BiologicalVariant variant = new BiologicalVariant();
                    variant.setVariant(alteration.getAlteration());
                    Oncogenicity oncogenicity = Oncogenicity.getByLevel(EvidenceUtils.getKnownEffectFromEvidence(EvidenceType.ONCOGENIC, map.get(EvidenceType.ONCOGENIC)));
                    variant.setOncogenic(oncogenicity != null ? oncogenicity.getDescription() : "");
                    variant.setMutationEffect(EvidenceUtils.getKnownEffectFromEvidence(EvidenceType.MUTATION_EFFECT, map.get(EvidenceType.MUTATION_EFFECT)));
                    variant.setOncogenicPmids(EvidenceUtils.getPmids(map.get(EvidenceType.ONCOGENIC)));
                    variant.setMutationEffectPmids(EvidenceUtils.getPmids(map.get(EvidenceType.MUTATION_EFFECT)));
                    variants.add(variant);
                }
                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Created biological annotations.");
            }
        }
        instance.setData(variants);
        meta.setCode(status.value());
        instance.setMeta(meta);
        return new ResponseEntity<ApiSearchVariantsBiological>(instance, status);
    }


    @ApiOperation(value = "", notes = "Get list of variant clinical information for specified gene.", response = ApiSearchVariantsClinical.class)
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/variants/clinical",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<ApiSearchVariantsClinical> searchVariantsClinicalGet(@ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol

    ) throws NotFoundException {
        ApiSearchVariantsClinical instance = new ApiSearchVariantsClinical();
        RespMeta meta = new RespMeta();
        HttpStatus status = HttpStatus.OK;
        Set<ClinicalVariant> variants = new HashSet<>();

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            if (gene != null) {
                Set<Alteration> alterations = new HashSet<>(AlterationUtils.getAllAlterations(gene));
                Map<Alteration, List<Alteration>> relevantAlterations = new HashMap<>();
                
                alterations = AlterationUtils.excludeVUS(gene, alterations);
                alterations = AlterationUtils.excludeGeneralAlterations(alterations);
                
                Set<EvidenceType> evidenceTypes = new HashSet<EvidenceType>() {{
                    add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
                    add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
                }};
                Map<Alteration, Map<TumorType, Map<LevelOfEvidence, Set<Evidence>>>> evidences = new HashMap<>();
                Set<LevelOfEvidence> publicLevels = LevelUtils.getPublicLevels();

                for (Alteration alteration : alterations) {
                    evidences.put(alteration, new HashMap<TumorType, Map<LevelOfEvidence, Set<Evidence>>>());
                    //Find all relevant alterations for all alterations in this gene.
                    relevantAlterations.put(alteration, AlterationUtils.getRelevantAlterations(gene, alteration.getAlteration(), alteration.getConsequence() != null ? alteration.getConsequence().getTerm() : null, alteration.getProteinStart(), alteration.getProteinEnd()));
                }

                Map<Gene, Set<Evidence>> geneEvidences =
                    EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypes);

                for (Evidence evidence : geneEvidences.get(gene)) {
                    if (publicLevels.contains(evidence.getLevelOfEvidence())) {
                        TumorType tumorType = evidence.getTumorType();
                        for (Map.Entry<Alteration, List<Alteration>> entry : relevantAlterations.entrySet()) {
                            if (CollectionUtils.intersection(entry.getValue(), evidence.getAlterations()).size() > 0) {
                                if (!evidences.get(entry.getKey()).containsKey(tumorType)) {
                                    evidences.get(entry.getKey()).put(tumorType, new HashMap<LevelOfEvidence, Set<Evidence>>());
                                }

                                LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();
                                if (!evidences.get(entry.getKey()).get(tumorType).containsKey(levelOfEvidence)) {
                                    evidences.get(entry.getKey()).get(tumorType).put(levelOfEvidence, new HashSet<Evidence>());
                                }
                                evidences.get(entry.getKey()).get(tumorType).get(levelOfEvidence).add(evidence);
                            }
                        }
                    }
                }

                for (Map.Entry<Alteration, Map<TumorType, Map<LevelOfEvidence, Set<Evidence>>>> entry : evidences.entrySet()) {
                    Alteration alteration = entry.getKey();
                    Map<TumorType, Map<LevelOfEvidence, Set<Evidence>>> map = entry.getValue();

                    for (Map.Entry<TumorType, Map<LevelOfEvidence, Set<Evidence>>> _entry : map.entrySet()) {
                        TumorType tumorType = _entry.getKey();

                        for (Map.Entry<LevelOfEvidence, Set<Evidence>> __entry : _entry.getValue().entrySet()) {
                            ClinicalVariant variant = new ClinicalVariant();
                            variant.setVariant(alteration.getAlteration());
                            variant.setCancerType(tumorType.getName());
                            variant.setLevel(__entry.getKey().getLevel());
                            variant.setDrug(EvidenceUtils.getDrugs(__entry.getValue()));
                            variant.setDrugPmids(EvidenceUtils.getPmids(__entry.getValue()));
                            variants.add(variant);
                        }
                    }
                }
            }
        }

        instance.setData(variants);
        meta.setCode(status.value());
        instance.setMeta(meta);
        return new ResponseEntity<ApiSearchVariantsClinical>(instance, status);
    }
}
