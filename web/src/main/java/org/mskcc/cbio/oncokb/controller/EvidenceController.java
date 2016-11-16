/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.apache.commons.collections.CollectionUtils;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author jgao
 */
@Controller
@RequestMapping(value = "/legacy-api/evidence.json")
public class EvidenceController {
    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    List<List<Evidence>> getEvidence(
        HttpMethod method,
        @RequestParam(value = "entrezGeneId", required = false) String entrezGeneId,
        @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol,
        @RequestParam(value = "alteration", required = false) String alteration,
        @RequestParam(value = "tumorType", required = false) String tumorType,
        @RequestParam(value = "evidenceType", required = false) String evidenceType,
        @RequestParam(value = "consequence", required = false) String consequence,
        @RequestParam(value = "proteinStart", required = false) String proteinStart,
        @RequestParam(value = "proteinEnd", required = false) String proteinEnd,
        @RequestParam(value = "geneStatus", required = false) String geneStatus,
        @RequestParam(value = "source", required = false) String source,
        @RequestParam(value = "levels", required = false) String levels,
        @RequestParam(value = "highestLevelOnly", required = false) Boolean highestLevelOnly) {

        List<List<Evidence>> evidences = new ArrayList<>();

        Map<String, Object> requestQueries = MainUtils.GetRequestQueries(entrezGeneId, hugoSymbol, alteration,
            tumorType, evidenceType, consequence, proteinStart, proteinEnd, geneStatus, source, levels);

        if (requestQueries == null) {
            return new ArrayList<>();
        }

        List<EvidenceQueryRes> evidenceQueries = processRequest(
            (List<Query>) requestQueries.get("queries"),
            new HashSet<>((List<EvidenceType>) requestQueries.get("evidenceTypes")),
            geneStatus, source, new HashSet<>((List<LevelOfEvidence>) requestQueries.get("levels")), highestLevelOnly);

        if (evidenceQueries != null) {
            for (EvidenceQueryRes query : evidenceQueries) {
                evidences.add(query.getEvidences());
            }
        }

        return evidences;
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<EvidenceQueryRes> getEvidence(
        @RequestBody EvidenceQueries body) {

        List<EvidenceQueryRes> result = new ArrayList<>();
        if (body.getQueries().size() > 0) {
            List<Query> requestQueries = body.getQueries();
            Set<EvidenceType> evidenceTypes = new HashSet<>();

            if (body.getEvidenceTypes() != null) {
                for (String type : body.getEvidenceTypes().split("\\s*,\\s*")) {
                    EvidenceType et = EvidenceType.valueOf(type);
                    evidenceTypes.add(et);
                }
            } else {
                evidenceTypes.add(EvidenceType.GENE_SUMMARY);
                evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
            }

            result = processRequest(requestQueries, evidenceTypes, body.getGeneStatus(), body.getSource(),
                body.getLevels(), body.getHighestLevelOnly());
        }

        return result;
    }

    // TODO: support hugoSymbol/entrezGeneId fusion without primary gene, see indicator controller
    private List<EvidenceQueryRes> processRequest(List<Query> requestQueries, Set<EvidenceType> evidenceTypes,
                                                  String geneStatus, String source,
                                                  Set<LevelOfEvidence> levelOfEvidences, Boolean highestLevelOnly) {
        List<EvidenceQueryRes> evidenceQueries = new ArrayList<>();

        if (source == null) {
            source = "quest";
        }
        
        if (evidenceTypes == null) {
            evidenceTypes = new HashSet<>(MainUtils.getAllEvidenceTypes());    
        }
        
        if (levelOfEvidences == null) {
            levelOfEvidences = LevelUtils.getPublicLevels();
        }

        if (requestQueries == null || requestQueries.size() == 0) {
            Set<Evidence> evidences = new HashSet<>();
            if ((evidenceTypes != null && evidenceTypes.size() > 0) ||
                (levelOfEvidences != null && levelOfEvidences.size() > 0)) {
                evidences = EvidenceUtils.getEvidenceByEvidenceTypesAndLevels(evidenceTypes, levelOfEvidences);
            }
            EvidenceQueryRes query = new EvidenceQueryRes();
            query.setEvidences(new ArrayList<>(evidences));
            return Collections.singletonList(query);
        } else {
            for (Query requestQuery : requestQueries) {
                EvidenceQueryRes query = new EvidenceQueryRes();

                query.setQuery(requestQuery);
                query.setGene(getGene(requestQuery.getEntrezGeneId(), requestQuery.getHugoSymbol()));

                if (query.getGene() != null) {
                    query.setOncoTreeTypes(TumorTypeUtils.getMappedOncoTreeTypesBySource(requestQuery.getTumorType(), source));
                    
                    if (requestQuery.getAlteration() != null) {
                        Set<Alteration> relevantAlts = AlterationUtils.getRelevantAlterations(query.getGene(), requestQuery.getAlteration(), requestQuery.getConsequence(), requestQuery.getProteinStart(), requestQuery.getProteinEnd());
                        query.setAlterations(relevantAlts == null ? null : new ArrayList<>(relevantAlts));

                        Alteration alteration = AlterationUtils.getAlteration(requestQuery.getHugoSymbol(), requestQuery.getAlteration(), AlterationType.MUTATION.name(), requestQuery.getConsequence(), requestQuery.getProteinStart(), requestQuery.getProteinEnd());
                        Set<Alteration> allelesAlts = AlterationUtils.getAlleleAlterations(alteration);
                        query.setAlleles(new ArrayList<>(allelesAlts));
                    } else if(query.getOncoTreeTypes() != null && query.getOncoTreeTypes().size() > 0) {
                        // if no alteration assigned, but has tumor type
                        query.setAlterations(new ArrayList<Alteration>(AlterationUtils.getAllAlterations(query.getGene())));
                    }
                }
                if(levelOfEvidences != null) {
                    query.setLevelOfEvidences(new ArrayList<LevelOfEvidence>(levelOfEvidences));
                }
                evidenceQueries.add(query);
            }
        }

        return assignEvidence(EvidenceUtils.getEvidence(evidenceQueries, evidenceTypes, geneStatus, levelOfEvidences),
            evidenceQueries, highestLevelOnly);
    }

    private Gene getGene(Integer entrezGeneId, String hugoSymbol) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Gene gene = null;

        if (entrezGeneId != null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        } else if (hugoSymbol != null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }

        return gene;
    }


    private List<EvidenceQueryRes> assignEvidence(Set<Evidence> evidences, List<EvidenceQueryRes> evidenceQueries,
                                                  Boolean highestLevelOnly) {
        highestLevelOnly = highestLevelOnly == null ? false : highestLevelOnly;

        for (EvidenceQueryRes query : evidenceQueries) {
            query.setEvidences(
                new ArrayList<>(
                    EvidenceUtils.keepHighestLevelForSameTreatments(EvidenceUtils.filterEvidence(evidences, query))));

            // Attach evidence if query doesn't contain any alteration and has alleles.
            if ((query.getAlterations() == null || query.getAlterations().isEmpty() || AlterationUtils.excludeVUS(query.getGene(), new HashSet<>(query.getAlterations())).size() == 0) && (query.getAlleles() != null && !query.getAlleles().isEmpty())) {
                // Get oncogenic and mutation effect evidences
                Set<Alteration> alleles = new HashSet<>(query.getAlleles());
                Set<Evidence> oncogenics = EvidenceUtils.getEvidence(alleles, Collections.singleton(EvidenceType.ONCOGENIC), null);
                Oncogenicity highestOncogenic = MainUtils.findHighestOncogenicByEvidences(oncogenics);
                if (highestOncogenic != null) {
                    Evidence recordMatchHighestOncogenicity = null;

                    for (Evidence evidence : oncogenics) {
                        if (evidence.getKnownEffect() != null) {
                            Oncogenicity oncogenicity = Oncogenicity.getByLevel(evidence.getKnownEffect());
                            if (oncogenicity != null && oncogenicity.equals(highestOncogenic)) {
                                recordMatchHighestOncogenicity = evidence;
                                break;
                            }
                        }
                    }

                    if (recordMatchHighestOncogenicity != null) {
                        Oncogenicity alleleOncogenicity = MainUtils.setToAlleleOncogenicity(highestOncogenic);
                        Evidence evidence = new Evidence();
                        evidence.setEvidenceId(recordMatchHighestOncogenicity.getEvidenceId());
                        evidence.setGene(recordMatchHighestOncogenicity.getGene());
                        evidence.setEvidenceType(EvidenceType.ONCOGENIC);
                        evidence.setKnownEffect(alleleOncogenicity == null ? "" : alleleOncogenicity.getDescription());
                        query.getEvidences().add(evidence);
                    }
                }

                Set<Alteration> altsWithHighestOncogenicity = new HashSet<>();

                for (Evidence evidence : EvidenceUtils.getEvidenceBasedOnHighestOncogenicity(new HashSet<Evidence>(oncogenics))) {
                    for (Alteration alt : evidence.getAlterations()) {
                        if (alleles.contains(alt)) {
                            altsWithHighestOncogenicity.add(alt);
                        }
                    }
                }

                Set<Evidence> mutationEffectsEvis = EvidenceUtils.getEvidence(altsWithHighestOncogenicity, Collections.singleton(EvidenceType.MUTATION_EFFECT), null);
                if (mutationEffectsEvis != null && mutationEffectsEvis.size() > 0) {
                    Set<String> effects = new HashSet<>();

                    for (Evidence mutationEffectEvi : mutationEffectsEvis) {
                        effects.add(mutationEffectEvi.getKnownEffect());
                    }

                    Evidence mutationEffect = new Evidence();
                    Evidence example = mutationEffectsEvis.iterator().next();
                    mutationEffect.setEvidenceId(example.getEvidenceId());
                    mutationEffect.setGene(example.getGene());
                    mutationEffect.setEvidenceType(EvidenceType.MUTATION_EFFECT);
                    mutationEffect.setKnownEffect(MainUtils.getAlleleConflictsMutationEffect(effects));
                    query.getEvidences().add(mutationEffect);
                }

                // Get treatment evidences
                Set<Evidence> alleleEvidences = EvidenceUtils.getEvidence(alleles, MainUtils.getSensitiveTreatmentEvidenceTypes(), LevelUtils.getPublicLevels());
                Set<Evidence> alleleEvidencesCopy = new HashSet<>();
                if (alleleEvidences != null) {
                    LevelOfEvidence highestLevelFromEvidence = LevelUtils.getHighestLevelFromEvidence(new HashSet<>(alleleEvidences));
                    if (highestLevelFromEvidence != null && LevelUtils.getPublicLevels().contains(highestLevelFromEvidence)) {
                        alleleEvidences = EvidenceUtils.getEvidence(alleles, MainUtils.getSensitiveTreatmentEvidenceTypes(), Collections.singleton(highestLevelFromEvidence));
                        for (Evidence evidence : alleleEvidences) {
                            Evidence tmpEvidence = new Evidence(evidence);
                            tmpEvidence.setLevelOfEvidence(LevelUtils.setToAlleleLevel(evidence.getLevelOfEvidence(), CollectionUtils.intersection(Collections.singleton(evidence.getOncoTreeType()), query.getOncoTreeTypes()).size() > 0));
                            alleleEvidencesCopy.add(tmpEvidence);
                        }
                        query.getEvidences().addAll(alleleEvidencesCopy);
                    }
                }
            }
            
            if(highestLevelOnly) {
                Set<Evidence> allEvidences = new HashSet<>(query.getEvidences());
                List<Evidence> filteredEvidences = new ArrayList<>();

                // Get highest sensitive evidences
                Set<Evidence> sensitiveEvidences = EvidenceUtils.getSensitiveEvidences(allEvidences);
                filteredEvidences.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(sensitiveEvidences));

                // Get highest resistance evidences
                Set<Evidence> resistanceEvidences = EvidenceUtils.getResistanceEvidences(allEvidences);
                filteredEvidences.addAll(EvidenceUtils.getOnlyHighestLevelEvidences(resistanceEvidences));
                
                query.setEvidences(filteredEvidences);
            }
        }
        return evidenceQueries;
    }
}