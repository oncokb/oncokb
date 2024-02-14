package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.CancerTypeMatch;
import org.mskcc.cbio.oncokb.apiModels.DrugMatch;
import org.mskcc.cbio.oncokb.apiModels.LevelsOfEvidenceMatch;
import org.mskcc.cbio.oncokb.bo.OncokbTranscriptService;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;
import static org.mskcc.cbio.oncokb.util.AlterationUtils.GENOMIC_CHANGE_FORMAT;
import static org.mskcc.cbio.oncokb.util.AlterationUtils.HGVSG_FORMAT;
import static org.mskcc.cbio.oncokb.util.LevelUtils.*;

/**
 * Created by Hongxin on 10/28/16.
 */
@Controller
public class PrivateSearchApiController implements PrivateSearchApi {
    private Integer DEFAULT_RETURN_LIMIT = 5;

    @Autowired
    CacheFetcher cacheFetcher;

    @Override
    public ResponseEntity<Set<BiologicalVariant>> searchVariantsBiologicalGet(
        @ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    ) {
        Set<BiologicalVariant> variants = new HashSet<>();
        HttpStatus status = HttpStatus.OK;

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            variants = MainUtils.getBiologicalVariants(gene);
        }
        return new ResponseEntity<>(variants, status);
    }

    @Override
    public ResponseEntity<Set<ClinicalVariant>> searchVariantsClinicalGet(
        @ApiParam(value = "") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
    ) {
        HttpStatus status = HttpStatus.OK;
        Set<ClinicalVariant> variants = new HashSet<>();

        if (hugoSymbol != null) {
            Gene gene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
            variants = MainUtils.getClinicalVariants(gene);
        }
        return new ResponseEntity<>(variants, status);
    }

    @Override
    public ResponseEntity<Set<Treatment>> searchTreatmentsGet(
        @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.", required = true) @RequestParam(value = "gene", required = false) String queryGene
        , @ApiParam(value = "The level of evidence.", defaultValue = "false") @RequestParam(value = "level", required = false) String queryLevel) {
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

    @Override
    public ResponseEntity<LinkedHashSet<TypeaheadSearchResp>> searchTypeAheadGet(
        @ApiParam(value = "The search query, it could be hugoSymbol, entrezGeneId, variant, or cancer type. At least two characters. Maximum two keywords are supported, separated by space", required = true) @RequestParam(value = "query") String query,
        @ApiParam(value = "The limit of returned result.") @RequestParam(value = "limit", defaultValue = "5", required = false) Integer limit) {
        final int QUERY_MIN_LENGTH = 2;
        LinkedHashSet<TypeaheadSearchResp> result = new LinkedHashSet<>();
        if (limit == null) {
            limit = DEFAULT_RETURN_LIMIT;
        }
        if (query != null && query.length() >= QUERY_MIN_LENGTH) {
            // genomic queries will not have space in the query
            String trimmedQuery = query.trim().replaceAll(" ", "");
            if (AlterationUtils.isValidHgvsg(trimmedQuery) || AlterationUtils.isValidGenomicChange(trimmedQuery)) {
                GNVariantAnnotationType type = null;

                if (AlterationUtils.isValidHgvsg(trimmedQuery)) {
                    type = GNVariantAnnotationType.HGVS_G;
                } else if (AlterationUtils.isValidGenomicChange(trimmedQuery)) {
                    type = GNVariantAnnotationType.GENOMIC_LOCATION;
                }
                if (type != null) {
                    Alteration alterationModel;
                    String refGenomeStr = "";
                    ReferenceGenome referenceGenome = ReferenceGenome.GRCh37;
                    Matcher rgm = (GNVariantAnnotationType.HGVS_G.equals(type) ? HGVSG_FORMAT : GENOMIC_CHANGE_FORMAT).matcher(trimmedQuery);
                    if (rgm.find()) {
                        refGenomeStr = rgm.group(2);
                        trimmedQuery = rgm.group(3);
                    }
                    if (StringUtils.isNotEmpty(refGenomeStr)) {
                        ReferenceGenome matchedReferenceGenome = MainUtils.searchEnum(ReferenceGenome.class, refGenomeStr);
                        if (matchedReferenceGenome != null) {
                            referenceGenome = matchedReferenceGenome;
                        }
                    }
                    try {
                        alterationModel = this.cacheFetcher.getAlterationFromGenomeNexus(type, referenceGenome, trimmedQuery);
                        if (alterationModel.getGene() != null) {
                            Query annotationQuery = QueryUtils.getQueryFromAlteration(referenceGenome, "", alterationModel, HGVSG_FORMAT.equals(type) ? trimmedQuery : "");
                            IndicatorQueryResp indicatorQueryResp = IndicatorUtils.processQuery(annotationQuery, null, false, null);
                            result.add(newTypeaheadAnnotation(trimmedQuery, type, referenceGenome, alterationModel, indicatorQueryResp));
                        }
                    } catch (org.genome_nexus.ApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                List<String> keywords = Arrays.asList(query.trim().split("\\s+"));

                if (keywords.size() == 1) {
                    // Blur search gene
                    result.addAll(convertGene(GeneUtils.searchGene(keywords.get(0), false), keywords.get(0)));

                    // Blur search variant
                    result.addAll(convertVariant(AlterationUtils.lookupVariant(keywords.get(0), false, false, AlterationUtils.getAllAlterations()), keywords.get(0)));

                    // Blur search drug
                    result.addAll(findEvidencesWithDrugAssociated(keywords.get(0), false));

                    // Blur search cancer type
                    result.addAll(findMatchingCancerTypes(keywords.get(0), false));

                    // If the keyword contains dash and result is empty, then we should return both fusion genes
                    if (keywords.get(0).contains("-") && result.isEmpty()) {
                        for (String subKeyword : keywords.get(0).split("-")) {
                            result.addAll(convertGene(GeneUtils.searchGene(subKeyword, false), subKeyword));
                        }
                    }
                } else {
                    // Assume one of the keyword is gene
                    Map<String, Set<Gene>> map = new HashedMap();
                    for (String keyword : keywords) {
                        if (keyword.contains("-")) {
                            Set<Gene> subGenes = new HashSet<>();
                            for (String subKeyword : keyword.split("-")) {
                                subGenes.addAll(GeneUtils.searchGene(subKeyword, false));
                            }
                            map.put(keyword, subGenes);
                        } else {
                            map.put(keyword, GeneUtils.searchGene(keyword, false));
                        }
                    }

                    result.addAll(getMatch(map, keywords, false));

                    // If there is no match, the key words could referring to a variant, try to do a blur variant search
                    String fullKeywords = StringUtils.join(keywords, " ");
                    result.addAll(convertVariant(AlterationUtils.lookupVariant(fullKeywords, false, false, AlterationUtils.getAllAlterations()), fullKeywords));

                    // Blur search for cancer type
                    result.addAll(findMatchingCancerTypes(fullKeywords, false));

                    // If there is no match in OncoKB database, still try to annotate variant
                    // Only when the oncogenicity is not empty
                    if (result.size() == 0) {
                        for (Map.Entry<String, Set<Gene>> entry : map.entrySet()) {
                            if (entry.getValue().size() > 0) {
                                for (Gene gene : entry.getValue()) {
                                    for (String keyword : keywords) {
                                        if (!keyword.equals(entry.getKey())) {
                                            Alteration alteration =
                                                AlterationUtils.getAlteration(gene.getHugoSymbol(), keyword, null, null, null, null, null);
                                            TypeaheadSearchResp typeaheadSearchResp = newTypeaheadVariant(alteration);
                                            typeaheadSearchResp.setVariantExist(false);
                                            result.add(typeaheadSearchResp);
                                            if (typeaheadSearchResp.getOncogenicity() == null
                                                || typeaheadSearchResp.getOncogenicity().isEmpty()) {
                                                String annotation = "Please make sure your query is valid.";
                                                if (typeaheadSearchResp.getAnnotation() != null) {
                                                    annotation = typeaheadSearchResp.getAnnotation() + " " + annotation;
                                                }
                                                typeaheadSearchResp.setAnnotation(annotation);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
            typeaheadSearchResp.setAnnotation("Please search at least " + QUERY_MIN_LENGTH + " characters.");
            typeaheadSearchResp.setQueryType(TypeaheadQueryType.TEXT);
            result.add(typeaheadSearchResp);
        }
        return new ResponseEntity<>(MainUtils.getLimit(result, limit), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LinkedHashSet<org.oncokb.oncokb_transcript.client.Drug>> searchDrugGet(
        @ApiParam(value = "The search query, it could be drug name, NCIT code", required = true) @RequestParam(value = "query") String query,
        @ApiParam(value = "The limit of returned result.") @RequestParam(value = "limit", defaultValue = "5", required = false) Integer limit
    ) throws ApiException {
        if (limit == null) {
            limit = DEFAULT_RETURN_LIMIT;
        }
        OncokbTranscriptService oncokbTranscriptService = new OncokbTranscriptService();
        return new ResponseEntity<>(MainUtils.getLimit(new LinkedHashSet<>(oncokbTranscriptService.findDrugs(query)), limit), HttpStatus.OK);
    }

    private List<TypeaheadSearchResp> findMatchingCancerTypes(String query, Boolean exactMatch) {
        // got all evidences and level
        Set<Evidence> evidences = EvidenceUtils.getEvidenceByEvidenceTypesAndLevels(EvidenceTypeUtils.getTreatmentEvidenceTypes(), LevelUtils.getPublicLevels());
        Map<String, CancerTypeMatch> result = new HashMap<>();

        query = query.toLowerCase();

        ArrayList<TumorType> matchedTumorTypes = new ArrayList<>();

        if (exactMatch) {
            matchedTumorTypes.add(ApplicationContextSingleton.getTumorTypeBo().getByName(query));
        } else {
            for (Map.Entry<String, TumorType> currSubtype : CacheUtils.getLowercaseSubtypeTumorTypeMap().entrySet()) {
                if (currSubtype.getKey().startsWith(query) || currSubtype.getKey().contains(query)) {
                    matchedTumorTypes.add(currSubtype.getValue());
                }
            }

            for (Map.Entry<String, TumorType> currMainType : CacheUtils.getMainTypeTumorTypeMap().entrySet()) {
                if (currMainType.getKey().toLowerCase().startsWith(query) || currMainType.getKey().toLowerCase().contains(query)) {
                    matchedTumorTypes.add(currMainType.getValue());
                }
            }
        }

        // cancer type not found, return an empty list
        if (matchedTumorTypes.isEmpty()) {
            return Collections.emptyList();
        }

        // definitely a matching cancer type, now searching evidence for relevant ones
        for (TumorType currMatchedCancer : matchedTumorTypes) {
            String matchKey = TumorTypeUtils.getTumorTypeName(currMatchedCancer).toLowerCase();

            for (Evidence evidence : evidences) {
                // exact match found
                if (TumorTypeUtils.findEvidenceRelevantCancerTypes(evidence).contains(currMatchedCancer)) {
                    updateCancerMap(
                        result, matchKey, evidence.getAlterations(),
                        currMatchedCancer, evidence.getLevelOfEvidence(),
                        matchKey.startsWith(query) ? 4.0 : 3.5
                    );
                }
            }
            updateCancerMap(
                result, matchKey, null, currMatchedCancer, null,
                matchKey.startsWith(query) ? 2.0 : 1.0
            );
        }

        TreeSet<CancerTypeMatch> cancerMatches = new TreeSet<>(new CancerTypeMatchComp());
        for (Map.Entry<String, CancerTypeMatch> entry : result.entrySet()) {
            cancerMatches.add(entry.getValue());
        }

        // conversion to desired list output
        return cancerMatches.stream().map(cancerMatch -> newTypeaheadCancer(cancerMatch)).collect(Collectors.toList());
    }

    // we do not show parenthesis if it's in alteration name.
    private static String getGeneStrInAnnotation(Gene gene, String altAnnotation) {
        altAnnotation = altAnnotation.replaceAll("\\(", "");
        altAnnotation = altAnnotation.replaceAll("\\)", "");
        return gene.getHugoSymbol() + " (" + altAnnotation + ")";
    }

    private static String getCancerTypeSearchAnnotation(Set<Alteration> alterations, String cancerType) {
        // group alterations by gene
        Map<Gene, Set<Alteration>> geneGroupedAlts = new HashMap<>();
        for (Alteration alteration : alterations) {
            if (!geneGroupedAlts.containsKey(alteration.getGene())) {
                geneGroupedAlts.put(alteration.getGene(), new HashSet<>());
            }
            geneGroupedAlts.get(alteration.getGene()).add(alteration);
        }

        // calculate occurrence based on number of portal alterations
        List<GeneCount> geneCounts = new ArrayList<>();
        Map<Gene, String> alterationAnnotation = new HashMap<>();
        for (Map.Entry<Gene, Set<Alteration>> entry : geneGroupedAlts.entrySet()) {
            Gene gene = entry.getKey();
            int totalAltCount = entry.getValue()
                .stream()
                .map(alteration -> (int) alteration.getPortalAlterations().stream().filter(pa -> pa.getCancerType().equalsIgnoreCase(cancerType)).count())
                .reduce(0, Integer::sum);
            GeneCount geneCount = new GeneCount();
            geneCount.setGene(gene);
            geneCount.setCount(totalAltCount);
            geneCounts.add(geneCount);

            // calculate occurrence based on number of portal alterations
            List<AlterationCount> alterationCounts = entry.getValue().stream().map(alteration -> {
                AlterationCount ac = new AlterationCount();
                ac.setAlteration(alteration);
                ac.setCount((int) alteration.getPortalAlterations().stream().filter(pa -> pa.getCancerType().equalsIgnoreCase(cancerType)).count());
                return ac;
            }).collect(Collectors.toList());
            alterationCounts = alterationCounts.stream().sorted(Comparator.comparing(AlterationCount::getCount).reversed()).collect(Collectors.toList());

            alterationAnnotation.put(gene, alterationCounts.size() > 3 ? StringUtils.join(alterationCounts.subList(0, 1).stream().map(ac -> ac.getAlteration().getName()).collect(Collectors.toList()), ", ") + " and " + (alterationCounts.size() - 1) + " other alterations" : StringUtils.join(alterationCounts.stream().map(ac -> ac.getAlteration().getName()).collect(Collectors.toList()), ", "));
        }

        geneCounts = geneCounts.stream().sorted(Comparator.comparing(GeneCount::getCount).reversed()).collect(Collectors.toList());
        return geneCounts.size() > 3 ? StringUtils.join(geneCounts.subList(0, 3).stream().map(geneCount -> getGeneStrInAnnotation(geneCount.getGene(), alterationAnnotation.get(geneCount.getGene()))).collect(Collectors.toList()), ", ") + " and more" : StringUtils.join(geneCounts.stream().map(geneCount -> getGeneStrInAnnotation(geneCount.getGene(), alterationAnnotation.get(geneCount.getGene()))).collect(Collectors.toList()), ", ");
    }

    private static TypeaheadSearchResp newTypeaheadCancer(CancerTypeMatch cancerMatch) {
        TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
        typeaheadSearchResp.setAnnotationByLevel(
            cancerMatch.getAlterationsByLevel()
                .entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, entry -> getCancerTypeSearchAnnotation(entry.getValue(), cancerMatch.getCancerType().getMainType()))
                ));

        Set<TumorType> currCancerType = new HashSet<>(Collections.singleton(cancerMatch.getCancerType()));
        typeaheadSearchResp.setTumorTypes(currCancerType);

        List<LevelOfEvidence> txLevels = new ArrayList<>();
        txLevels.addAll(THERAPEUTIC_RESISTANCE_LEVELS);
        txLevels.addAll(THERAPEUTIC_SENSITIVE_LEVELS);

        if (cancerMatch.getAlterationsByLevel() != null && cancerMatch.findHighestLevel(txLevels) != null) {
            LevelOfEvidence highestLevel = cancerMatch.findHighestLevel(txLevels);

            if (LevelUtils.isSensitiveLevel(highestLevel)) {
                typeaheadSearchResp.setHighestSensitiveLevel(highestLevel.getLevel());
            } else {
                typeaheadSearchResp.setHighestResistanceLevel(highestLevel.getLevel());
            }

            String cancerType = cancerMatch.getCancerType().getCode();
            if (StringUtils.isEmpty(cancerType)) {
                cancerType = cancerMatch.getCancerType().getSubtype();
            }
            if (StringUtils.isEmpty(cancerType)) {
                cancerType = cancerMatch.getCancerType().getMainType();
            }
            typeaheadSearchResp.setLink("actionable-genes#sections=Tx&cancerType=" + cancerType);
        } else {
            typeaheadSearchResp.setLink("");
        }

        typeaheadSearchResp.setQueryType(TypeaheadQueryType.CANCER_TYPE);

        return typeaheadSearchResp;
    }

    private static void updateCancerMap(Map<String, CancerTypeMatch> map, String key, Set<Alteration> alterations, TumorType cancer, LevelOfEvidence level, Double weight) {
        // gene, alterations, level of evidence can be null
        if (!map.containsKey(key)) {
            CancerTypeMatch cancerMatch = new CancerTypeMatch();
            cancerMatch.setCancerType(cancer);
            cancerMatch.setWeight(weight);

            List<LevelOfEvidence> txLevels = new ArrayList<>();
            txLevels.addAll(THERAPEUTIC_RESISTANCE_LEVELS);
            txLevels.addAll(THERAPEUTIC_SENSITIVE_LEVELS);
            cancerMatch.setAlterationsByLevel(new TreeMap<>(new LevelOfEvidenceComp(txLevels)));

            map.put(key, cancerMatch);
        }
        if (alterations != null) {
            Set<Alteration> alterationsForLevel = map.get(key).getAlterationsByLevel().get(level);
            if (alterationsForLevel == null) {
                alterationsForLevel = new HashSet<>();
                map.get(key).getAlterationsByLevel().put(level, alterationsForLevel);
            }

            alterationsForLevel.addAll(alterations);
        }
    }

    private LinkedHashSet<TypeaheadSearchResp> getMatch(Map<String, Set<Gene>> map, List<String> keywords, Boolean exactMatch) {
        LinkedHashSet<TypeaheadSearchResp> result = new LinkedHashSet<>();
        if (map == null || keywords == null) {
            return result;
        }
        if (exactMatch == null)
            exactMatch = false;
        for (Map.Entry<String, Set<Gene>> entry : map.entrySet()) {
            if (entry.getValue().size() > 0) {
                for (Gene gene : entry.getValue()) {

                    List<Alteration> alterations = AlterationUtils.getAllAlterations(null, gene);
                    // When more than two keywords present, the index does not matter anymore.
                    // As long as there is match, return it.
                    if (keywords.size() > 2) {
                        Set<Alteration> keywordsMatches = null;
                        for (String keyword : keywords) {
                            if (!keyword.equals(entry.getKey())) {
                                List<Alteration> matches = AlterationUtils.lookupVariant(keyword, exactMatch, false, alterations);
                                if (matches != null) {
                                    if (keywordsMatches == null) {
                                        keywordsMatches = new HashSet<>();
                                        keywordsMatches.addAll(matches);
                                    } else {
                                        List<Alteration> intersection = (List<Alteration>) CollectionUtils.intersection(keywordsMatches, matches);
                                        keywordsMatches = new HashSet<>();
                                        keywordsMatches.addAll(intersection);
                                    }
                                }
                            }
                        }
                        if (keywordsMatches != null && keywordsMatches.size() > 0) {
                            result.addAll(convertVariant(new ArrayList<>(keywordsMatches), ""));
                        }
                    } else {
                        for (String keyword : keywords) {
                            if (!keyword.equals(entry.getKey()))
                                result.addAll(convertVariant(AlterationUtils.lookupVariant(keyword, exactMatch, false, alterations), keyword));
                        }
                    }
                }
            }
        }
        return result;
    }

    private TreeSet<TypeaheadSearchResp> convertGene(Set<Gene> genes, String keyword) {
        TreeSet<TypeaheadSearchResp> result = new TreeSet<>(new GeneComp(keyword));
        if (genes != null) {
            Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getEvidenceByGenes(genes);
            for (Gene gene : genes) {
                TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
                typeaheadSearchResp.setGene(gene);
                typeaheadSearchResp.setVariantExist(false);
                typeaheadSearchResp.setLink("/gene/" + gene.getHugoSymbol());
                typeaheadSearchResp.setQueryType(TypeaheadQueryType.GENE);

                if (evidences.containsKey(gene)) {
                    LevelOfEvidence highestSensitiveLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(evidences.get(gene), LevelUtils.getSensitiveLevels());
                    LevelOfEvidence highestResistanceLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(evidences.get(gene), LevelUtils.getResistanceLevels());
                    typeaheadSearchResp.setHighestSensitiveLevel(highestSensitiveLevel == null ? "" : highestSensitiveLevel.getLevel());
                    typeaheadSearchResp.setHighestResistanceLevel(highestResistanceLevel == null ? "" : highestResistanceLevel.getLevel());
                }
                result.add(typeaheadSearchResp);
            }
        }
        return result;
    }

    private TreeSet<TypeaheadSearchResp> convertVariant(List<Alteration> alterations, String keyword) {
        TreeSet<TypeaheadSearchResp> result = new TreeSet<>(new VariantComp(keyword));
        if (alterations != null) {
            for (Alteration alteration : alterations) {
                result.add(newTypeaheadVariant(alteration));
            }
        }
        return result;
    }

    private static TypeaheadSearchResp newTypeaheadDrug(DrugMatch drugMatch) {
        TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
        typeaheadSearchResp.setGene(drugMatch.getGene());
        typeaheadSearchResp.setVariants(drugMatch.getAlterations());
        typeaheadSearchResp.setDrug(drugMatch.getDrug());
        typeaheadSearchResp.setTumorTypes(drugMatch.getTumorTypes());

        if (LevelUtils.isSensitiveLevel(drugMatch.getLevelOfEvidence())) {
            typeaheadSearchResp.setHighestSensitiveLevel(drugMatch.getLevelOfEvidence().getLevel());
        } else {
            typeaheadSearchResp.setHighestResistanceLevel(drugMatch.getLevelOfEvidence().getLevel());
        }
        typeaheadSearchResp.setQueryType(TypeaheadQueryType.DRUG);

        if (drugMatch.getAlterations().size() > 1 || drugMatch.getAlterations().size() == 0) {
            typeaheadSearchResp.setLink("/gene/" + drugMatch.getGene().getHugoSymbol());
        } else {
            typeaheadSearchResp.setLink("/gene/" + drugMatch.getGene().getHugoSymbol() + "/" + drugMatch.getAlterations().iterator().next().getAlteration());
        }
        return typeaheadSearchResp;
    }

    private static String getDrugMatchKey(Gene gene, Drug drug, LevelOfEvidence level) {
        return gene.getHugoSymbol() + drug.getDrugName() + level.getLevel();
    }

    private static void updateMap(Map<String, DrugMatch> map, String key, Gene gene, Set<Alteration> alterations, Drug drug, LevelOfEvidence level, Collection<TumorType> tumorTypes, Double weight) {
        if (!map.containsKey(key)) {
            DrugMatch drugMatch = new DrugMatch();
            drugMatch.setGene(gene);
            drugMatch.setLevelOfEvidence(level);
            drugMatch.setDrug(drug);
            drugMatch.setWeight(weight);
            map.put(key, drugMatch);
        }
        map.get(key).getAlterations().addAll(alterations);
        map.get(key).getTumorTypes().addAll(tumorTypes);
    }

    private static List<TypeaheadSearchResp> findEvidencesWithDrugAssociated(String query, Boolean exactMatch) {
        Set<Evidence> evidences = EvidenceUtils.getEvidenceByEvidenceTypesAndLevels(EvidenceTypeUtils.getTreatmentEvidenceTypes(), LevelUtils.getPublicLevels());
        Map<String, DrugMatch> result = new HashMap<>();

        if (exactMatch == null) {
            exactMatch = false;
        }

        query = query.toLowerCase();

        for (Evidence evidence : evidences) {
            boolean isMatch = false;
            for (Treatment treatment : evidence.getTreatments()) {
                if (isMatch) {
                    break;
                }
                for (Drug drug : treatment.getDrugs()) {
                    String matchKey = getDrugMatchKey(evidence.getGene(), drug, evidence.getLevelOfEvidence());
                    if (isMatch) {
                        break;
                    }
                    if (drug.getDrugName().toLowerCase().equals(query)) {
                        updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getCancerTypes(), 4.0);
                        isMatch = true;
                    } else if (drug.getNcitCode() != null && drug.getNcitCode().toLowerCase().equals(query)) {
                        updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getCancerTypes(), 4.0);
                        isMatch = true;
                    } else {
                        for (String synonym : drug.getSynonyms()) {
                            if (synonym.toLowerCase().equals(query)) {
                                updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getCancerTypes(), 3.0);
                                isMatch = true;
                                break;
                            }
                        }
                    }
                    if (!exactMatch) {
                        String lowerCaseDrugName = drug.getDrugName().toLowerCase();
                        if (lowerCaseDrugName.startsWith(query)) {
                            updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getCancerTypes(), 2.0);
                            isMatch = true;
                        } else if (lowerCaseDrugName.contains(query)) {
                            updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getCancerTypes(), 1.5);
                            isMatch = true;
                        } else {
                            for (String synonym : drug.getSynonyms()) {
                                String lower = synonym.toLowerCase();

                                if (lower.startsWith(query)) {
                                    updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getCancerTypes(), 1.0);
                                    isMatch = true;
                                    break;
                                } else if (lower.contains(query)) {
                                    updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getCancerTypes(), 0.5);
                                    isMatch = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        TreeSet<DrugMatch> drugMatches = new TreeSet<>(new LevelsOfEvidenceMatchComp());
        for (Map.Entry<String, DrugMatch> entry : result.entrySet()) {
            drugMatches.add(entry.getValue());
        }

        return drugMatches.stream().map(drugMatch -> newTypeaheadDrug(drugMatch)).collect(Collectors.toList());
    }

    private TypeaheadSearchResp newTypeaheadVariant(Alteration alteration) {
        TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
        typeaheadSearchResp.setGene(alteration.getGene());
        typeaheadSearchResp.setVariants(Collections.singleton(alteration));
        typeaheadSearchResp.setVariantExist(true);

        ReferenceGenome referenceGenome = alteration.getReferenceGenomes().stream().findAny().orElse(DEFAULT_REFERENCE_GENOME);

        Query query = new Query();
        query.setEntrezGeneId(alteration.getGene().getEntrezGeneId());
        query.setAlteration(alteration.getAlteration());
        query.setReferenceGenome(alteration.getReferenceGenomes().iterator().next());

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, false, null);
        typeaheadSearchResp.setOncogenicity(resp.getOncogenic());
        typeaheadSearchResp.setVUS(resp.getVUS());
        typeaheadSearchResp.setAnnotation(resp.getVariantSummary() + " Click here to see more annotation details.");

        if (resp.getHighestSensitiveLevel() != null) {
            typeaheadSearchResp.setHighestSensitiveLevel(resp.getHighestSensitiveLevel().getLevel());
        }
        if (resp.getHighestResistanceLevel() != null) {
            typeaheadSearchResp.setHighestResistanceLevel(resp.getHighestResistanceLevel().getLevel());
        }

        if (alteration.getAlteration() != null && alteration.getAlteration().equalsIgnoreCase("oncogenic mutations")) {
            typeaheadSearchResp.setOncogenicity(Oncogenicity.YES.getOncogenic());
        }

        typeaheadSearchResp.setQueryType(TypeaheadQueryType.VARIANT);

        String link = "/gene/" + alteration.getGene().getHugoSymbol() + "/" + alteration.getAlteration();
        if (referenceGenome != DEFAULT_REFERENCE_GENOME) {
            link += "?refGenome=" + referenceGenome;
        }
        typeaheadSearchResp.setLink(link);
        return typeaheadSearchResp;
    }

    private TypeaheadSearchResp newTypeaheadAnnotation(String query, GNVariantAnnotationType type, ReferenceGenome referenceGenome, Alteration alteration, IndicatorQueryResp queryResp) {
        TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
        typeaheadSearchResp.setGene(alteration.getGene());
        typeaheadSearchResp.setVariants(Collections.singleton(alteration));
        typeaheadSearchResp.setVariantExist(true);

        typeaheadSearchResp.setOncogenicity(queryResp.getOncogenic());
        typeaheadSearchResp.setVUS(queryResp.getVUS());
        typeaheadSearchResp.setAnnotation(queryResp.getVariantSummary() + " Click here to see more annotation details.");

        if (queryResp.getHighestSensitiveLevel() != null) {
            typeaheadSearchResp.setHighestSensitiveLevel(queryResp.getHighestSensitiveLevel().getLevel());
        }
        if (queryResp.getHighestResistanceLevel() != null) {
            typeaheadSearchResp.setHighestResistanceLevel(queryResp.getHighestResistanceLevel().getLevel());
        }

        typeaheadSearchResp.setOncogenicity(queryResp.getOncogenic());

        typeaheadSearchResp.setQueryType(TypeaheadQueryType.GENOMIC);

        String link = "/" + (GNVariantAnnotationType.HGVS_G.equals(type) ? "hgvsg" : "genomic-change") + "/" + query;
        if (referenceGenome != DEFAULT_REFERENCE_GENOME) {
            link += "?refGenome=" + referenceGenome;
        }
        typeaheadSearchResp.setLink(link);
        return typeaheadSearchResp;
    }
}

class GeneComp implements Comparator<TypeaheadSearchResp> {
    private String keyword;

    public GeneComp(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    @Override
    public int compare(TypeaheadSearchResp e1, TypeaheadSearchResp e2) {
        if (e1 == null || e1.getGene() == null) {
            return 1;
        }
        if (e2 == null || e2.getGene() == null) {
            return -1;
        }
        return GeneUtils.compareGenesByKeyword(e1.getGene(), e2.getGene(), this.keyword);
    }
}

class VariantComp implements Comparator<TypeaheadSearchResp> {
    private String keyword;

    public VariantComp(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    @Override
    public int compare(TypeaheadSearchResp e1, TypeaheadSearchResp e2) {
        if (e1 == null || e1.getVariants() == null || e1.getVariants().size() == 0) {
            return 1;
        }
        if (e2 == null || e2.getVariants() == null || e2.getVariants().size() == 0) {
            return -1;
        }
        Alteration a1 = e1.getVariants().iterator().next();
        Alteration a2 = e2.getVariants().iterator().next();
        String name1 = a1.getAlteration().toLowerCase();
        String name2 = a2.getAlteration().toLowerCase();
        if (a1.getName() != null && a2.getName() != null) {
            name1 = a1.getName().toLowerCase();
            name2 = a2.getName().toLowerCase();
        }
        Integer index1 = name1.indexOf(this.keyword);
        Integer index2 = name2.indexOf(this.keyword);
        if (index1.equals(index2)) {
            //Compare Oncogenicity. Treat YES, LIKELY as the same
            Oncogenicity o1 = Oncogenicity.getByEffect(e1.getOncogenicity());
            Oncogenicity o2 = Oncogenicity.getByEffect(e2.getOncogenicity());
            if (o1 != null && o1.equals(Oncogenicity.LIKELY)) {
                o1 = Oncogenicity.YES;
            }
            if (o2 != null && o2.equals(Oncogenicity.LIKELY)) {
                o2 = Oncogenicity.YES;
            }
            Integer result = MainUtils.compareOncogenicity(o1, o2, true);
            if (result == 0) {
                // Compare highest sensitive level
                result = LevelUtils.compareLevel(LevelOfEvidence.getByLevel(e1.getHighestSensitiveLevel()), LevelOfEvidence.getByLevel(e2.getHighestSensitiveLevel()));
                if (result == 0) {
                    // Compare which is the highest resistance level
                    result = LevelUtils.compareLevel(LevelOfEvidence.getByLevel(e1.getHighestResistanceLevel()), LevelOfEvidence.getByLevel(e2.getHighestResistanceLevel()));
                    if (result == 0) {
                        result = name1.compareTo(name2);
                        if (result == 0) {
                            // Compare gene name
                            result = e1.getGene().getHugoSymbol().compareTo(e2.getGene().getHugoSymbol());
                        }
                    }
                }
            }
            return result;
        } else {
            if (index1.equals(-1))
                return 1;
            if (index2.equals(-1))
                return -1;
            return index1 - index2;
        }
    }
}

class LevelsOfEvidenceMatchComp implements Comparator<LevelsOfEvidenceMatch> {
    @Override
    public int compare(LevelsOfEvidenceMatch o1, LevelsOfEvidenceMatch o2) {
        int result = o2.getWeight().compareTo(o1.getWeight());
        if (result == 0) {
            result = LevelUtils.compareLevel(o1.getLevelOfEvidence(), o2.getLevelOfEvidence());
            if (result == 0) {
                result = o1.getGene().getHugoSymbol().compareTo(o2.getGene().getHugoSymbol());
            }
        }
        return result;
    }
}

class CancerTypeMatchComp implements Comparator<CancerTypeMatch> {
    @Override
    public int compare(CancerTypeMatch o1, CancerTypeMatch o2) {
        int result = o2.getWeight().compareTo(o1.getWeight());
        double weightForNoEvidenceFound = 2.0;

        if (result == 0 && o1.getWeight() > weightForNoEvidenceFound && o2.getWeight() > weightForNoEvidenceFound) {
            List<LevelOfEvidence> txLevels = new ArrayList<>();
            txLevels.addAll(THERAPEUTIC_RESISTANCE_LEVELS);
            txLevels.addAll(THERAPEUTIC_SENSITIVE_LEVELS);

            LevelOfEvidence o1HighestLevelOfEvidence = o1.findHighestLevel(txLevels);
            LevelOfEvidence o2HighestLevelOfEvidence = o2.findHighestLevel(txLevels);

            result = LevelUtils.compareLevel(o1HighestLevelOfEvidence, o2HighestLevelOfEvidence, txLevels);
        }

        if (result == 0) {
            result = TumorTypeUtils.getTumorTypeName(o1.getCancerType()).compareTo(TumorTypeUtils.getTumorTypeName(o2.getCancerType()));
        }

        return result;
    }
}

class LevelOfEvidenceComp implements Comparator<LevelOfEvidence> {
    private final List<LevelOfEvidence> customLevels;

    public LevelOfEvidenceComp(List<LevelOfEvidence> levels) {
        this.customLevels = levels;
    }

    @Override
    public int compare(LevelOfEvidence o1, LevelOfEvidence o2) {
        return LevelUtils.compareLevel(o1, o2, customLevels);
    }
}

class GeneCount {
    Gene gene;
    int count;

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

class AlterationCount {
    Alteration alteration;
    int count;

    public Alteration getAlteration() {
        return alteration;
    }

    public void setAlteration(Alteration alteration) {
        this.alteration = alteration;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
