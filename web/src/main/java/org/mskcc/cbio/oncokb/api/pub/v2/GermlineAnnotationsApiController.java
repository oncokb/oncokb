package org.mskcc.cbio.oncokb.api.pub.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.genome_nexus.client.GenomicLocation;
import org.genome_nexus.client.TranscriptConsequenceSummary;
import org.genome_nexus.client.VariantAnnotation;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByGenomicChangeQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByHGVScQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByHGVSgQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotationQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.GermlineQuery;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpError;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.GermlineIndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.genomeNexus.TranscriptSummaryAlterationResult;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.GenomeNexusUtils;
import org.mskcc.cbio.oncokb.util.IndicatorUtils2;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.mskcc.cbio.oncokb.util.QueryUtils;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Api(tags = "Germline Annotations", description = "Providing germline annotations")
@Controller
@RequestMapping(value = "/api/v2")
public class GermlineAnnotationsApiController {
    @Autowired
    CacheFetcher cacheFetcher;

    // Annotate mutations by genomic change
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by genomic change.", response = GermlineIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byGenomicChange",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<GermlineIndicatorQueryResp> annotateMutationsByGenomicChangeGet(
        @ApiParam(value = "Genomic location following TCGA MAF format. Example: 7,140453136,140453136,A,T", required = true) @RequestParam(value = "genomicLocation", required = true) String genomicLocation
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (StringUtils.isEmpty(genomicLocation)) {
            throw new ApiHttpErrorException("genomicLocation is missing.", HttpStatus.BAD_REQUEST);
        }

        ReferenceGenome matchedRG = resolveMatchedRG(referenceGenome);

        AnnotateMutationByGenomicChangeQuery query = new AnnotateMutationByGenomicChangeQuery();
        query.setGenomicLocation(genomicLocation);
        query.setReferenceGenome(matchedRG);
        query.setTumorType(tumorType);
        query.getGermlineQuery().setGermline(true);
        GermlineIndicatorQueryResp indicatorQueryResp = annotateMutationsByGenomicChange(Collections.singletonList(query)).get(0);

        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by genomic change.", response = GermlineIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byGenomicChange",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<GermlineIndicatorQueryResp>> annotateMutationsByGenomicChangePost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByGenomicChangeQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(annotateMutationsByGenomicChange(body), HttpStatus.OK);
    }

    // Annotate mutations by HGVSg
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by HGVSg.", response = GermlineIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byHGVSg",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<GermlineIndicatorQueryResp> annotateMutationsByHGVSgGet(
        @ApiParam(value = "HGVS genomic format following HGVS nomenclature. Example: 7:g.140453136A>T", required = true) @RequestParam(value = "hgvsg", required = true) String hgvsg
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (StringUtils.isEmpty(hgvsg)) {
            throw new ApiHttpErrorException("hgvsg is missing.", HttpStatus.BAD_REQUEST);
        }

        ReferenceGenome matchedRG = resolveMatchedRG(referenceGenome);

        if (!AlterationUtils.isValidHgvsg(hgvsg)) {
            throw new ApiHttpErrorException("hgvsg is invalid.", HttpStatus.BAD_REQUEST);
        }

        AnnotateMutationByHGVSgQuery query = new AnnotateMutationByHGVSgQuery();
        query.setHgvsg(hgvsg);
        query.setReferenceGenome(matchedRG);
        query.setTumorType(tumorType);
        query.getGermlineQuery().setGermline(true);
        GermlineIndicatorQueryResp indicatorQueryResp = annotateMutationsByHGVSg(Collections.singletonList(query)).get(0);
        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by HGVSg.", response = GermlineIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byHGVSg",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<GermlineIndicatorQueryResp>> annotateMutationsByHGVSgPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVSgQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(annotateMutationsByHGVSg(body), HttpStatus.OK);
    }

    // Annotate mutations by HGVSc
    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutation by HGVSc.", response = GermlineIndicatorQueryResp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byHGVSc",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<GermlineIndicatorQueryResp> annotateMutationsByHGVScGet(
        @ApiParam(value = "HGVS cDNA format following HGVS nomenclature. Example: EGFR:c.2369C>T", required = true) @RequestParam(value = "hgvsc", required = true) String hgvsc
        , @ApiParam(value = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37", required = false, defaultValue = "GRCh37") @RequestParam(value = "referenceGenome", required = false, defaultValue = "GRCh37") String referenceGenome
        , @ApiParam(value = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma") @RequestParam(value = "tumorType", required = false) String tumorType
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (StringUtils.isEmpty(hgvsc)) {
            throw new ApiHttpErrorException("hgvsc is missing.", HttpStatus.BAD_REQUEST);
        }

        ReferenceGenome matchedRG = resolveMatchedRG(referenceGenome);

        if (!AlterationUtils.isValidHgvsc(hgvsc)) {
            throw new ApiHttpErrorException("hgvsc is invalid.", HttpStatus.BAD_REQUEST);
        }

        AnnotateMutationByHGVScQuery query = new AnnotateMutationByHGVScQuery();
        query.setHgvsc(hgvsc);
        query.setReferenceGenome(matchedRG);
        query.setTumorType(tumorType);
        query.getGermlineQuery().setGermline(true);

        GermlineIndicatorQueryResp indicatorQueryResp = annotateMutationsByHGVSc(Collections.singletonList(query)).get(0);
        return new ResponseEntity<>(indicatorQueryResp, HttpStatus.OK);
    }

    @PublicApi
    @PremiumPublicApi
    @ApiOperation(value = "", notes = "Annotate mutations by HGVSc.", response = GermlineIndicatorQueryResp.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GermlineIndicatorQueryResp.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error, error message will be given.", response = ApiHttpError.class)})
    @RequestMapping(value = "/annotate/germline/mutations/byHGVSc",
        consumes = {"application/json"},
        produces = {"application/json"},
        method = RequestMethod.POST)
    public ResponseEntity<List<GermlineIndicatorQueryResp>> annotateMutationsByHGVScPost(
        @ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody() List<AnnotateMutationByHGVScQuery> body
    ) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (body == null) {
            throw new ApiHttpErrorException("The request body is missing.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(annotateMutationsByHGVSc(body), HttpStatus.OK);
    }

    private ReferenceGenome resolveMatchedRG(String referenceGenome) throws ApiHttpErrorException {
        ReferenceGenome matchedRG = null;
        if (!StringUtils.isEmpty(referenceGenome)) {
            matchedRG = MainUtils.searchEnum(ReferenceGenome.class, referenceGenome);
            if (matchedRG == null) {
                throw new ApiHttpErrorException("referenceGenome \"" + referenceGenome + "\" is an invalid Reference Genome value.", HttpStatus.BAD_REQUEST);
            }
        }
        return matchedRG;
    }

    private List<GermlineIndicatorQueryResp> annotateMutationsByGenomicChange(List<AnnotateMutationByGenomicChangeQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<GermlineIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByGenomicChangeQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByGenomicChangeQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByGenomicChangeQuery query = mutations.get(i);
            query.getGermlineQuery().setGermline(true);
            ReferenceGenome referenceGenome = query.getReferenceGenome();
            if (referenceGenome == null) {
                query.setReferenceGenome(ReferenceGenome.GRCh37);
            }
            if (referenceGenome == ReferenceGenome.GRCh38) {
                grch38Map.put(i, grch38Queries.size());
                grch38Queries.add(query);
            } else {
                grch37Map.put(i, grch37Queries.size());
                grch37Queries.add(query);
            }
        }

        List<GermlineIndicatorQueryResp> grch37Alts = annotateMutationsByGenomicChange(ReferenceGenome.GRCh37, grch37Queries);
        List<GermlineIndicatorQueryResp> grch38Alts = annotateMutationsByGenomicChange(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByGenomicChangeQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<GermlineIndicatorQueryResp> annotateMutationsByGenomicChange(ReferenceGenome referenceGenome, List<AnnotateMutationByGenomicChangeQuery> queries) throws ApiException, org.genome_nexus.ApiException {
        List<GenomicLocation> queriesToGN = new ArrayList<>();
        Map<String, Integer> queryIndexMap = new HashMap<>();
        for (AnnotateMutationByGenomicChangeQuery query : queries) {
            query.getGermlineQuery().setGermline(true);
            GenomicLocation genomicLocation = GenomeNexusUtils.convertGenomicLocation(query.getGenomicLocation());
            if (this.cacheFetcher.genomicLocationShouldBeAnnotated(genomicLocation, referenceGenome)) {
                if (!queryIndexMap.containsKey(query.getGenomicLocation())) {
                    queryIndexMap.put(query.getGenomicLocation(), queriesToGN.size());
                    queriesToGN.add(genomicLocation);
                }
            }
        }

        List<org.genome_nexus.client.VariantAnnotation> variantAnnotations = GenomeNexusUtils.getGenomicLocationVariantsAnnotation(queriesToGN, referenceGenome);
        if (variantAnnotations.size() != queriesToGN.size()) {
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }

        List<GermlineIndicatorQueryResp> result = new ArrayList<>();
        List<Alteration> allAlterations =  AlterationUtils.getAllAlterations();
        for (AnnotateMutationByGenomicChangeQuery query : queries) {
            GermlineIndicatorQueryResp indicatorQueryResp = null;
            if (queryIndexMap.containsKey(query.getGenomicLocation())) {
                VariantAnnotation variantAnnotation = variantAnnotations.get(queryIndexMap.get(query.getGenomicLocation()));
                List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                    Collections.singletonList(variantAnnotation),
                    referenceGenome
                );
                TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty()
                    ? new TranscriptSummaryAlterationResult()
                    : annotatedAlteration.get(0);
                indicatorQueryResp = getIndicatorQueryForCuratedHgvs(
                    query,
                    query.getGermlineQuery(),
                    variantAnnotation.getHgvsg(),
                    selectedAnnotatedAlteration,
                    referenceGenome,
                    allAlterations
                );

                if (indicatorQueryResp == null && !query.getGermlineQuery().isGermline()) {
                    indicatorQueryResp = this.getIndicatorQueryFromGenomicLocation(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        query.getGenomicLocation(),
                        query.getTumorType(),
                        query.getGermlineQuery().getGermline(),
                        new HashSet<>(query.getEvidenceTypes())
                    );
                    indicatorQueryResp.getQuery().setHgvsInfo(selectedAnnotatedAlteration.getMessage());
                }
            }

            if (indicatorQueryResp == null) {
                indicatorQueryResp = this.getIndicatorQueryFromGenomicLocation(
                    query.getReferenceGenome(),
                    new TranscriptSummaryAlterationResult(),
                    query.getGenomicLocation(),
                    query.getTumorType(),
                    query.getGermlineQuery().getGermline(),
                    query.getEvidenceTypes()
                );
            }

            indicatorQueryResp.getQuery().setId(query.getId());
            result.add(indicatorQueryResp);
        }
        return result;
    }

    private List<GermlineIndicatorQueryResp> annotateMutationsByHGVSg(List<AnnotateMutationByHGVSgQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<GermlineIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByHGVSgQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByHGVSgQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVSgQuery query = mutations.get(i);
            query.getGermlineQuery().setGermline(true);
            ReferenceGenome referenceGenome = query.getReferenceGenome();
            if (referenceGenome == null) {
                referenceGenome = ReferenceGenome.GRCh37;
            }
            query.setReferenceGenome(referenceGenome);
            if (referenceGenome == ReferenceGenome.GRCh38) {
                grch38Map.put(i, grch38Queries.size());
                grch38Queries.add(query);
            } else {
                grch37Map.put(i, grch37Queries.size());
                grch37Queries.add(query);
            }
        }

        List<GermlineIndicatorQueryResp> grch37Alts = annotateMutationsByHGVSg(ReferenceGenome.GRCh37, grch37Queries);
        List<GermlineIndicatorQueryResp> grch38Alts = annotateMutationsByHGVSg(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVSgQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<GermlineIndicatorQueryResp> annotateMutationsByHGVSg(ReferenceGenome referenceGenome, List<AnnotateMutationByHGVSgQuery> queries) throws ApiException, org.genome_nexus.ApiException {
        List<String> queriesToGN = new ArrayList<>();
        Map<String, Integer> queryIndexMap = new HashMap<>();
        for (AnnotateMutationByHGVSgQuery query : queries) {
            query.getGermlineQuery().setGermline(true);
            String hgvsg = query.getHgvsg();
            if (this.cacheFetcher.hgvsgShouldBeAnnotated(hgvsg, referenceGenome)) {
                if (!queryIndexMap.containsKey(query.getHgvsg())) {
                    queryIndexMap.put(hgvsg, queriesToGN.size());
                    queriesToGN.add(hgvsg);
                }
            }
        }

        List<org.genome_nexus.client.VariantAnnotation> variantAnnotations = GenomeNexusUtils.getHgvsVariantsAnnotation(queriesToGN, referenceGenome);
        if (variantAnnotations.size() != queriesToGN.size()) {
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }

        List<GermlineIndicatorQueryResp> result = new ArrayList<>();
        List<Alteration> allAlterations = AlterationUtils.getAllAlterations();
        for (AnnotateMutationByHGVSgQuery query : queries) {
            GermlineIndicatorQueryResp indicatorQueryResp = null;
            if (queryIndexMap.containsKey(query.getHgvsg())) {
                VariantAnnotation variantAnnotation = variantAnnotations.get(queryIndexMap.get(query.getHgvsg()));
                List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                    Collections.singletonList(variantAnnotation),
                    referenceGenome
                );
                TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty()
                    ? new TranscriptSummaryAlterationResult()
                    : annotatedAlteration.get(0);
                indicatorQueryResp = getIndicatorQueryForCuratedHgvs(
                    query,
                    query.getGermlineQuery(),
                    variantAnnotation.getHgvsg(),
                    selectedAnnotatedAlteration,
                    referenceGenome,
                    allAlterations
                );

                if (indicatorQueryResp == null && !query.getGermlineQuery().isGermline()) {
                    indicatorQueryResp = this.getIndicatorQueryFromHGVS(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        variantAnnotation.getHgvsg(),
                        query.getTumorType(),
                        query.getGermlineQuery().getGermline(),
                        new HashSet<>(query.getEvidenceTypes())
                    );
                    indicatorQueryResp.getQuery().setHgvsInfo(selectedAnnotatedAlteration.getMessage());
                }
            }

            if (indicatorQueryResp == null) {
                indicatorQueryResp = this.getIndicatorQueryFromHGVS(
                    query.getReferenceGenome(),
                    new TranscriptSummaryAlterationResult(),
                    query.getHgvsg(),
                    query.getTumorType(),
                    query.getGermlineQuery().getGermline(),
                    query.getEvidenceTypes()
                );
            }
            indicatorQueryResp.getQuery().setId(query.getId());
            result.add(indicatorQueryResp);
        }
        return result;
    }

    private GermlineIndicatorQueryResp getIndicatorQueryForCuratedHgvs(
        AnnotationQuery query,
        GermlineQuery germlineQuery,
        String hgvsg,
        TranscriptSummaryAlterationResult selectedAnnotatedAlteration,
        ReferenceGenome referenceGenome,
        List<Alteration> allAlterations
    ) throws org.genome_nexus.ApiException {
        Alteration alteration = AlterationUtils.findAlterationWithGeneticType(
            referenceGenome,
            hgvsg,
            allAlterations,
            germlineQuery.isGermline()
        );
        if (alteration != null && alteration.getForGermline() == germlineQuery.isGermline()) {
            Query indicatorQuery = new Query(
                null,
                referenceGenome,
                null,
                alteration.getGene().getHugoSymbol(),
                hgvsg,
                null,
                null,
                query.getTumorType(),
                null,
                null,
                null,
                hgvsg,
                true,
                null,
                null
            );
            return IndicatorUtils2.processQueryGermline(indicatorQuery, null, false, query.getEvidenceTypes(), false);
        }

        alteration = null;
        String hgvsc = null;
        if (selectedAnnotatedAlteration.getTranscriptConsequenceSummary() != null) {
            hgvsc = selectedAnnotatedAlteration.getTranscriptConsequenceSummary().getHgvsc();
        }
        if (StringUtils.isNotEmpty(hgvsc)) {
            alteration = null;
            String[] hgvscParts = hgvsc.split(":");
            if (hgvscParts.length == 2) {
                hgvsc = hgvscParts[1];
                alteration = AlterationUtils.findAlterationWithGeneticType(
                    referenceGenome,
                    hgvsc,
                    allAlterations,
                    germlineQuery.isGermline()
                );
            }
        }
        if (alteration != null && alteration.getForGermline() == germlineQuery.isGermline()) {
            Query indicatorQuery = new Query(
                null,
                referenceGenome,
                null,
                alteration.getGene().getHugoSymbol(),
                hgvsc,
                null,
                null,
                query.getTumorType(),
                null,
                null,
                null,
                hgvsg,
                true,
                null,
                null
            );
            return IndicatorUtils2.processQueryGermline(indicatorQuery, null, false, query.getEvidenceTypes(), false);
        }

        return null;
    }

    private List<GermlineIndicatorQueryResp> annotateMutationsByHGVSc(List<AnnotateMutationByHGVScQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<GermlineIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByHGVScQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByHGVScQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVScQuery query = mutations.get(i);
            query.getGermlineQuery().setGermline(true);
            ReferenceGenome referenceGenome = query.getReferenceGenome();
            if (referenceGenome == null) {
                referenceGenome = ReferenceGenome.GRCh37;
            }
            query.setReferenceGenome(referenceGenome);
            if (referenceGenome == ReferenceGenome.GRCh38) {
                grch38Map.put(i, grch38Queries.size());
                grch38Queries.add(query);
            } else {
                grch37Map.put(i, grch37Queries.size());
                grch37Queries.add(query);
            }
        }

        List<GermlineIndicatorQueryResp> grch37Alts = annotateMutationsByHGVSc(ReferenceGenome.GRCh37, grch37Queries);
        List<GermlineIndicatorQueryResp> grch38Alts = annotateMutationsByHGVSc(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVScQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<GermlineIndicatorQueryResp> annotateMutationsByHGVSc(ReferenceGenome referenceGenome, List<AnnotateMutationByHGVScQuery> queries) throws ApiException, org.genome_nexus.ApiException {
        List<GermlineIndicatorQueryResp> result = new ArrayList<>();

        List<String> queriesToGN = new ArrayList<>();
        Map<String, Integer> queryToGNIndexMap = new HashMap<>();
        AnnotateMutationByHGVScQuery[] resultIndexToQuery = new AnnotateMutationByHGVScQuery[queries.size()];

        List<Alteration> allAlterations = AlterationUtils.getAllAlterations();

        for (int i = 0; i < queries.size(); i++) {
            AnnotateMutationByHGVScQuery query = queries.get(i);
            query.getGermlineQuery().setGermline(true);
            String hgvsc = query.getHgvsc();

            if (this.cacheFetcher.hgvscShouldBeAnnotated(hgvsc)) {
                Alteration alteration = AlterationUtils.findAlterationWithGeneticType(
                    referenceGenome,
                    query.getAlteration(),
                    allAlterations,
                    query.getGermlineQuery().isGermline()
                );

                if (query.getGermlineQuery().isGermline() || (alteration != null && alteration.getForGermline() == false)) {
                    Query indicatorQuery = new Query(
                        query.getId(),
                        query.getReferenceGenome(),
                        null,
                        query.getGene(),
                        query.getAlteration(),
                        null,
                        null,
                        query.getTumorType(),
                        null,
                        null,
                        null,
                        null,
                        true,
                        null,
                        null
                    );
                    GermlineIndicatorQueryResp germlineResp = IndicatorUtils2.processQueryGermline(
                        indicatorQuery,
                        null,
                        false,
                        query.getEvidenceTypes(),
                        false
                    );
                    germlineResp.getQuery().setId(query.getId());
                    result.add(germlineResp);
                } else {
                    if (!queryToGNIndexMap.containsKey(query.getHgvsc())) {
                        queryToGNIndexMap.put(hgvsc, queriesToGN.size());
                        Gene gene = GeneUtils.getGeneByAlias(query.getGene());
                        if (gene != null) {
                            queriesToGN.add(gene.getHugoSymbol() + ":" + query.getAlteration());
                        } else {
                            queriesToGN.add(hgvsc);
                        }
                    }
                    resultIndexToQuery[result.size()] = query;
                    result.add(null);
                }
            } else {
                GermlineIndicatorQueryResp resp = this.getIndicatorQueryFromHGVS(
                    query.getReferenceGenome(),
                    new TranscriptSummaryAlterationResult(),
                    query.getHgvsc(),
                    query.getTumorType(),
                    query.getGermlineQuery().getGermline(),
                    query.getEvidenceTypes()
                );
                resp.getQuery().setId(query.getId());
                result.add(resp);
            }
        }

        List<org.genome_nexus.client.VariantAnnotation> variantAnnotations = GenomeNexusUtils.getHgvsVariantsAnnotation(queriesToGN, referenceGenome);
        if (variantAnnotations.size() != queriesToGN.size()) {
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }

        for (int i = 0; i < result.size(); i++) {
            GermlineIndicatorQueryResp indicatorQueryResp = null;
            if (result.get(i) == null) {
                AnnotateMutationByHGVScQuery query = resultIndexToQuery[i];
                if (queryToGNIndexMap.containsKey(query.getHgvsc())) {
                    VariantAnnotation variantAnnotation = variantAnnotations.get(queryToGNIndexMap.get(query.getHgvsc()));
                    List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                        Collections.singletonList(variantAnnotation),
                        referenceGenome
                    );
                    TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty()
                        ? new TranscriptSummaryAlterationResult()
                        : annotatedAlteration.get(0);
                    indicatorQueryResp = this.getIndicatorQueryFromHGVS(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        variantAnnotation.getHgvsg(),
                        query.getTumorType(),
                        query.getGermlineQuery().getGermline(),
                        new HashSet<>(query.getEvidenceTypes())
                    );
                    indicatorQueryResp.getQuery().setHgvsInfo(selectedAnnotatedAlteration.getMessage());
                    indicatorQueryResp.getQuery().setId(query.getId());
                }
                result.set(i, indicatorQueryResp);
            }
        }
        return result;
    }

    private GermlineIndicatorQueryResp getIndicatorQueryFromGenomicLocation(
        ReferenceGenome referenceGenome,
        TranscriptSummaryAlterationResult transcriptSummaryAlterationResult,
        String hgvs,
        String tumorType,
        Boolean germline,
        Set<EvidenceType> evidenceTypes
    ) {
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, transcriptSummaryAlterationResult, hgvs);
        query.setGermline(Boolean.TRUE.equals(germline));

        GermlineIndicatorQueryResp indicatorQueryResp = IndicatorUtils2.processQueryGermline(
            query,
            null,
            false,
            evidenceTypes,
            false
        );

        // Indicate which transcript was used to generate the annotation
        TranscriptConsequenceSummary summary = transcriptSummaryAlterationResult.getTranscriptConsequenceSummary();
        addTranscriptAndExonToResponse(indicatorQueryResp, summary);
        indicatorQueryResp.getQuery().setHgvsInfo(transcriptSummaryAlterationResult.getMessage());
        return indicatorQueryResp;
    }

    private GermlineIndicatorQueryResp getIndicatorQueryFromHGVS(
        ReferenceGenome referenceGenome,
        TranscriptSummaryAlterationResult transcriptSummaryAlterationResult,
        String hgvs,
        String tumorType,
        Boolean germline,
        Set<EvidenceType> evidenceTypes
    ) {
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, transcriptSummaryAlterationResult, hgvs);
        query.setGermline(Boolean.TRUE.equals(germline));

        GermlineIndicatorQueryResp indicatorQueryResp = IndicatorUtils2.processQueryGermline(
            query,
            null,
            false,
            evidenceTypes,
            false
        );

        // Indicate which transcript was used to generate the annotation
        TranscriptConsequenceSummary summary = transcriptSummaryAlterationResult.getTranscriptConsequenceSummary();
        addTranscriptAndExonToResponse(indicatorQueryResp, summary);
        indicatorQueryResp.getQuery().setHgvsInfo(transcriptSummaryAlterationResult.getMessage());
        return indicatorQueryResp;
    }

    private void addTranscriptAndExonToResponse(GermlineIndicatorQueryResp response, TranscriptConsequenceSummary summary) {
        if (summary != null) {
            if (StringUtils.isNotEmpty(summary.getTranscriptId())) {
                response.getQuery().setCanonicalTranscript(summary.getTranscriptId());
            }
            if (StringUtils.isNotEmpty(summary.getExon())) {
                response.setExon(StringUtils.substringBefore(summary.getExon(), "/"));
            }
        }
    }

}
