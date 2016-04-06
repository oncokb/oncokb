/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
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
@RequestMapping(value = "/api/indicator.json")
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

            Gene gene = GeneUtils.getGene(query.getEntrezGeneId(), query.getHugoSymbol());
            indicatorQuery.setGeneExist(gene == null ? false : true);

            if (gene != null) {
                List<Alteration> relevantAlterations = AlterationUtils.getRelevantAlterations(
                        gene, query.getAlteration(), query.getConsequence(),
                        query.getProteinStart(), query.getProteinEnd());

                if(relevantAlterations == null || relevantAlterations.size() == 0) {
                    indicatorQuery.setVariantExist(false);
                }else {
                    indicatorQuery.setVariantExist(true);
                }

                indicatorQuery.setOncogenic(findHighestOncogenic(
                        EvidenceUtils.getRelevantEvidences(query, source, body.getGeneStatus(), Arrays.asList(EvidenceType.ONCOGENIC), null)
                ));

                indicatorQuery.setVUS(isVUS(
                        EvidenceUtils.getRelevantEvidences(query, source, body.getGeneStatus(), Arrays.asList(EvidenceType.VUS), null)
                ));

                List<EvidenceType> evidenceTypes = new ArrayList<>();
                evidenceTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE);
                evidenceTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
                evidenceTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE);
                evidenceTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);

                Map<String, String> highestLevels = findHighestLevel(
                        EvidenceUtils.getRelevantEvidences(query, source, body.getGeneStatus(), evidenceTypes, body.getLevels()
                        )
                );
                indicatorQuery.setHighestSensitiveLevel(highestLevels.get("sensitive"));
                indicatorQuery.setHighestResistanceLevel(highestLevels.get("resistant"));
            }
            result.add(indicatorQuery);
        }
        return result;
    }

    private Boolean isVUS(List<Evidence> evidenceList) {
        for(Evidence evidence : evidenceList) {
            if(evidence.getEvidenceType().equals(EvidenceType.VUS)) {
                return true;
            }
        }
        return false;
    }
    private String findHighestOncogenic(List<Evidence> evidences) {
        List<String> levels = Arrays.asList("-1", "0", "2", "1");
        List<String> levelsExplanation = Arrays.asList("Unknown", "Not Oncogenic", "Likely Oncogenic", "Oncogenic");

        int index = -1;

        if (evidences != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getKnownEffect() != null) {
                    int _index = -1;
                    _index = levels.indexOf(evidence.getKnownEffect());
                    if (_index > index) {
                        index = _index;
                    }
                }
            }
        }

        return index > -1 ? levelsExplanation.get(index) : "";
    }

    private Map<String, String> findHighestLevel(List<Evidence> evidences) {
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

        Map<String, String> levels = new HashMap<>();

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
        levels.put("sensitive", levelSIndex > -1 ? sensitiveLevels.get(levelSIndex).name() : null);
        levels.put("resistant", levelRIndex > -1 ? resistanceLevels.get(levelRIndex).name() : null);
        return levels;
    }
}