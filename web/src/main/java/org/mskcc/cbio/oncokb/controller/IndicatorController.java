/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.apache.commons.collections.CollectionUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author jgao
 */
@Controller
@RequestMapping(value = "/legacy-api/indicator.json")
public class IndicatorController {
    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<IndicatorQueryResp> getResult(
        @RequestBody EvidenceQueries body) {

        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null || body.getQueries() == null || body.getQueries().size() == 0) {
            return result;
        }

        String source = body.getSource() == null ? "oncokb" : body.getSource();

        for (Query query : body.getQueries()) {
            IndicatorQueryResp indicatorQuery = new IndicatorQueryResp();
            indicatorQuery.setQuery(query);

            Gene gene = query.getEntrezGeneId() == null ? GeneUtils.getGeneByHugoSymbol(query.getHugoSymbol()) :
                GeneUtils.getGeneByHugoSymbol(query.getHugoSymbol());
            indicatorQuery.setGeneExist(gene == null ? false : true);

            if (gene != null) {
                // Gene summary
                indicatorQuery.setGeneSummary(SummaryUtils.geneSummary(gene));

                Set<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterations(
                    gene, query.getAlteration(), query.getConsequence(),
                    query.getProteinStart(), query.getProteinEnd());
                Map<String, LevelOfEvidence> highestLevels = new HashMap<>();

                if (relevantAlterations == null || relevantAlterations.size() == 0) {
                    indicatorQuery.setVariantExist(false);
                } else {
                    indicatorQuery.setVariantExist(true);
                }

                Alteration alteration = AlterationUtils.getAlteration(query.getHugoSymbol(), query.getAlteration(), null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());

                Set<Alteration> alleles = new HashSet<>();
                List<OncoTreeType> oncoTreeTypes = new ArrayList<>();
                
                if (query.getTumorType() != null) {
                    oncoTreeTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(query.getTumorType(), source);
                    // Tumor type summary
                    Set<Evidence> tumorTypeSummary = EvidenceUtils.getEvidence(Collections.singleton(alteration), Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), new HashSet<>(oncoTreeTypes), null);
                    if (tumorTypeSummary != null && tumorTypeSummary.size() > 0) {
                        indicatorQuery.setTumorTypeSummary(tumorTypeSummary.iterator().next().getDescription());
                    }
                }
                
                if (alteration != null) {
                    alleles = AlterationUtils.getAlleleAlterations(alteration);

                    // Mutation summary
                    Set<Evidence> mutationSummary = EvidenceUtils.getEvidence(Collections.singleton(alteration), Collections.singleton(EvidenceType.MUTATION_SUMMARY), null);
                    if (mutationSummary != null && mutationSummary.size() > 0) {
                        indicatorQuery.setVariantSummary(mutationSummary.iterator().next().getDescription());
                    }else {
                        indicatorQuery.setVariantSummary(SummaryUtils.variantSummary(Collections.singleton(gene), new ArrayList<Alteration>(relevantAlterations), query.getAlteration(), new HashSet<OncoTreeType>(oncoTreeTypes), query.getTumorType()));
                    }
                }
                
                indicatorQuery.setVUS(isVUS(
                    EvidenceUtils.getRelevantEvidences(query, source, body.getGeneStatus(), Collections.singleton(EvidenceType.VUS), null)
                ));

                if (alleles == null || alleles.size() == 0) {
                    indicatorQuery.setAlleleExist(false);
                } else {
                    indicatorQuery.setAlleleExist(true);
                }
                if (indicatorQuery.getVariantExist() && !indicatorQuery.getVUS()) {
                    Oncogenicity oncogenicity = MainUtils.findHighestOncogenicByEvidences(
                        EvidenceUtils.getRelevantEvidences(query, source, body.getGeneStatus(), Collections.singleton(EvidenceType.ONCOGENIC), null)
                    );
                    indicatorQuery.setOncogenic(oncogenicity == null ? "" : oncogenicity.getDescription());

                    Set<Evidence> treatmentEvidences = EvidenceUtils.getRelevantEvidences(query, source, body.getGeneStatus(),
                        MainUtils.getTreatmentEvidenceTypes(),
                        (body.getLevels() != null ?
                            new HashSet<LevelOfEvidence>(CollectionUtils.intersection(body.getLevels(),
                                LevelUtils.getPublicLevels())) : LevelUtils.getPublicLevels()));

                    if (treatmentEvidences != null) {
                        indicatorQuery.setTreatments(getIndicatorQueryTreatments(treatmentEvidences));
                        highestLevels = findHighestLevel(treatmentEvidences);
                    }
                } else if (indicatorQuery.getAlleleExist() || indicatorQuery.getVUS()) {
                    Oncogenicity oncogenicity = MainUtils.setToAlleleOncogenicity(MainUtils.findHighestOncogenicByEvidences(
                        EvidenceUtils.getEvidence(alleles, Collections.singleton(EvidenceType.ONCOGENIC), null)));

                    indicatorQuery.setOncogenic(oncogenicity == null ? "" : oncogenicity.getDescription());
                    Set<Evidence> treatmentEvidences = EvidenceUtils.getEvidence(alleles, MainUtils.getTreatmentEvidenceTypes(),
                        (
                            body.getLevels() != null ?
                                new HashSet<LevelOfEvidence>(CollectionUtils.intersection(body.getLevels(),
                                    LevelUtils.getPublicLevels())) : LevelUtils.getPublicLevels())
                    );
                    
                    indicatorQuery.setTreatments(getIndicatorQueryTreatments(treatmentEvidences));
                    highestLevels = findHighestLevel(treatmentEvidences);

                    LevelOfEvidence sensitive = highestLevels.get("sensitive");
                    if (sensitive != null)
                        highestLevels.put("sensitive", LevelUtils.setToAlleleLevel(sensitive, true));
                    highestLevels.put("resistant", null);
                }
                indicatorQuery.setHighestSensitiveLevel(highestLevels.get("sensitive") == null ? "" : highestLevels.get("sensitive").name());
                indicatorQuery.setHighestResistanceLevel(highestLevels.get("resistant") == null ? "" : highestLevels.get("resistant").name());
            }
            result.add(indicatorQuery);
        }
        return result;
    }

    private Set<IndicatorQueryTreatment> getIndicatorQueryTreatments(Set<Evidence> evidences) {
        Set<IndicatorQueryTreatment> treatments = new HashSet<>();
        if (evidences != null) {
            for (Evidence evidence : evidences) {
                Set<String> pmids = new HashSet<>();
                for (Article article : evidence.getArticles()) {
                    pmids.add(article.getPmid());
                }
                for (Treatment treatment : evidence.getTreatments()) {
                    IndicatorQueryTreatment indicatorQueryTreatment = new IndicatorQueryTreatment();
                    indicatorQueryTreatment.setDrugs(treatment.getDrugs());
                    indicatorQueryTreatment.setApprovedIndications(treatment.getApprovedIndications());
                    indicatorQueryTreatment.setLevel(evidence.getLevelOfEvidence());
                    indicatorQueryTreatment.setPmids(pmids);
                    treatments.add(indicatorQueryTreatment);
                }
            }
        }
        return treatments;
    }

    private Boolean isVUS(Set<Evidence> evidenceList) {
        for (Evidence evidence : evidenceList) {
            if (evidence.getEvidenceType().equals(EvidenceType.VUS)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, LevelOfEvidence> findHighestLevel(Set<Evidence> evidences) {
        List<LevelOfEvidence> sensitiveLevels = new ArrayList<>();
        sensitiveLevels.add(LevelOfEvidence.LEVEL_4);
        sensitiveLevels.add(LevelOfEvidence.LEVEL_3B);
        sensitiveLevels.add(LevelOfEvidence.LEVEL_3A);
        sensitiveLevels.add(LevelOfEvidence.LEVEL_2B);
        sensitiveLevels.add(LevelOfEvidence.LEVEL_2A);
        sensitiveLevels.add(LevelOfEvidence.LEVEL_1);

        List<LevelOfEvidence> resistanceLevels = new ArrayList<>();
        resistanceLevels.add(LevelOfEvidence.LEVEL_R3);
        resistanceLevels.add(LevelOfEvidence.LEVEL_R2);
        resistanceLevels.add(LevelOfEvidence.LEVEL_R1);

        int levelSIndex = -1;
        int levelRIndex = -1;

        Map<String, LevelOfEvidence> levels = new HashMap<>();

        if (evidences != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getLevelOfEvidence() != null) {
                    int _index = -1;
                    if (evidence.getKnownEffect().equalsIgnoreCase("sensitive")) {
                        _index = sensitiveLevels.indexOf(evidence.getLevelOfEvidence());
                        if (_index > levelSIndex) {
                            levelSIndex = _index;
                        }
                    } else if (evidence.getKnownEffect().equalsIgnoreCase("resistant")) {
                        _index = resistanceLevels.indexOf(evidence.getLevelOfEvidence());
                        if (_index > levelRIndex) {
                            levelRIndex = _index;
                        }
                    }
                }
            }
        }
        levels.put("sensitive", levelSIndex > -1 ? sensitiveLevels.get(levelSIndex) : null);
        levels.put("resistant", levelRIndex > -1 ? resistanceLevels.get(levelRIndex) : null);
        return levels;
    }
}