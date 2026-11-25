package org.mskcc.cbio.oncokb.service;

import org.apache.commons.lang3.StringUtils;
import org.genome_nexus.client.GenomicLocation;
import org.genome_nexus.client.TranscriptConsequenceSummary;
import org.genome_nexus.client.VariantAnnotation;
import org.mskcc.cbio.oncokb.api.pub.v1.ApiControllerUtils;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByGenomicChangeQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByHGVScQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByHGVSgQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotationQuery;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.GermlineIndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.InheritanceMechanism;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.genomeNexus.TranscriptSummaryAlterationResult;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.GenomeNexusUtils;
import org.mskcc.cbio.oncokb.util.IndicatorUtils;
import org.mskcc.cbio.oncokb.util.QueryUtils;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Germline variant annotation shared by the REST controller ({@code GermlineAnnotationsApiController})
 * and the MCP endpoint, so both return identical annotations. Single-query methods hold the
 * validation the REST GET endpoints perform; list methods back the POST endpoints.
 */
@Service
public class GermlineAnnotationService {
    private static final boolean GERMLINE = true;

    @Autowired
    CacheFetcher cacheFetcher;

    public GermlineIndicatorQueryResp annotateMutationByGenomicChange(String genomicLocation, String referenceGenome, String tumorType) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        if (StringUtils.isEmpty(genomicLocation)) {
            throw new ApiHttpErrorException("genomicLocation is missing.", HttpStatus.BAD_REQUEST);
        }

        ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

        AnnotateMutationByGenomicChangeQuery query = new AnnotateMutationByGenomicChangeQuery();
        query.setGenomicLocation(genomicLocation);
        query.setReferenceGenome(matchedRG);
        query.setTumorType(tumorType);
        GermlineIndicatorQueryResp indicatorQueryResp = annotateMutationsByGenomicChange(Collections.singletonList(query)).get(0);

        return indicatorQueryResp;
    }

    public GermlineIndicatorQueryResp annotateMutationByHGVSg(String hgvsg, String referenceGenome, String tumorType) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        hgvsg = StringUtils.trim(hgvsg);

        if (StringUtils.isEmpty(hgvsg)) {
            throw new ApiHttpErrorException("hgvsg is missing.", HttpStatus.BAD_REQUEST);
        }

        ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

        if (!AlterationUtils.isValidHgvsg(hgvsg)) {
            throw new ApiHttpErrorException("hgvsg is invalid.", HttpStatus.BAD_REQUEST);
        }

        AnnotateMutationByHGVSgQuery query = new AnnotateMutationByHGVSgQuery();
        query.setHgvsg(hgvsg);
        query.setReferenceGenome(matchedRG);
        query.setTumorType(tumorType);
        GermlineIndicatorQueryResp indicatorQueryResp = annotateMutationsByHGVSg(Collections.singletonList(query)).get(0);
        return indicatorQueryResp;
    }

    public GermlineIndicatorQueryResp annotateMutationByHGVSc(String hgvsc, String referenceGenome, String tumorType, List<InheritanceMechanism> inheritanceMechanisms) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        hgvsc = StringUtils.trim(hgvsc);

        if (StringUtils.isEmpty(hgvsc)) {
            throw new ApiHttpErrorException("hgvsc is missing.", HttpStatus.BAD_REQUEST);
        }

        ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

        if (!AlterationUtils.isValidHgvsc(hgvsc)) {
            throw new ApiHttpErrorException("hgvsc is invalid.", HttpStatus.BAD_REQUEST);
        }

        AnnotateMutationByHGVScQuery query = new AnnotateMutationByHGVScQuery();
        query.setHgvsc(hgvsc);
        query.setReferenceGenome(matchedRG);
        query.setTumorType(tumorType);
        query.setInheritanceMechanisms(inheritanceMechanisms);

        GermlineIndicatorQueryResp indicatorQueryResp = annotateMutationsByHGVSc(Collections.singletonList(query)).get(0);
        return indicatorQueryResp;
    }

    public List<GermlineIndicatorQueryResp> annotateMutationsByGenomicChange(List<AnnotateMutationByGenomicChangeQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<GermlineIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByGenomicChangeQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByGenomicChangeQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByGenomicChangeQuery query = mutations.get(i);
            query.setGermline(GERMLINE);
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
                    referenceGenome,
                    query.isGermline()
                );
                TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty()
                    ? new TranscriptSummaryAlterationResult()
                    : annotatedAlteration.get(0);
                indicatorQueryResp = getIndicatorQueryForCuratedHgvs(
                    query,
                    variantAnnotation.getHgvsg(),
                    selectedAnnotatedAlteration,
                    referenceGenome,
                    allAlterations
                );

                if (indicatorQueryResp == null) {
                    indicatorQueryResp = this.getIndicatorQueryFromGenomicLocation(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        query.getGenomicLocation(),
                        query.getTumorType(),
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
                    query.getEvidenceTypes()
                );
            }

            indicatorQueryResp.getQuery().setId(query.getId());
            result.add(indicatorQueryResp);
        }
        return result;
    }

    public List<GermlineIndicatorQueryResp> annotateMutationsByHGVSg(List<AnnotateMutationByHGVSgQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<GermlineIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByHGVSgQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByHGVSgQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVSgQuery query = mutations.get(i);
            query.setHgvsg(StringUtils.trim(query.getHgvsg()));
            query.setGermline(GERMLINE);
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
                    referenceGenome,
                    query.isGermline()
                );
                TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty()
                    ? new TranscriptSummaryAlterationResult()
                    : annotatedAlteration.get(0);
                indicatorQueryResp = getIndicatorQueryForCuratedHgvs(
                    query,
                    variantAnnotation.getHgvsg(),
                    selectedAnnotatedAlteration,
                    referenceGenome,
                    allAlterations
                );

                if (indicatorQueryResp == null) {
                    indicatorQueryResp = this.getIndicatorQueryFromHGVS(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        variantAnnotation.getHgvsg(),
                        query.getTumorType(),
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
        String hgvsg,
        TranscriptSummaryAlterationResult selectedAnnotatedAlteration,
        ReferenceGenome referenceGenome,
        List<Alteration> allAlterations
    ) throws org.genome_nexus.ApiException {
        Alteration alteration = AlterationUtils.findAlterationWithGeneticType(
            referenceGenome,
            selectedAnnotatedAlteration.getHgvscAlteration().getGene(),
            hgvsg,
            allAlterations,
            GERMLINE
        );
        if (alteration != null && Boolean.TRUE.equals(alteration.getForGermline())) {
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
                GERMLINE,
                null,
                null
            );
            GermlineIndicatorQueryResp response = this.cacheFetcher.processQueryGermline(
                indicatorQuery.getReferenceGenome(),
                indicatorQuery.getEntrezGeneId(),
                indicatorQuery.getHugoSymbol(),
                indicatorQuery.getAlteration(),
                indicatorQuery.getAlterationType(),
                indicatorQuery.getTumorType(),
                indicatorQuery.getConsequence(),
                indicatorQuery.getProteinStart(),
                indicatorQuery.getProteinEnd(),
                indicatorQuery.getSvType(),
                indicatorQuery.getHgvs(),
                indicatorQuery.getInheritanceMechanism(),
                indicatorQuery.getPathogenicity(),
                null,
                false,
                query.getEvidenceTypes(),
                false
            );
            addTranscriptAndExonToResponse(response, selectedAnnotatedAlteration.getTranscriptConsequenceSummary());
            return response;
        }

        alteration = null;
        String hgvsc = selectedAnnotatedAlteration.getHgvscAlteration().getAlteration();
        if (StringUtils.isNotEmpty(hgvsc)) {
            alteration = AlterationUtils.findAlterationWithGeneticType(
                referenceGenome,
                selectedAnnotatedAlteration.getHgvscAlteration().getGene(),
                hgvsc,
                allAlterations,
                GERMLINE
            );
        }
        if (alteration != null && Boolean.TRUE.equals(alteration.getForGermline())) {
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
                GERMLINE,
                null,
                null
            );
            GermlineIndicatorQueryResp response = this.cacheFetcher.processQueryGermline(
                indicatorQuery.getReferenceGenome(),
                indicatorQuery.getEntrezGeneId(),
                indicatorQuery.getHugoSymbol(),
                indicatorQuery.getAlteration(),
                indicatorQuery.getAlterationType(),
                indicatorQuery.getTumorType(),
                indicatorQuery.getConsequence(),
                indicatorQuery.getProteinStart(),
                indicatorQuery.getProteinEnd(),
                indicatorQuery.getSvType(),
                indicatorQuery.getHgvs(),
                indicatorQuery.getInheritanceMechanism(),
                indicatorQuery.getPathogenicity(),
                null,
                false,
                query.getEvidenceTypes(),
                false
            );
            addTranscriptAndExonToResponse(response, selectedAnnotatedAlteration.getTranscriptConsequenceSummary());
            return response;
        }

        return null;
    }

    public List<GermlineIndicatorQueryResp> annotateMutationsByHGVSc(List<AnnotateMutationByHGVScQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<GermlineIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByHGVScQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByHGVScQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVScQuery query = mutations.get(i);
            query.setHgvsc(StringUtils.trim(query.getHgvsc()));
            query.setGermline(GERMLINE);
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
            GermlineIndicatorQueryResp indicatorQueryResp = query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i));
            indicatorQueryResp.setGenomicIndicators(IndicatorUtils.filterGenomicIndicatorsByInheritanceMechanisms(indicatorQueryResp.getGenomicIndicators(), query.getInheritanceMechanisms()));
            result.add(indicatorQueryResp);
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
            String hgvsc = query.getHgvsc();

            // Malformed or otherwise unannotatable hgvsc: return an empty result without hitting GN.
            if (!this.cacheFetcher.hgvscShouldBeAnnotated(hgvsc)) {
                result.add(emptyHgvscResult(query));
                continue;
            }

            // Already in the OncoKB database: annotate directly, no Genome Nexus needed.
            Alteration alteration = AlterationUtils.findAlterationWithGeneticType(
                referenceGenome,
                GeneUtils.getGeneByHugoSymbol(query.getGene()),
                query.getAlteration(),
                allAlterations,
                query.isGermline()
            );
            if (alteration != null) {
                result.add(annotateHgvscFromDatabase(query));
                continue;
            }

            // We only annotate on the OncoKB canonical transcript, so without a known gene and its
            // canonical transcript there is nothing for Genome Nexus to resolve the cDNA change
            // against that we would actually annotate on. Skip the GN call and return an empty result.
            Gene gene = GeneUtils.getGeneByHugoSymbol(query.getGene());
            String isoform = GenomeNexusUtils.getIsoform(gene, referenceGenome);
            if (gene == null || StringUtils.isEmpty(isoform)) {
                result.add(emptyHgvscResult(query));
                continue;
            }

            // Resolve through Genome Nexus. A cDNA change can exist on multiple transcripts of the same
            // gene, and if we only give GN the hugo symbol we cannot control which one it picks, so swap
            // it out for the OncoKB canonical transcript id so GN resolves the transcript we annotate on.
            if (!queryToGNIndexMap.containsKey(hgvsc)) {
                queryToGNIndexMap.put(hgvsc, queriesToGN.size());
                queriesToGN.add(isoform + ":" + query.getAlteration());
            }
            resultIndexToQuery[result.size()] = query;
            result.add(null);
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
                        referenceGenome,
                        query.isGermline()
                    );
                    TranscriptSummaryAlterationResult selectedAnnotatedAlteration = annotatedAlteration.isEmpty()
                        ? new TranscriptSummaryAlterationResult()
                        : annotatedAlteration.get(0);
                    indicatorQueryResp = this.getIndicatorQueryFromHGVS(
                        query.getReferenceGenome(),
                        selectedAnnotatedAlteration,
                        variantAnnotation.getHgvsg(),
                        query.getTumorType(),
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

    private GermlineIndicatorQueryResp emptyHgvscResult(AnnotateMutationByHGVScQuery query) {
        GermlineIndicatorQueryResp resp = this.getIndicatorQueryFromHGVS(
            query.getReferenceGenome(),
            new TranscriptSummaryAlterationResult(),
            query.getHgvsc(),
            query.getTumorType(),
            query.getEvidenceTypes()
        );
        resp.getQuery().setId(query.getId());
        return resp;
    }

    private GermlineIndicatorQueryResp annotateHgvscFromDatabase(AnnotateMutationByHGVScQuery query) {
        GermlineIndicatorQueryResp germlineResp = this.cacheFetcher.processQueryGermline(
            query.getReferenceGenome(),
            null,
            query.getGene(),
            query.getAlteration(),
            null,
            query.getTumorType(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            query.getEvidenceTypes(),
            false
        );
        Gene matchedGene = GeneUtils.getGene(germlineResp.getQuery().getEntrezGeneId(), germlineResp.getQuery().getHugoSymbol());
        if (matchedGene != null) {
            if (ReferenceGenome.GRCh38.equals(germlineResp.getQuery().getReferenceGenome())) {
                germlineResp.getQuery().setCanonicalTranscript(matchedGene.getGrch38Isoform());
            } else {
                germlineResp.getQuery().setCanonicalTranscript(matchedGene.getGrch37Isoform());
            }
        }
        germlineResp.getQuery().setId(query.getId());
        return germlineResp;
    }

    private GermlineIndicatorQueryResp getIndicatorQueryFromGenomicLocation(
        ReferenceGenome referenceGenome,
        TranscriptSummaryAlterationResult transcriptSummaryAlterationResult,
        String hgvs,
        String tumorType,
        Set<EvidenceType> evidenceTypes
    ) {
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, transcriptSummaryAlterationResult, hgvs, GERMLINE);
        query.setGermline(GERMLINE);

        GermlineIndicatorQueryResp indicatorQueryResp = this.cacheFetcher.processQueryGermline(
            query.getReferenceGenome(),
            query.getEntrezGeneId(),
            query.getHugoSymbol(),
            query.getAlteration(),
            query.getAlterationType(),
            query.getTumorType(),
            query.getConsequence(),
            query.getProteinStart(),
            query.getProteinEnd(),
            query.getSvType(),
            query.getHgvs(),
            query.getInheritanceMechanism(),
            query.getPathogenicity(),
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
        Set<EvidenceType> evidenceTypes
    ) {
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, transcriptSummaryAlterationResult, hgvs, GERMLINE);
        query.setGermline(GERMLINE);

        GermlineIndicatorQueryResp indicatorQueryResp = this.cacheFetcher.processQueryGermline(
            query.getReferenceGenome(),
            query.getEntrezGeneId(),
            query.getHugoSymbol(),
            query.getAlteration(),
            query.getAlterationType(),
            query.getTumorType(),
            query.getConsequence(),
            query.getProteinStart(),
            query.getProteinEnd(),
            query.getSvType(),
            query.getHgvs(),
            query.getInheritanceMechanism(),
            query.getPathogenicity(),
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
        }
    }
}
