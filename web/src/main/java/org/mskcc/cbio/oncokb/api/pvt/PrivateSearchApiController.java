package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.DrugMatch;
import org.mskcc.cbio.oncokb.apiModels.NCITDrug;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Hongxin on 10/28/16.
 */
@Controller
public class PrivateSearchApiController implements PrivateSearchApi {
    private Integer DEFAULT_RETURN_LIMIT = 5;

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
        @ApiParam(value = "The search query, it could be hugoSymbol, entrezGeneId or variant. At least two characters. Maximum two keywords are supported, separated by space", required = true) @RequestParam(value = "query") String query,
        @ApiParam(value = "The limit of returned result.") @RequestParam(value = "limit", defaultValue = "5", required = false) Integer limit) {
        final int QUERY_MIN_LENGTH = 2;
        LinkedHashSet<TypeaheadSearchResp> result = new LinkedHashSet<>();
        if (limit == null) {
            limit = DEFAULT_RETURN_LIMIT;
        }
        if (query != null && query.length() >= QUERY_MIN_LENGTH) {
            List<String> keywords = Arrays.asList(query.trim().split("\\s+"));

            if (keywords.size() == 1) {
                // Blur search gene
                result.addAll(convertGene(GeneUtils.searchGene(keywords.get(0), false), keywords.get(0)));

                // Blur search variant
                result.addAll(convertVariant(AlterationUtils.lookupVariant(keywords.get(0), false, AlterationUtils.getAllAlterations()), keywords.get(0)));

                // Blur search drug
                result.addAll(findEvidencesWithDrugAssociated(keywords.get(0), false));

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
                result.addAll(convertVariant(AlterationUtils.lookupVariant(fullKeywords, false, AlterationUtils.getAllAlterations()), fullKeywords));

                // If there is no match in OncoKB database, still try to annotate variant
                // Only when the oncogenicity is not empty
                if (result.size() == 0) {
                    for (Map.Entry<String, Set<Gene>> entry : map.entrySet()) {
                        if (entry.getValue().size() > 0) {
                            for (Gene gene : entry.getValue()) {
                                for (String keyword : keywords) {
                                    if (!keyword.equals(entry.getKey())) {
                                        Alteration alteration =
                                            AlterationUtils.getAlteration(gene.getHugoSymbol(), keyword, null, null, null, null);
                                        AlterationUtils.annotateAlteration(alteration, keyword);
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
        } else {
            TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
            typeaheadSearchResp.setAnnotation("Please search at least " + QUERY_MIN_LENGTH + " characters.");
            typeaheadSearchResp.setQueryType(TypeaheadQueryType.TEXT);
            result.add(typeaheadSearchResp);
        }
        return new ResponseEntity<>(getLimit(result, limit), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LinkedHashSet<NCITDrug>> searchDrugGet(String query, Integer limit) {
        if (limit == null) {
            limit = DEFAULT_RETURN_LIMIT;
        }
        return new ResponseEntity<>(getLimit(NCITDrugUtils.findDrugs(query), limit), HttpStatus.OK);
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

                    Set<Alteration> alterations = AlterationUtils.getAllAlterations(gene);
                    // When more than two keywords present, the index does not matter anymore.
                    // As long as there is match, return it.
                    if (keywords.size() > 2) {
                        Set<Alteration> keywordsMatches = null;
                        for (String keyword : keywords) {
                            if (!keyword.equals(entry.getKey())) {
                                List<Alteration> matches = AlterationUtils.lookupVariant(keyword, exactMatch, alterations);
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
                        result.addAll(convertVariant(new ArrayList<>(keywordsMatches), ""));
                    } else {
                        for (String keyword : keywords) {
                            if (!keyword.equals(entry.getKey()))
                                result.addAll(convertVariant(AlterationUtils.lookupVariant(keyword, exactMatch, alterations), keyword));
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

    private static void updateMap( Map<String, DrugMatch> map, String key, Gene gene, Set<Alteration> alterations, Drug drug, LevelOfEvidence level, TumorType tumorType, Double weight ) {
        if(!map.containsKey(key)) {
            DrugMatch drugMatch = new DrugMatch();
            drugMatch.setGene(gene);
            drugMatch.setLevelOfEvidence(level);
            drugMatch.setDrug(drug);
            drugMatch.setWeight(weight);
            map.put(key, drugMatch);
        }
        map.get(key).getAlterations().addAll(alterations);
        map.get(key).getTumorTypes().add(tumorType);
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
                        updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getOncoTreeType(), 4.0);
                        isMatch = true;
                    } else if (drug.getNcitCode() != null && drug.getNcitCode().toLowerCase().equals(query)) {
                        updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getOncoTreeType(), 4.0);
                        isMatch = true;
                    } else {
                        for (String synonym : drug.getSynonyms()) {
                            if (synonym.toLowerCase().equals(query)) {
                                updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getOncoTreeType(), 3.0);
                                isMatch = true;
                                break;
                            }
                        }
                    }
                    if(!exactMatch) {
                        String lowerCaseDrugName = drug.getDrugName().toLowerCase();
                        if (lowerCaseDrugName.startsWith(query)) {
                            updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getOncoTreeType(), 2.0);
                            isMatch = true;
                        } else if (lowerCaseDrugName.contains(query)) {
                            updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getOncoTreeType(), 1.5);
                            isMatch = true;
                        } else {
                            for (String synonym : drug.getSynonyms()) {
                                String lower = synonym.toLowerCase();

                                if(lower.startsWith(query)) {
                                    updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getOncoTreeType(), 1.0);
                                    isMatch = true;
                                    break;
                                } else if (lower.contains(query)) {
                                    updateMap(result, matchKey, evidence.getGene(), evidence.getAlterations(), drug, evidence.getLevelOfEvidence(), evidence.getOncoTreeType(), 0.5);
                                    isMatch = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        TreeSet<DrugMatch> drugMatches = new TreeSet<>(new DrugMatchComp());
        for(Map.Entry<String, DrugMatch> entry : result.entrySet()) {
            drugMatches.add(entry.getValue());
        }
        return drugMatches.stream().map(drugMatch -> newTypeaheadDrug(drugMatch)).collect(Collectors.toList());
    }

    private TypeaheadSearchResp newTypeaheadVariant(Alteration alteration) {
        TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
        typeaheadSearchResp.setGene(alteration.getGene());
        typeaheadSearchResp.setVariants(Collections.singleton(alteration));
        typeaheadSearchResp.setVariantExist(true);

        Query query = new Query();
        query.setEntrezGeneId(alteration.getGene().getEntrezGeneId());
        query.setAlteration(alteration.getAlteration());

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, false, null);
        typeaheadSearchResp.setOncogenicity(resp.getOncogenic());
        typeaheadSearchResp.setVUS(resp.getVUS());
        typeaheadSearchResp.setAnnotation(resp.getVariantSummary());
        // TODO: populate treatment info.

        Set<Evidence> evidenceList = new HashSet<>(EvidenceUtils.getEvidence(AlterationUtils.getRelevantAlterations(alteration), null, null, null));
        LevelOfEvidence highestSensitiveLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(evidenceList, LevelUtils.getSensitiveLevels());
        LevelOfEvidence highestResistanceLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(evidenceList, LevelUtils.getResistanceLevels());

        if (highestSensitiveLevel != null) {
            typeaheadSearchResp.setHighestSensitiveLevel(highestSensitiveLevel.getLevel());
        }
        if (highestResistanceLevel != null) {
            typeaheadSearchResp.setHighestResistanceLevel(highestResistanceLevel.getLevel());
        }

        if (alteration.getAlteration() != null && alteration.getAlteration().equalsIgnoreCase("oncogenic mutations")) {
            typeaheadSearchResp.setOncogenicity(Oncogenicity.YES.getOncogenic());
        }

        typeaheadSearchResp.setQueryType(TypeaheadQueryType.VARIANT);

        // TODO: switch to variant page once it's ready.
        typeaheadSearchResp.setLink("/gene/" + alteration.getGene().getHugoSymbol() + "/" + alteration.getAlteration());
        return typeaheadSearchResp;
    }

    private <T> LinkedHashSet<T> getLimit(LinkedHashSet<T> result, Integer limit) {
        if (limit == null)
            limit = DEFAULT_RETURN_LIMIT;
        Integer count = 0;
        LinkedHashSet<T> firstFew = new LinkedHashSet<>();
        Iterator<T> itr = result.iterator();
        while (itr.hasNext() && count < limit) {
            firstFew.add(itr.next());
            count++;
        }
        return firstFew;
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
            //Compare Oncogenicity. Treat YES, LIKELY, PREDICTED as the same
            Oncogenicity o1 = Oncogenicity.getByEffect(e1.getOncogenicity());
            Oncogenicity o2 = Oncogenicity.getByEffect(e2.getOncogenicity());
            if (o1 != null && (o1.equals(Oncogenicity.LIKELY) || o1.equals(Oncogenicity.PREDICTED))) {
                o1 = Oncogenicity.YES;
            }
            if (o2 != null && (o2.equals(Oncogenicity.LIKELY) || o2.equals(Oncogenicity.PREDICTED))) {
                o2 = Oncogenicity.YES;
            }
            Integer result = MainUtils.compareOncogenicity(o1, o2, true);
            if (result == 0) {
                // Compare highest sensitive level
                result = LevelUtils.compareLevel(LevelOfEvidence.getByLevel(e1.getHighestSensitiveLevel()), LevelOfEvidence.getByLevel(e2.getHighestSensitiveLevel()));
                if (result == 0) {
                    // Compare highest resistance level
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

class DrugMatchComp implements Comparator<DrugMatch> {
    @Override
    public int compare(DrugMatch d1, DrugMatch d2) {
        int result = d2.getWeight().compareTo(d1.getWeight()) ;
        if(result == 0) {
            result = LevelUtils.compareLevel(d1.getLevelOfEvidence(), d2.getLevelOfEvidence());
            if(result == 0) {
                result = d1.getGene().getHugoSymbol().compareTo(d2.getGene().getHugoSymbol());
            }
        }
        return result;
    }
}
