package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.DrugMatch;
import org.mskcc.cbio.oncokb.apiModels.annotation.*;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
@Api(tags = "Annotations", description = "Providing annotation services")
@Controller
public class AnnotationsApiController {
    final String EVIDENCE_TYPES_DESCRIPTION = "Evidence type to compute. This could help to improve the performance if you only look for sub-content. Example: ONCOGENIC. All available evidence type are GENE_SUMMARY, MUTATION_SUMMARY, TUMOR_TYPE_SUMMARY, PROGNOSTIC_SUMMARY, DIAGNOSTIC_SUMMARY, ONCOGENIC, MUTATION_EFFECT, PROGNOSTIC_IMPLICATION, DIAGNOSTIC_IMPLICATION, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE. For multiple evidence types query, use ',' as separator.";

    @Autowired
    CacheFetcher cacheFetcher;

    // Annotate mutations by protein change
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by protein change.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byProteinChange",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateMutationsByProteinChangeGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation. Example: BRAF") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Higher priority than hugoSymbol). Example: 673") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Protein Change. Example: V600E") @RequestParam(value = "alteration", required = false) String proteinChange
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "Consequence. Exacmple: missense_variant", allowableValues = "feature_truncation, frameshift_variant, inframe_deletion, inframe_insertion, start_lost, missense_variant, splice_region_variant, stop_gained, synonymous_variant, intron_variant") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "Protein Start. Example: 600") @RequestParam(value = "proteinStart", required = false) Integer proteinStart
        , @ApiParam(value = "Protein End. Example: 600") @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            ReferenceGenome matchedRG = null;
            if (!StringUtils.isEmpty(referenceGenome)) {
                matchedRG = MainUtils.searchEnum(ReferenceGenome.class, referenceGenome);
                if (matchedRG == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
            Query query = new Query(null, matchedRG, entrezGeneId, hugoSymbol, proteinChange, null, null, tumorType, consequence, proteinStart, proteinEnd, null);
            indicatorQueryResp = this.cacheFetcher.processQuery(
                query.getReferenceGenome(),
                query.getEntrezGeneId(),
                query.getHugoSymbol(),
                query.getAlteration(),
                null,
                query.getTumorType(),
                query.getConsequence(),
                query.getProteinStart(),
                query.getProteinEnd(),
                null,
                null,
                null,
                false,
                new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ","))
            );
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by protein change.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byProteinChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByProteinChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByProteinChangeQuery> body
    ) {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            for (AnnotateMutationByProteinChangeQuery query : body) {
                IndicatorQueryResp resp = this.cacheFetcher.processQuery(
                    query.getReferenceGenome(),
                    query.getGene() == null ? null : query.getGene().getEntrezGeneId(),
                    query.getGene() == null ? null : query.getGene().getHugoSymbol(),
                    query.getAlteration(),
                    null,
                    query.getTumorType(),
                    query.getConsequence(),
                    query.getProteinStart(),
                    query.getProteinEnd(),
                    null,
                    null,
                    null,
                    false,
                    query.getEvidenceTypes()
                );
                resp.getQuery().setId(query.getId());
                result.add(resp);
            }
        }
        return new ResponseEntity<>(result, status);
    }

    // Annotate mutations by genomic change
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by genomic change.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byGenomicChange",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateMutationsByGenomicChangeGet(
        @ApiParam(value = "Genomic location. Example: 7,140453136,140453136,A,T", required = true) @RequestParam(value = "genomicLocation", required = true) String genomicLocation
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) throws ApiException, org.genome_nexus.ApiException {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if (StringUtils.isEmpty(genomicLocation)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ReferenceGenome matchedRG = null;
        if (!StringUtils.isEmpty(referenceGenome)) {
            matchedRG = MainUtils.searchEnum(ReferenceGenome.class, referenceGenome);
            if (matchedRG == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        indicatorQueryResp = this.getIndicatorQueryFromGenomicLocation(matchedRG, genomicLocation, tumorType, new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")), cacheFetcher.getAllTranscriptGenes());
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by genomic change.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byGenomicChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByGenomicChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByGenomicChangeQuery> body
    ) throws ApiException, org.genome_nexus.ApiException {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Set<org.oncokb.oncokb_transcript.client.Gene> allTranscriptGenes = cacheFetcher.getAllTranscriptGenes();
            for (AnnotateMutationByGenomicChangeQuery query : body) {
                IndicatorQueryResp resp = this.getIndicatorQueryFromGenomicLocation(query.getReferenceGenome(), query.getGenomicLocation(), query.getTumorType(), query.getEvidenceTypes(), allTranscriptGenes);
                resp.getQuery().setId(query.getId());
                result.add(resp);
            }
        }
        return new ResponseEntity<>(result, status);
    }

    // Annotate mutations by HGVSg
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by HGVSg.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byHGVSg",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateMutationsByHGVSgGet(
        @ApiParam(value = "HGVS genomic format. Example: 7:g.140453136A>T", required = true) @RequestParam(value = "hgvsg", required = true) String hgvsg
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) throws ApiException, org.genome_nexus.ApiException {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if (StringUtils.isEmpty(hgvsg)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            ReferenceGenome matchedRG = null;
            if (!StringUtils.isEmpty(referenceGenome)) {
                matchedRG = MainUtils.searchEnum(ReferenceGenome.class, referenceGenome);
                if (matchedRG == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            if (!AlterationUtils.isValidHgvsg(hgvsg)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            indicatorQueryResp = this.getIndicatorQueryFromHGVSg(
                matchedRG,
                hgvsg,
                tumorType,
                new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")),
                cacheFetcher.getAllTranscriptGenes()
            );
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by genomic change.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/mutations/byHGVSg",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateMutationsByHGVSgPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVSgQuery> body
    ) throws ApiException, org.genome_nexus.ApiException {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Set<org.oncokb.oncokb_transcript.client.Gene> allTranscriptGenes = cacheFetcher.getAllTranscriptGenes();
            for (AnnotateMutationByHGVSgQuery query : body) {
                IndicatorQueryResp resp = this.getIndicatorQueryFromHGVSg(
                    query.getReferenceGenome(),
                    query.getHgvsg(),
                    query.getTumorType(),
                    query.getEvidenceTypes(),
                    allTranscriptGenes
                );
                resp.getQuery().setId(query.getId());
                result.add(resp);
            }
        }
        return new ResponseEntity<>(result, status);
    }

    // Annotate copy number alterations
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate copy number alteration.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/copyNumberAlterations",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateCopyNumberAlterationsGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation. Example: BRAF") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Higher priority than hugoSymbol). Example: 673") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Copy number alteration type", required = true) @RequestParam(value = "copyNameAlterationType", required = true) CopyNumberAlterationType copyNameAlterationType
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            if (copyNameAlterationType == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            ReferenceGenome matchedRG = null;
            if (!StringUtils.isEmpty(referenceGenome)) {
                matchedRG = MainUtils.searchEnum(ReferenceGenome.class, referenceGenome);
                if (matchedRG == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
            indicatorQueryResp = this.cacheFetcher.processQuery(
                matchedRG,
                entrezGeneId,
                hugoSymbol,
                StringUtils.capitalize(copyNameAlterationType.name().toLowerCase()),
                null,
                tumorType,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")));
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate copy number alterations.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/copyNumberAlterations",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateCopyNumberAlterationsPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateCopyNumberAlterationQuery> body
    ) {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {

            for (AnnotateCopyNumberAlterationQuery query : body) {
                Gene gene = new Gene();
                if (query.getGene() != null) {
                    try {
                        gene = this.cacheFetcher.findGeneBySymbol(
                            query.getGene().getEntrezGeneId() != null ?
                                query.getGene().getEntrezGeneId().toString() :
                                query.getGene().getHugoSymbol()
                        );
                        if (gene == null) {
                            gene = new Gene();
                            gene.setEntrezGeneId(query.getGene().getEntrezGeneId());
                            gene.setHugoSymbol(query.getGene().getHugoSymbol());
                        }
                    } catch (ApiException e) {
                    }
                }
                IndicatorQueryResp resp = this.cacheFetcher.processQuery(
                    query.getReferenceGenome(),
                    gene.getEntrezGeneId(),
                    gene.getHugoSymbol(),
                    StringUtils.capitalize(query.getCopyNameAlterationType().name().toLowerCase()),
                    null,
                    query.getTumorType(), null, null, null, null,
                    null, null, false, query.getEvidenceTypes());
                resp.getQuery().setId(query.getId());
                result.add(resp);
            }
        }
        return new ResponseEntity<>(result, status);
    }

    // Annotate structural variants
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate structural variant.", response = IndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/structuralVariants",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<IndicatorQueryResp> annotateStructuralVariantsGet(
        @ApiParam(value = "The gene symbol A used in Human Genome Organisation. Example: ABL1") @RequestParam(value = "hugoSymbolA", required = false) String hugoSymbolA
        , @ApiParam(value = "The entrez gene ID A. (Higher priority than hugoSymbolA) Example: 25") @RequestParam(value = "entrezGeneIdA", required = false) Integer entrezGeneIdA
        , @ApiParam(value = "The gene symbol B used in Human Genome Organisation.Example: BCR ") @RequestParam(value = "hugoSymbolB", required = false) String hugoSymbolB
        , @ApiParam(value = "The entrez gene ID B. (Higher priority than hugoSymbolB) Example: 613") @RequestParam(value = "entrezGeneIdB", required = false) Integer entrezGeneIdB
        , @ApiParam(value = "Structural variant type", required = true) @RequestParam(value = "structuralVariantType", required = true) StructuralVariantType structuralVariantType
        , @ApiParam(value = "Whether is functional fusion", required = true) @RequestParam(value = "isFunctionalFusion", defaultValue = "FALSE", required = true) Boolean isFunctionalFusion
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = EVIDENCE_TYPES_DESCRIPTION) @RequestParam(value = "evidenceType", required = false) String evidenceTypes
    ) {
        HttpStatus status = HttpStatus.OK;
        IndicatorQueryResp indicatorQueryResp = null;

        if (structuralVariantType == null || isFunctionalFusion == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if ((entrezGeneIdA != null && hugoSymbolA != null && !GeneUtils.isSameGene(entrezGeneIdA, hugoSymbolA)) || (entrezGeneIdB != null && hugoSymbolB != null && !GeneUtils.isSameGene(entrezGeneIdB, hugoSymbolB))) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            Gene geneA = new Gene();
            try {
                geneA = this.cacheFetcher.findGeneBySymbol(entrezGeneIdA == null ? hugoSymbolA : entrezGeneIdA.toString());
                if (geneA == null) {
                    geneA = new Gene();
                }
            } catch (ApiException e) {
            }
            if (geneA.getEntrezGeneId() == null && StringUtils.isEmpty(geneA.getHugoSymbol())) {
                geneA.setEntrezGeneId(entrezGeneIdA);
                geneA.setHugoSymbol(hugoSymbolA == null ? "" : hugoSymbolA);
            }
            Gene geneB = new Gene();
            try {
                geneB = this.cacheFetcher.findGeneBySymbol(entrezGeneIdB == null ? hugoSymbolB : entrezGeneIdB.toString());
                if (geneB == null) {
                    geneB = new Gene();
                }
            } catch (ApiException e) {
            }
            if (geneB.getEntrezGeneId() == null && StringUtils.isEmpty(geneB.getHugoSymbol())) {
                geneB.setEntrezGeneId(entrezGeneIdB);
                geneB.setHugoSymbol(hugoSymbolB == null ? "" : hugoSymbolB);
            }

            ReferenceGenome matchedRG = null;
            if (!StringUtils.isEmpty(referenceGenome)) {
                matchedRG = MainUtils.searchEnum(ReferenceGenome.class, referenceGenome);
                if (matchedRG == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
            String fusionName = FusionUtils.getFusionName(geneA, geneB);
            indicatorQueryResp = this.cacheFetcher.processQuery(
                matchedRG, null, fusionName, null, AlterationType.STRUCTURAL_VARIANT.name(), tumorType, isFunctionalFusion ? "fusion" : null, null, null, structuralVariantType, null,
                null, false, new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")));
        }
        return new ResponseEntity<>(indicatorQueryResp, status);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate structural variants.", response = IndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = IndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = String.class)})
    @RequestMapping(value = "/annotate/structuralVariants",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<IndicatorQueryResp>> annotateStructuralVariantsPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody(required = true) List<AnnotateStructuralVariantQuery> body
    ) {
        HttpStatus status = HttpStatus.OK;
        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            for (AnnotateStructuralVariantQuery query : body) {
                Gene geneA = new Gene();
                if (query.getGeneA() != null) {
                    try {
                        geneA = this.cacheFetcher.findGeneBySymbol(
                            query.getGeneA().getEntrezGeneId() != null ?
                                query.getGeneA().getEntrezGeneId().toString() :
                                query.getGeneA().getHugoSymbol()
                        );
                        if (geneA == null) {
                            geneA = new Gene();
                        }
                    } catch (ApiException e) {
                    }
                }
                if (StringUtils.isEmpty(geneA.getHugoSymbol()) && geneA.getEntrezGeneId() == null && query.getGeneA() != null) {
                    geneA.setHugoSymbol(query.getGeneA().getHugoSymbol() == null ? "" : query.getGeneA().getHugoSymbol());
                    geneA.setEntrezGeneId(query.getGeneA().getEntrezGeneId());
                }

                Gene geneB = new Gene();
                if (query.getGeneB() != null) {
                    try {
                        geneB = this.cacheFetcher.findGeneBySymbol(
                            query.getGeneB().getEntrezGeneId() != null ?
                                query.getGeneB().getEntrezGeneId().toString() :
                                query.getGeneB().getHugoSymbol()
                        );
                        if (geneB == null) {
                            geneB = new Gene();
                        }
                    } catch (ApiException e) {
                    }
                }
                if (StringUtils.isEmpty(geneB.getHugoSymbol()) && geneB.getEntrezGeneId() == null && query.getGeneB() != null) {
                    geneB.setHugoSymbol(query.getGeneB().getHugoSymbol() == null ? "" : query.getGeneB().getHugoSymbol());
                    geneB.setEntrezGeneId(query.getGeneB().getEntrezGeneId());
                }

                String fusionName = FusionUtils.getFusionName(geneA, geneB);

                IndicatorQueryResp resp = this.cacheFetcher.processQuery(
                    query.getReferenceGenome(),  null, fusionName, null, AlterationType.STRUCTURAL_VARIANT.name(), query.getTumorType(), query.getFunctionalFusion() ? "fusion" : "", null, null, query.getStructuralVariantType(), null,
                    null, false, query.getEvidenceTypes());
                resp.getQuery().setId(query.getId());
                result.add(resp);
            }
        }
        return new ResponseEntity<>(result, status);
    }

    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Get annotations based on search", response = AnnotationSearchResult.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/annotation/search",
        produces = {"application/json"},
        method = RequestMethod.GET)
    ResponseEntity<LinkedHashSet<AnnotationSearchResult>> annotationSearchGet(
        @ApiParam(value = "The search query, it could be hugoSymbol, variant or cancer type. At least two characters. Maximum two keywords are supported, separated by space", required = true) @RequestParam(value = "query") String query,
        @ApiParam(value = "The limit of returned result.") @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit
    ) {
        final int DEFAULT_LIMIT = 10;
        final int QUERY_MIN_LENGTH = 2;
        TreeSet<AnnotationSearchResult> result = new TreeSet<>(new AnnotationSearchResultComp(query));
        if(limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (query != null && query.length() >= QUERY_MIN_LENGTH) {
            List<String> keywords = Arrays.asList(query.trim().split("\\s+"));

            if (keywords.size() == 1) {
                // Blur search gene
                result.addAll(findActionableGenesByGeneSearch(keywords.get(0)));

                // Blur search variant
                result.addAll(findActionableGenesByAlterationSearch(keywords.get(0)));

                // Blur search cancer type
                result.addAll(findActionableGenesByCancerType(keywords.get(0)));

                // If the keyword contains dash and result is empty, then we should return both fusion genes
                if (keywords.get(0).contains("-") && result.isEmpty()) {
                    for (String subKeyword : keywords.get(0).split("-")) {
                        result.addAll(findActionableGenesByGeneSearch(subKeyword));
                    }
                }
            } else {
                // Assume that the first keyword is a gene, followed by alteration
                // Todo: We should be able to find the gene even if it is not the first keyword.
                Set<Gene> geneMatches = new HashSet<>();
                if (keywords.get(0).contains("-")) {
                    Set<Gene> subGenes = new HashSet<>();
                    for (String subKeyword : keywords.get(0).split("-")) {
                        subGenes.addAll(GeneUtils.searchGene(subKeyword, false));
                    }
                    geneMatches.addAll(subGenes);
                } else {
                    geneMatches.addAll(GeneUtils.searchGene(keywords.get(0), false));
                }

                String alterationKeywords = StringUtils.join(keywords.subList(1, keywords.size()), " ");
                List<Alteration> altMatches = AlterationUtils.lookupVariant(alterationKeywords, false, true, AlterationUtils.getAllAlterations())
                    .stream()
                    .filter(alt -> geneMatches.contains(alt.getGene()))
                    .collect(Collectors.toList());
                for (Gene gene: geneMatches) {
                    for (Alteration alteration: altMatches) {
                        Query indicatorQuery = new Query();
                        indicatorQuery.setEntrezGeneId(gene.getEntrezGeneId());
                        indicatorQuery.setHugoSymbol(gene.getHugoSymbol());
                        if (alteration.getName().toLowerCase().contains(alterationKeywords.toLowerCase())) {
                            indicatorQuery.setAlteration(alteration.getName());
                        } else {
                            indicatorQuery.setAlteration((alteration.getAlteration()));
                        }
                        AnnotationSearchResult annotationSearchResult = new AnnotationSearchResult();
                        annotationSearchResult.setQueryType(AnnotationSearchQueryType.VARIANT);
                        annotationSearchResult.setIndicatorQueryResp(IndicatorUtils.processQuery(indicatorQuery, null, null, null));
                        if (annotationSearchResult.getIndicatorQueryResp().getVariantExist()) {
                            result.add(annotationSearchResult);
                        }
                    }
                }

                // // If there is no match, the keywords could referring to a variant, try to do a blur variant search
                String fullKeywords = StringUtils.join(keywords, " ");
                result.addAll(findActionableGenesByAlterationSearch(fullKeywords));

                // // // Blur search for multi-word cancer type
                result.addAll(findActionableGenesByCancerType(fullKeywords));
            }
        }

        LinkedHashSet<AnnotationSearchResult> orderedResult = new LinkedHashSet<>();
        orderedResult.addAll(result);
        
        return new ResponseEntity<>(MainUtils.getLimit(orderedResult, limit), HttpStatus.OK);
    }

    private LinkedHashSet<AnnotationSearchResult> findActionableGenesByGeneSearch(String keyword) {
        LinkedHashSet<AnnotationSearchResult> result = new LinkedHashSet<>();
        Set<Gene> geneMatches = GeneUtils.searchGene(keyword, false);
        for (Gene gene: geneMatches) {
            Query query = new Query();
            query.setEntrezGeneId(gene.getEntrezGeneId());
            query.setHugoSymbol(gene.getHugoSymbol());
            AnnotationSearchResult annotationSearchResult = new AnnotationSearchResult();
            annotationSearchResult.setQueryType(AnnotationSearchQueryType.GENE);
            annotationSearchResult.setIndicatorQueryResp(IndicatorUtils.processQuery(query, null, null, null));
            result.add(annotationSearchResult);
        }
        return result;
    }

    private LinkedHashSet<AnnotationSearchResult> findActionableGenesByAlterationSearch(String keyword) {
        LinkedHashSet<AnnotationSearchResult> result = new LinkedHashSet<>();
        List<Alteration> altMatches = AlterationUtils.lookupVariant(keyword, false, true, AlterationUtils.getAllAlterations());
        for (Alteration alteration: altMatches) {
            Query indicatorQuery = new Query();
            if (alteration.getName().toLowerCase().contains(keyword.toLowerCase())) {
                indicatorQuery.setAlteration(alteration.getName());
            } else {
                indicatorQuery.setAlteration((alteration.getAlteration()));
            }
            indicatorQuery.setEntrezGeneId(alteration.getGene().getEntrezGeneId());
            indicatorQuery.setHugoSymbol(alteration.getGene().getHugoSymbol());
            AnnotationSearchResult annotationSearchResult = new AnnotationSearchResult();
            annotationSearchResult.setQueryType(AnnotationSearchQueryType.VARIANT);
            annotationSearchResult.setIndicatorQueryResp(IndicatorUtils.processQuery(indicatorQuery, null, null, null));
            result.add(annotationSearchResult);
        }
        return result;
    }

    private LinkedHashSet<AnnotationSearchResult> findActionableGenesByCancerType(String query) {

        Set<Evidence> allImplicationEvidences = EvidenceUtils.getEvidenceByEvidenceTypesAndLevels(EvidenceTypeUtils.getImplicationEvidenceTypes(), LevelUtils.getPublicLevels());

        query = query.toLowerCase();

        Set<TumorType> tumorTypeMatches = new HashSet<>();
        for(Map.Entry<String, TumorType> subtype: CacheUtils.getLowercaseSubtypeTumorTypeMap().entrySet()) {
            if(subtype.getKey().contains(query)) {
                tumorTypeMatches.add(subtype.getValue());
            }
        }

        for(Map.Entry<String, TumorType> mainType: CacheUtils.getMainTypeTumorTypeMap().entrySet()) {
            if (mainType.getKey().toLowerCase().contains(query)) {
                tumorTypeMatches.add(mainType.getValue());
            }
        }

        if (tumorTypeMatches.isEmpty()) {
            return new LinkedHashSet<>();
        }

        LinkedHashSet<AnnotationSearchResult> result = new LinkedHashSet<>();
        Set<SearchObject> searchObjects = new HashSet<>();
        for (TumorType tumorType : tumorTypeMatches) {
            for (Evidence evidence : allImplicationEvidences) {
                if (TumorTypeUtils.findEvidenceRelevantCancerTypes(evidence).contains(tumorType)) {
                    for (Alteration alteration: evidence.getAlterations()) {
                        SearchObject searchObject = new SearchObject();
                        searchObject.setGene(evidence.getGene());
                        searchObject.setAlteration(alteration);
                        searchObject.setTumorType(tumorType);
                        searchObjects.add(searchObject);
                    }
                }
            }
        }

        for (SearchObject searchObject: searchObjects) {
            Query indicatorQuery = new Query();
            indicatorQuery.setEntrezGeneId(searchObject.getGene().getEntrezGeneId());
            indicatorQuery.setHugoSymbol(searchObject.getGene().getHugoSymbol());
            indicatorQuery.setAlteration(searchObject.getAlteration().getName());
            if (StringUtils.isNotEmpty(searchObject.getTumorType().getSubtype()) && searchObject.getTumorType().getSubtype().toLowerCase().contains(query)) {
                indicatorQuery.setTumorType(searchObject.getTumorType().getSubtype());
            } else {
                indicatorQuery.setTumorType(searchObject.getTumorType().getMainType());
            }
            AnnotationSearchResult annotationSearchResult = new AnnotationSearchResult();
            annotationSearchResult.setQueryType(AnnotationSearchQueryType.CANCER_TYPE);
            annotationSearchResult.setIndicatorQueryResp(IndicatorUtils.processQuery(indicatorQuery, null, null, null));
            result.add(annotationSearchResult);
        }

        return result;
    }

    private IndicatorQueryResp getIndicatorQueryFromGenomicLocation(
        ReferenceGenome referenceGenome,
        String genomicLocation,
        String tumorType,
        Set<EvidenceType> evidenceTypes,
        Set<org.oncokb.oncokb_transcript.client.Gene> allTranscriptGenes
    ) throws ApiException, org.genome_nexus.ApiException {
        Alteration alteration;
        if (!this.cacheFetcher.genomicLocationShouldBeAnnotated(GNVariantAnnotationType.GENOMIC_LOCATION, genomicLocation, referenceGenome, allTranscriptGenes)) {
            alteration = new Alteration();
        } else {
            alteration = this.cacheFetcher.getAlterationFromGenomeNexus(GNVariantAnnotationType.GENOMIC_LOCATION, referenceGenome, genomicLocation);
        }
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, alteration, null);
        return this.cacheFetcher.processQuery(
            referenceGenome,
            query.getEntrezGeneId(),
            query.getHugoSymbol(),
            query.getAlteration(),
            null,
            query.getTumorType(),
            query.getConsequence(),
            query.getProteinStart(),
            query.getProteinEnd(),
            null,
            null,
            null,
            false,
            evidenceTypes
        );
    }

    private IndicatorQueryResp getIndicatorQueryFromHGVSg(
        ReferenceGenome referenceGenome,
        String hgvsg,
        String tumorType,
        Set<EvidenceType> evidenceTypes,
        Set<org.oncokb.oncokb_transcript.client.Gene> allTranscriptGenes
    ) throws ApiException, org.genome_nexus.ApiException {
        Alteration alteration;
        if (!this.cacheFetcher.genomicLocationShouldBeAnnotated(GNVariantAnnotationType.HGVS_G, hgvsg, referenceGenome, allTranscriptGenes)) {
            alteration = new Alteration();
        } else {
            alteration = this.cacheFetcher.getAlterationFromGenomeNexus(GNVariantAnnotationType.HGVS_G, referenceGenome, hgvsg);
        }
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, alteration, hgvsg);

        return this.cacheFetcher.processQuery(
            referenceGenome,
            query.getEntrezGeneId(),
            query.getHugoSymbol(),
            query.getAlteration(),
            null,
            query.getTumorType(),
            query.getConsequence(),
            query.getProteinStart(),
            query.getProteinEnd(),
            null,
            query.getHgvs(),
            null,
            false,
            evidenceTypes
        );
    }

    private static Map<String, Gene> getGenePool(List<Gene> genes) {
        Map<String, Gene> map = new HashMap<>();
        for (Gene gene : genes) {
            map.put(gene.getHugoSymbol().toLowerCase(), gene);
            map.put(gene.getEntrezGeneId().toString(), gene);
            for (String alias : gene.getGeneAliases()) {
                map.put(alias.toLowerCase(), gene);
            }
        }
        return map;
    }

    private static Gene findGeneFromPool(Map<String, Gene> genePool, QueryGene queryGene) {
        return genePool.get(queryGene.getEntrezGeneId() == null ? queryGene.getHugoSymbol().toLowerCase() : queryGene.getEntrezGeneId().toString());
    }
}

class AnnotationSearchResultComp implements Comparator<AnnotationSearchResult> {
    private String keyword;

    public AnnotationSearchResultComp(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    @Override
    public int compare(AnnotationSearchResult a1, AnnotationSearchResult a2) {
        IndicatorQueryResp i1 = a1.getIndicatorQueryResp();
        IndicatorQueryResp i2 = a2.getIndicatorQueryResp();

        // Compare by query type
        Integer result = MainUtils.compareAnnotationSearchQueryType(a1.getQueryType(), a2.getQueryType(), true);

        String name1 = "";
        String name2 = "";
        if (result == 0) {
            if (a1.getQueryType().equals(AnnotationSearchQueryType.GENE)) {
                name1 = i1.getQuery().getHugoSymbol().toLowerCase();
                name2 = i2.getQuery().getHugoSymbol().toLowerCase();
            }
            if (a1.getQueryType().equals(AnnotationSearchQueryType.VARIANT)) {
                name1 = i1.getQuery().getAlteration().toLowerCase();
                name2 = i2.getQuery().getAlteration().toLowerCase();
            }
            if (a1.getQueryType().equals(AnnotationSearchQueryType.CANCER_TYPE)) {
                name1 = i1.getQuery().getTumorType().toLowerCase();
                name2 = i2.getQuery().getTumorType().toLowerCase();
            }
        }
        Integer index1 = name1.indexOf(this.keyword);
        Integer index2 = name2.indexOf(this.keyword);
        if (index1.equals(index2)) {
            return compareLevel(i1, i2, name1, name2);
        } else {
            if (index1.equals(-1))
                return 1;
            if (index2.equals(-1))
                return -1;
            return compareLevel(i1, i2, name1, name2);
        }

    }

    private Integer compareLevel(IndicatorQueryResp i1, IndicatorQueryResp i2, String name1, String name2) {
        // Compare therapeutic levels
        LevelOfEvidence i1Level = i1.getHighestSensitiveLevel();
        LevelOfEvidence i2Level = i2.getHighestSensitiveLevel();
        if (i1Level == null) {
            i1Level = i1.getHighestResistanceLevel();
        }
        if (i2Level == null) {
            i2Level = i2.getHighestResistanceLevel();
        }
        Integer result = LevelUtils.compareLevel(i1Level, i2Level, LevelUtils.getIndexedTherapeuticLevels());
        if (result == 0) {
            // Compare diagnostic level
            result = LevelUtils.compareLevel(i1.getHighestDiagnosticImplicationLevel(), i2.getHighestDiagnosticImplicationLevel(), LevelUtils.getIndexedDiagnosticLevels());
            if (result == 0) {
                result = LevelUtils.compareLevel(i1.getHighestPrognosticImplicationLevel(), i2.getHighestPrognosticImplicationLevel(), LevelUtils.getIndexedPrognosticLevels());
                if (result == 0) {
                    result = LevelUtils.compareLevel(i1.getHighestFdaLevel(), i2.getHighestFdaLevel(), LevelUtils.getIndexedFdaLevels());
                    if (result == 0) {
                        //Compare Oncogenicity. Treat YES, LIKELY as the same
                        Oncogenicity o1 = Oncogenicity.getByEffect(i1.getOncogenic());
                        Oncogenicity o2 = Oncogenicity.getByEffect(i2.getOncogenic());
                        if (o1 != null && o1.equals(Oncogenicity.LIKELY)) {
                            o1 = Oncogenicity.YES;
                        }
                        if (o2 != null && o2.equals(Oncogenicity.LIKELY)) {
                            o2 = Oncogenicity.YES;
                        }
                        result = MainUtils.compareOncogenicity(o1, o2, true);
                        if (result == 0) {
                            // Compare alteration name
                            if (i1 == null || StringUtils.isNotEmpty(i1.getQuery().getAlteration())) {
                                return 1;
                            }
                            if (i2 == null || StringUtils.isNotEmpty(i2.getQuery().getAlteration())) {
                                return -1;
                            }
                            name1 = i1.getQuery().getAlteration().toLowerCase();
                            name2 = i2.getQuery().getAlteration().toLowerCase();
                            result = name1.compareTo(name2);
                            if (result == 0) {
                                // Compare gene name
                                result = i1.getQuery().getHugoSymbol().compareTo(i2.getQuery().getHugoSymbol());
                            }
                        }
                    }
                    return result;
                }
            }

        }
        return result;
    }
}

class SearchObject {
    private Gene gene;
    private Alteration alteration;
    private TumorType tumorType;

    public Gene getGene() {
        return this.gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public Alteration getAlteration() {
        return this.alteration;
    }

    public void setAlteration(Alteration alteration) {
        this.alteration = alteration;
    }

    public TumorType getTumorType() {
        return this.tumorType;
    }

    public void setTumorType(TumorType tumorType) {
        this.tumorType = tumorType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchObject)) return false;
        SearchObject searchObject = (SearchObject) o;
        return Objects.equals(getAlteration(), searchObject.getAlteration()) &&
            Objects.equals(getGene(), searchObject.getGene()) &&
            Objects.equals(getTumorType(), searchObject.getTumorType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGene(), getAlteration(), getTumorType());
    }

}
