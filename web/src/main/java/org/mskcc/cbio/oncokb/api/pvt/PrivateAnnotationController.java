package org.mskcc.cbio.oncokb.api.pvt;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCompare.Tree;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.mskcc.cbio.oncokb.util.EvidenceTypeUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.IndicatorUtils;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.Objects;

@RestController
@Api(tags = "Annotation")
public class PrivateAnnotationController {

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

                // // Blur search variant
                result.addAll(findActionableGenesByAlterationSearch(keywords.get(0)));

                // // // Blur search cancer type
                result.addAll(findActionalGenesByCancerType(keywords.get(0)));

                // // If the keyword contains dash and result is empty, then we should return both fusion genes
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
                        indicatorQuery.setAlteration(alteration.getAlteration());
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
                result.addAll(findActionalGenesByCancerType(fullKeywords));
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
            indicatorQuery.setAlteration(alteration.getName());
            indicatorQuery.setEntrezGeneId(alteration.getGene().getEntrezGeneId());
            indicatorQuery.setHugoSymbol(alteration.getGene().getHugoSymbol());
            AnnotationSearchResult annotationSearchResult = new AnnotationSearchResult();
            annotationSearchResult.setQueryType(AnnotationSearchQueryType.VARIANT);
            annotationSearchResult.setIndicatorQueryResp(IndicatorUtils.processQuery(indicatorQuery, null, null, null));
            result.add(annotationSearchResult);
        }
        return result;
    }

    private LinkedHashSet<AnnotationSearchResult> findActionalGenesByCancerType(String query) {

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
                        SearchObject searchObject = new SearchObject();
                        searchObject.setGene(evidence.getGene());
                        searchObject.setAlteration(evidence.getAlterations().iterator().next());
                        searchObject.setTumorType(tumorType);
                        searchObjects.add(searchObject);
                }
            }
        }

        for (SearchObject searchObject: searchObjects) {
            Query indicatorQuery = new Query();
            indicatorQuery.setEntrezGeneId(searchObject.getGene().getEntrezGeneId());
            indicatorQuery.setHugoSymbol(searchObject.getGene().getHugoSymbol());
            indicatorQuery.setAlteration(searchObject.getAlteration().getName());
            if (searchObject.getTumorType().getMainType().toLowerCase().contains(query)) {
                indicatorQuery.setTumorType(searchObject.getTumorType().getMainType());
            } else {
                indicatorQuery.setTumorType(searchObject.getTumorType().getSubtype());
            }
            AnnotationSearchResult annotationSearchResult = new AnnotationSearchResult();
            annotationSearchResult.setQueryType(AnnotationSearchQueryType.CANCER_TYPE);
            annotationSearchResult.setIndicatorQueryResp(IndicatorUtils.processQuery(indicatorQuery, null, null, null));
            result.add(annotationSearchResult);
        }

        return result;
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
            if (a1.getQueryType().equals(TypeaheadQueryType.GENE)) {
                name1 = i1.getQuery().getHugoSymbol().toLowerCase();
                name2 = i2.getQuery().getHugoSymbol().toLowerCase();
            }
            if (a1.getQueryType().equals(TypeaheadQueryType.VARIANT)) {
                name1 = i1.getQuery().getAlteration().toLowerCase();
                name2 = i2.getQuery().getAlteration().toLowerCase();
            }
            if (a1.getQueryType().equals(TypeaheadQueryType.CANCER_TYPE)) {
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
                            result = name1.compareTo(name2);
                            if (result == 0) {
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


