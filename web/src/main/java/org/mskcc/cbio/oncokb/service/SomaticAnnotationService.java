package org.mskcc.cbio.oncokb.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.genome_nexus.client.GenomicLocation;
import org.genome_nexus.client.TranscriptConsequenceSummary;
import org.genome_nexus.client.VariantAnnotation;
import org.mskcc.cbio.oncokb.api.pub.v1.ApiControllerUtils;
import org.mskcc.cbio.oncokb.apiModels.annotation.*;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.genomeNexus.TranscriptSummaryAlterationResult;
import org.mskcc.cbio.oncokb.util.*;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Somatic variant annotation shared by the REST controller ({@code AnnotationsApiController}) and the
 * MCP endpoint, so both return identical annotations. Single-query methods hold the validation the
 * REST GET endpoints perform; list methods back the POST endpoints.
 */
@Service
public class SomaticAnnotationService {
    private static final boolean SOMATIC = false;

    @Autowired
    CacheFetcher cacheFetcher;

    public SomaticIndicatorQueryResp annotateMutationByProteinChange(String hugoSymbol, Integer entrezGeneId, String proteinChange, String referenceGenome, String consequence, Integer proteinStart, Integer proteinEnd, String tumorType, String evidenceTypes) throws ApiHttpErrorException {
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            throw new ApiHttpErrorException("entrezGeneId \"" + entrezGeneId + "\"" + " and hugoSymbol \"" + hugoSymbol + "\" are not the same gene.", HttpStatus.BAD_REQUEST);
        } else {
            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);
            Query query = new Query(null, matchedRG, entrezGeneId, hugoSymbol, proteinChange, null, null, tumorType, consequence, proteinStart, proteinEnd, null, SOMATIC, null, null);
            indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
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
                null,
                null,
                false,
                new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")),
                false
            );
        }

        return indicatorQueryResp;
    }

    public SomaticIndicatorQueryResp annotateMutationByGenomicChange(String genomicLocation, String referenceGenome, String tumorType, String evidenceTypes) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (StringUtils.isEmpty(genomicLocation)) {
            throw new ApiHttpErrorException("genomicLocation is missing.", HttpStatus.BAD_REQUEST);
        }

        ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);
        
        AnnotateMutationByGenomicChangeQuery query = new AnnotateMutationByGenomicChangeQuery();
        query.setGenomicLocation(genomicLocation);
        query.setReferenceGenome(matchedRG);
        query.setTumorType(tumorType);
        query.setGermline(SOMATIC);
        query.setEvidenceTypes(new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")));

        indicatorQueryResp = annotateMutationsByGenomicChange(Collections.singletonList(query)).get(0);

        return indicatorQueryResp;
    }

    public SomaticIndicatorQueryResp annotateMutationByHGVSg(String hgvsg, String referenceGenome, String tumorType, String evidenceTypes) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        SomaticIndicatorQueryResp indicatorQueryResp = null;
        hgvsg = StringUtils.trim(hgvsg);

        if (StringUtils.isEmpty(hgvsg)) {
            throw new ApiHttpErrorException("hgvsg is missing.", HttpStatus.BAD_REQUEST);
        } else {
            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

            if (!AlterationUtils.isValidHgvsg(hgvsg)) {
                throw new ApiHttpErrorException("hgvsg is invalid.", HttpStatus.BAD_REQUEST);
            }
            
            AnnotateMutationByHGVSgQuery query = new AnnotateMutationByHGVSgQuery();
            query.setHgvsg(hgvsg);
            query.setReferenceGenome(matchedRG);
            query.setTumorType(tumorType);
            query.setGermline(SOMATIC);
            query.setEvidenceTypes(new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")));

            indicatorQueryResp = annotateMutationsByHGVSg(Collections.singletonList(query)).get(0);
        }
        return indicatorQueryResp;
    }

    public SomaticIndicatorQueryResp annotateMutationByHGVSc(String hgvsc, String referenceGenome, String tumorType) throws ApiException, org.genome_nexus.ApiException, ApiHttpErrorException {
        SomaticIndicatorQueryResp indicatorQueryResp = null;
        hgvsc = StringUtils.trim(hgvsc);

        if (StringUtils.isEmpty(hgvsc)) {
            throw new ApiHttpErrorException("hgvsc is missing.", HttpStatus.BAD_REQUEST);
        } else {
            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

            if (!AlterationUtils.isValidHgvsc(hgvsc)) {
                throw new ApiHttpErrorException("hgvsc is invalid.", HttpStatus.BAD_REQUEST);
            }
            
            AnnotateMutationByHGVScQuery query = new AnnotateMutationByHGVScQuery();
            query.setHgvsc(hgvsc);
            query.setReferenceGenome(matchedRG);
            query.setTumorType(tumorType);
            query.setGermline(SOMATIC);

            indicatorQueryResp = annotateMutationsByHGVSc(Collections.singletonList(query)).get(0);
        }
        return indicatorQueryResp;
    }

    public SomaticIndicatorQueryResp annotateCopyNumberAlteration(String hugoSymbol, Integer entrezGeneId, CopyNumberAlterationType copyNameAlterationType, String referenceGenome, String tumorType, String evidenceTypes) throws ApiHttpErrorException {
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            throw new ApiHttpErrorException("entrezGeneId \"" + entrezGeneId + "\"" + " and hugoSymbol \"" + hugoSymbol + "\" are not the same gene.", HttpStatus.BAD_REQUEST);
        } else {
            if (copyNameAlterationType == null) {
                throw new ApiHttpErrorException("copyNameAlterationType is missing.", HttpStatus.BAD_REQUEST);
            }
            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);
            indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
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
                null,
                null,
                false,
                new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")),
                false);
        }
        return indicatorQueryResp;
    }

    public SomaticIndicatorQueryResp annotateStructuralVariant(String hugoSymbolA, Integer entrezGeneIdA, String hugoSymbolB, Integer entrezGeneIdB, StructuralVariantType structuralVariantType, Boolean isFunctionalFusion, String referenceGenome, String tumorType, String evidenceTypes) throws ApiHttpErrorException {
        SomaticIndicatorQueryResp indicatorQueryResp = null;

        if (structuralVariantType == null) {
            throw new ApiHttpErrorException("structuralVariantType is missing.", HttpStatus.BAD_REQUEST);
        } else if (isFunctionalFusion == null) {
            throw new ApiHttpErrorException("isFunctionalFusion is missing.", HttpStatus.BAD_REQUEST);
        }
        if (entrezGeneIdA != null && hugoSymbolA != null && !GeneUtils.isSameGene(entrezGeneIdA, hugoSymbolA)) {
            throw new ApiHttpErrorException("entrezGeneIdA \"" + entrezGeneIdA + "\"" + " and hugoSymbolA \"" + hugoSymbolA + "\" are not the same gene.", HttpStatus.BAD_REQUEST);
        } else if (entrezGeneIdB != null && hugoSymbolB != null && !GeneUtils.isSameGene(entrezGeneIdB, hugoSymbolB)) {
            throw new ApiHttpErrorException("entrezGeneIdB \"" + entrezGeneIdB + "\"" + " and hugoSymbolB \"" + hugoSymbolB + "\" are not the same gene.", HttpStatus.BAD_REQUEST);
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

            ReferenceGenome matchedRG = ApiControllerUtils.resolveMatchedRG(referenceGenome);

            String fusionName = FusionUtils.getFusionName(geneA, geneB);
            indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
                matchedRG, null, fusionName, null, AlterationType.STRUCTURAL_VARIANT.name(), tumorType, isFunctionalFusion ? "fusion" : null, null, null, structuralVariantType, null,
                null, null, null, false, new HashSet<>(MainUtils.stringToEvidenceTypes(evidenceTypes, ",")), false);
        }
        return indicatorQueryResp;
    }

    private SomaticIndicatorQueryResp getIndicatorQueryFromGenomicLocation(
        ReferenceGenome referenceGenome,
        TranscriptSummaryAlterationResult transcriptSummaryAlterationResult,
        String hgvs,
        String tumorType,
        Set<EvidenceType> evidenceTypes
    ) {
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, transcriptSummaryAlterationResult, hgvs, SOMATIC);
        query.setGermline(SOMATIC);

        SomaticIndicatorQueryResp indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
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
            null,
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

    private SomaticIndicatorQueryResp getIndicatorQueryFromHGVS(
        ReferenceGenome referenceGenome,
        TranscriptSummaryAlterationResult transcriptSummaryAlterationResult,
        String hgvs,
        String tumorType,
        Set<EvidenceType> evidenceTypes
    ) {
        Query query = QueryUtils.getQueryFromAlteration(referenceGenome, tumorType, transcriptSummaryAlterationResult, hgvs, SOMATIC);
        query.setGermline(SOMATIC);

        SomaticIndicatorQueryResp indicatorQueryResp = this.cacheFetcher.processQuerySomatic(
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
            null,
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

    public SampleQueryResp annotateSample(AnnotateSampleQuery sample) throws ApiException, org.genome_nexus.ApiException {
        SampleQueryResp annotatedSample = new SampleQueryResp();
        annotatedSample.setId(sample.getId());

        List<SomaticIndicatorQueryResp> structuralVariants = new ArrayList<>();
        List<SomaticIndicatorQueryResp> copyNumberAlterations = new ArrayList<>();
        List<SomaticIndicatorQueryResp> genomicChange = new ArrayList<>();
        List<SomaticIndicatorQueryResp> cDnaChange = new ArrayList<>();
        List<SomaticIndicatorQueryResp> proteinChange = new ArrayList<>();
        List<SomaticIndicatorQueryResp> hgvsg = new ArrayList<>();

        String tumorType = sample.getTumorType();
        annotatedSample.setTumorType(tumorType);
        if (sample.getStructuralVariants() != null) {
            setTumorTypeForQueries(sample.getStructuralVariants(), tumorType);
            structuralVariants = annotateStructuralVariants(sample.getStructuralVariants());
        }
        if (sample.getCopyNumberAlterations() != null) {
            setTumorTypeForQueries(sample.getCopyNumberAlterations(), tumorType);
            copyNumberAlterations = annotateCopyNumberAlterations(sample.getCopyNumberAlterations());
        }
        if (sample.getMutations() != null) {
            if (sample.getMutations().getGenomicChange() != null) {
                setTumorTypeForQueries(sample.getMutations().getGenomicChange(), tumorType);
                genomicChange = annotateMutationsByGenomicChange(sample.getMutations().getGenomicChange());
            }
            if (sample.getMutations().getProteinChange() != null) {
                setTumorTypeForQueries(sample.getMutations().getProteinChange(), tumorType);
                proteinChange = annotateMutationsByProteinChange(sample.getMutations().getProteinChange());
            }
            if (sample.getMutations().getHgvsg() != null) {
                setTumorTypeForQueries(sample.getMutations().getHgvsg(), tumorType);
                hgvsg = annotateMutationsByHGVSg(sample.getMutations().getHgvsg());
            }
            if (sample.getMutations().getcDnaChange() != null) {
                setTumorTypeForQueries(sample.getMutations().getcDnaChange(), tumorType);
                cDnaChange = annotateMutationsByHGVSg(sample.getMutations().getcDnaChange());
            }
        }

        annotatedSample.setStructuralVariants(structuralVariants);
        annotatedSample.setCopyNumberAlterations(copyNumberAlterations);
        annotatedSample.setMutations(Stream.of(genomicChange, cDnaChange, proteinChange, hgvsg)
            .flatMap(Collection::stream)
            .collect(Collectors.toList())
        );
        return annotatedSample;
    }

    private <T extends AnnotationQuery> void setTumorTypeForQueries(List<T> queries, String tumorType) {
        if (Strings.isEmpty(tumorType)) {
            return;
        }

        for (AnnotationQuery query : queries) {
            query.setTumorType(tumorType);
        }
    }

    public List<SomaticIndicatorQueryResp> annotateStructuralVariants(List<AnnotateStructuralVariantQuery> structuralVariants) {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        for (AnnotateStructuralVariantQuery query : structuralVariants) {
            query.setGermline(SOMATIC);
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

            SomaticIndicatorQueryResp resp = this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                null,
                fusionName,
                null,
                AlterationType.STRUCTURAL_VARIANT.name(),
                query.getTumorType(),
                query.getFunctionalFusion() ? "fusion" : "",
                null,
                null,
                query.getStructuralVariantType(),
                null,
                null,
                null,
                null,
                false, 
                query.getEvidenceTypes(), 
                false
            );
            resp.getQuery().setId(query.getId());
            result.add(resp);
        }
        return result;
    }

    public List<SomaticIndicatorQueryResp> annotateCopyNumberAlterations(List<AnnotateCopyNumberAlterationQuery> copyNumberAlterations) {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        for (AnnotateCopyNumberAlterationQuery query : copyNumberAlterations) {
            query.setGermline(SOMATIC);
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
            SomaticIndicatorQueryResp resp = this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                gene.getEntrezGeneId(),
                gene.getHugoSymbol(),
                StringUtils.capitalize(query.getCopyNameAlterationType().name().toLowerCase()),
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
            resp.getQuery().setId(query.getId());
            result.add(resp);
        }
        return result;
    }

    public List<SomaticIndicatorQueryResp> annotateMutationsByGenomicChange(List<AnnotateMutationByGenomicChangeQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByGenomicChangeQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByGenomicChangeQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByGenomicChangeQuery query = mutations.get(i);
            query.setGermline(SOMATIC);
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

        List<SomaticIndicatorQueryResp> grch37Alts = annotateMutationsByGenomicChange(ReferenceGenome.GRCh37, grch37Queries);
        List<SomaticIndicatorQueryResp> grch38Alts = annotateMutationsByGenomicChange(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByGenomicChangeQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByGenomicChange(ReferenceGenome referenceGenome, List<AnnotateMutationByGenomicChangeQuery> queries) throws ApiException, org.genome_nexus.ApiException {
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
        if(variantAnnotations.size() != queriesToGN.size()){
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }
        
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<Alteration> allAlterations =  AlterationUtils.getAllAlterations();
        for (AnnotateMutationByGenomicChangeQuery query : queries) {
            SomaticIndicatorQueryResp indicatorQueryResp = null;
            if (queryIndexMap.containsKey(query.getGenomicLocation())) {
                VariantAnnotation variantAnnotation = variantAnnotations.get(queryIndexMap.get(query.getGenomicLocation()));
                List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                    Collections.singletonList(variantAnnotation), 
                    referenceGenome,
                    SOMATIC
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

    public List<SomaticIndicatorQueryResp> annotateMutationsByProteinChange(List<AnnotateMutationByProteinChangeQuery> mutations) {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        for (AnnotateMutationByProteinChangeQuery query : mutations) {
            query.setGermline(SOMATIC);
            SomaticIndicatorQueryResp resp = this.cacheFetcher.processQuerySomatic(
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
                null,
                null,
                false,
                query.getEvidenceTypes(),
                false
            );
            resp.getQuery().setId(query.getId());
            result.add(resp);
        }
        return result;
    }

    public List<SomaticIndicatorQueryResp> annotateMutationsByHGVSg(List<AnnotateMutationByHGVSgQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByHGVSgQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByHGVSgQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVSgQuery query = mutations.get(i);
            query.setHgvsg(StringUtils.trim(query.getHgvsg()));
            query.setGermline(SOMATIC);
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

        List<SomaticIndicatorQueryResp> grch37Alts = annotateMutationsByHGVSg(ReferenceGenome.GRCh37, grch37Queries);
        List<SomaticIndicatorQueryResp> grch38Alts = annotateMutationsByHGVSg(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVSgQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByHGVSg(ReferenceGenome referenceGenome, List<AnnotateMutationByHGVSgQuery> queries) throws ApiException, org.genome_nexus.ApiException {
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
        if(variantAnnotations.size() != queriesToGN.size()){
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }
        
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<Alteration> allAlterations = AlterationUtils.getAllAlterations();
        for (AnnotateMutationByHGVSgQuery query : queries) {
            SomaticIndicatorQueryResp indicatorQueryResp = null;
            if (queryIndexMap.containsKey(query.getHgvsg())) {
                VariantAnnotation variantAnnotation = variantAnnotations.get(queryIndexMap.get(query.getHgvsg()));
                List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                    Collections.singletonList(variantAnnotation), 
                    referenceGenome,
                    SOMATIC
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

    private SomaticIndicatorQueryResp getIndicatorQueryForCuratedHgvs(
        AnnotationQuery query,
        String hgvsg,
        TranscriptSummaryAlterationResult selectedAnnotatedAlteration,
        ReferenceGenome referenceGenome, 
        List<Alteration> allAlterations
    ) throws org.genome_nexus.ApiException {
        Alteration alteration = AlterationUtils.findAlterationWithGeneticType(
            referenceGenome,
            selectedAnnotatedAlteration.getHgvspAlteration().getGene(),
            hgvsg,
            allAlterations,
            SOMATIC
        );
        if (alteration != null) {
            return this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                null,
                alteration.getGene().getHugoSymbol(),
                hgvsg,
                null,
                query.getTumorType(),
                null,
                null,
                null,
                null,
                hgvsg,
                null,
                null,
                null,
                false,
                query.getEvidenceTypes(),
                false
            );
        }

        alteration = null;
        String hgvsc = selectedAnnotatedAlteration.getHgvscAlteration().getAlteration();
        if (StringUtils.isNotEmpty(hgvsc)) {
            alteration = AlterationUtils.findAlterationWithGeneticType(
                referenceGenome,
                selectedAnnotatedAlteration.getHgvscAlteration().getGene(),
                hgvsc,
                allAlterations,
                SOMATIC
            );
        }
        if (alteration != null) {
            return this.cacheFetcher.processQuerySomatic(
                query.getReferenceGenome(),
                null,
                alteration.getGene().getHugoSymbol(),
                hgvsc,
                null,
                query.getTumorType(),
                null,
                null,
                null,
                null,
                hgvsg,
                null,
                null,
                null,
                false,
                query.getEvidenceTypes(),
                false
            );
        }

        return null;
    }

    public List<SomaticIndicatorQueryResp> annotateMutationsByHGVSc(List<AnnotateMutationByHGVScQuery> mutations) throws ApiException, org.genome_nexus.ApiException {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();
        List<AnnotateMutationByHGVScQuery> grch37Queries = new ArrayList<>();
        List<AnnotateMutationByHGVScQuery> grch38Queries = new ArrayList<>();
        Map<Integer, Integer> grch37Map = new HashMap<>();
        Map<Integer, Integer> grch38Map = new HashMap<>();

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVScQuery query = mutations.get(i);
            query.setHgvsc(StringUtils.trim(query.getHgvsc()));
            query.setGermline(SOMATIC);
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

        List<SomaticIndicatorQueryResp> grch37Alts = annotateMutationsByHGVSc(ReferenceGenome.GRCh37, grch37Queries);
        List<SomaticIndicatorQueryResp> grch38Alts = annotateMutationsByHGVSc(ReferenceGenome.GRCh38, grch38Queries);

        for (int i = 0; i < mutations.size(); i++) {
            AnnotateMutationByHGVScQuery query = mutations.get(i);
            result.add(query.getReferenceGenome() == ReferenceGenome.GRCh37 ? grch37Alts.get(grch37Map.get(i)) : grch38Alts.get(grch38Map.get(i)));
        }
        return result;
    }

    private List<SomaticIndicatorQueryResp> annotateMutationsByHGVSc(ReferenceGenome referenceGenome, List<AnnotateMutationByHGVScQuery> queries) throws ApiException, org.genome_nexus.ApiException {
        List<SomaticIndicatorQueryResp> result = new ArrayList<>();

        List<String> queriesToGN = new ArrayList<>();
        Map<String, Integer> queryToGNIndexMap = new HashMap<>();
        AnnotateMutationByHGVScQuery[] resultIndexToQuery = new AnnotateMutationByHGVScQuery[queries.size()];

        List<Alteration> allAlterations = AlterationUtils.getAllAlterations();

        for (int i = 0; i < queries.size(); i++) {
            AnnotateMutationByHGVScQuery query = queries.get(i);
            String hgvsc = query.getHgvsc();

            if (this.cacheFetcher.hgvscShouldBeAnnotated(hgvsc)) {
                Alteration alteration = AlterationUtils.findAlterationWithGeneticType(
                    referenceGenome,
                    GeneUtils.getGeneByHugoSymbol(query.getGene()),
                    query.getAlteration(),
                    allAlterations,
                    SOMATIC
                );

                if (alteration != null) {
                    // If we found hgvsc in database, then we should skip using GN to resolve
                    SomaticIndicatorQueryResp resp = this.cacheFetcher.processQuerySomatic(
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
                    resp.getQuery().setId(query.getId());
                    result.add(resp);
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
                SomaticIndicatorQueryResp resp = this.getIndicatorQueryFromHGVS(
                    query.getReferenceGenome(),
                    new TranscriptSummaryAlterationResult(),
                    query.getHgvsc(),
                    query.getTumorType(),
                    query.getEvidenceTypes()
                );
                resp.getQuery().setId(query.getId());
                result.add(resp);
            }
        }

        List<org.genome_nexus.client.VariantAnnotation> variantAnnotations = GenomeNexusUtils.getHgvsVariantsAnnotation(queriesToGN, referenceGenome);
        if(variantAnnotations.size() != queriesToGN.size()){
            throw new ApiException("Number of variants that have been annotated by GenomeNexus is not equal to the number of queries");
        }

        for (int i = 0; i < result.size(); i++) {
            SomaticIndicatorQueryResp indicatorQueryResp = null;
            if (result.get(i) == null) {
                AnnotateMutationByHGVScQuery query = resultIndexToQuery[i];
                if (queryToGNIndexMap.containsKey(query.getHgvsc())) {
                    VariantAnnotation variantAnnotation = variantAnnotations.get(queryToGNIndexMap.get(query.getHgvsc()));
                    List<TranscriptSummaryAlterationResult> annotatedAlteration = AlterationUtils.getAlterationsFromGenomeNexus(
                        Collections.singletonList(variantAnnotation), 
                        referenceGenome,
                        SOMATIC
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

    private void addTranscriptAndExonToResponse(SomaticIndicatorQueryResp response, TranscriptConsequenceSummary summary) {
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
