package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * Created by Hongxin on 10/28/16.
 */
@Controller
public class PrivateSearchApiController implements PrivateSearchApi {
    private Integer TYPEAHEAD_RETURN_LIMIT = 5;

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
        @ApiParam(value = "The search query, it could be hugoSymbol, entrezGeneId or variant.", required = true) @RequestParam(value = "query") String query,
        @ApiParam(value = "The limit of returned result.") @RequestParam(value = "limit", defaultValue = "5", required = false) Integer limit) {
        LinkedHashSet<TypeaheadSearchResp> result = new LinkedHashSet<>();
        if (limit == null) {
            limit = TYPEAHEAD_RETURN_LIMIT;
        }
        if (query != null) {
            List<String> keywords = Arrays.asList(query.trim().split("\\s+"));

            if (keywords.size() == 1) {
                // Blur search gene
                result.addAll(convertGene(GeneUtils.searchGene(keywords.get(0), false), keywords.get(0)));

                // Blur search variant
                result.addAll(convertVariant(AlterationUtils.lookupVariant(keywords.get(0), false, AlterationUtils.getAllAlterations()), keywords.get(0)));
            } else {
                // Assume one of the keyword is gene
                Map<String, Set<Gene>> map = new HashedMap();
                for (String keyword : keywords) {
                    map.put(keyword, GeneUtils.searchGene(keyword, true));
                }

                result.addAll(getMatch(map, keywords, false));

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
                                        if (typeaheadSearchResp.getOncogenicity() != null
                                            && !typeaheadSearchResp.getOncogenicity().isEmpty()) {
                                            result.add(typeaheadSearchResp);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ResponseEntity<>(getLimit(result, limit), HttpStatus.OK);
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
                typeaheadSearchResp.setLink("/genes/" + gene.getHugoSymbol());
                typeaheadSearchResp.setQueryType("gene");

                if (evidences.containsKey(gene)) {
                    LevelOfEvidence highestSensitiveLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(evidences.get(gene), LevelUtils.getPublicSensitiveLevels());
                    LevelOfEvidence highestResistanceLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(evidences.get(gene), LevelUtils.getPublicResistanceLevels());
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

    private TypeaheadSearchResp newTypeaheadVariant(Alteration alteration) {
        TypeaheadSearchResp typeaheadSearchResp = new TypeaheadSearchResp();
        typeaheadSearchResp.setGene(alteration.getGene());
        typeaheadSearchResp.setVariant(alteration);
        typeaheadSearchResp.setVariantExist(true);

        Query query = new Query();
        query.setEntrezGeneId(alteration.getGene().getEntrezGeneId());
        query.setAlteration(alteration.getAlteration());

        IndicatorQueryResp resp = IndicatorUtils.processQuery(query, null, null, null, false);
        typeaheadSearchResp.setOncogenicity(resp.getOncogenic());
        typeaheadSearchResp.setVUS(resp.getVUS());
        typeaheadSearchResp.setAnnotation(resp.getVariantSummary());
        // TODO: populate treatment info.

        Set<Evidence> evidenceList = new HashSet<>(EvidenceUtils.getEvidence(AlterationUtils.getRelevantAlterations(alteration), null, null, null));
        LevelOfEvidence highestSensitiveLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(evidenceList, LevelUtils.getPublicSensitiveLevels());
        LevelOfEvidence highestResistanceLevel = LevelUtils.getHighestLevelFromEvidenceByLevels(evidenceList, LevelUtils.getPublicResistanceLevels());

        if (highestSensitiveLevel != null) {
            typeaheadSearchResp.setHighestSensitiveLevel(highestSensitiveLevel.getLevel());
        }
        if (highestResistanceLevel != null) {
            typeaheadSearchResp.setHighestResistanceLevel(highestResistanceLevel.getLevel());
        }

        typeaheadSearchResp.setQueryType("variant");

        // TODO: switch to variant page once it's ready.
        typeaheadSearchResp.setLink("/genes/" + alteration.getGene().getHugoSymbol());
        return typeaheadSearchResp;
    }

    private LinkedHashSet<TypeaheadSearchResp> getLimit(LinkedHashSet<TypeaheadSearchResp> result, Integer limit) {
        if (limit == null)
            limit = TYPEAHEAD_RETURN_LIMIT;
        Integer count = 0;
        LinkedHashSet<TypeaheadSearchResp> firstFew = new LinkedHashSet<>();
        Iterator<TypeaheadSearchResp> itr = result.iterator();
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
        if (e1 == null || e1.getVariant() == null) {
            return 1;
        }
        if (e2 == null || e2.getVariant() == null) {
            return -1;
        }
        String name1 = e1.getVariant().getAlteration().toLowerCase();
        String name2 = e2.getVariant().getAlteration().toLowerCase();
        if (e1.getVariant().getName() != null && e2.getVariant().getName() != null) {
            name1 = e1.getVariant().getName().toLowerCase();
            name2 = e2.getVariant().getName().toLowerCase();
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
