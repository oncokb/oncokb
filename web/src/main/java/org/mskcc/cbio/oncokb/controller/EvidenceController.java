/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
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
@RequestMapping(value = "/api/evidence.json")
public class EvidenceController {
    private static Logger logger = Logger.getLogger(EvidenceController.class);

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
            @RequestParam(value = "geneStatus", required = false) String geneStatus,
            @RequestParam(value = "source", required = false) String source) {

        List<List<Evidence>> evidences = new ArrayList<>();
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        List<Query> requestQueries = new ArrayList<>();
        List<EvidenceType> evidenceTypes = new ArrayList<>();
        List<EvidenceQueryRes> evidenceQueries = new ArrayList<>();

        String[] genes = {};

        if (entrezGeneId != null) {
            for (String id : entrezGeneId.split(",")) {
                Query requestQuery = new Query();
                requestQuery.setEntrezGeneId(Integer.parseInt(id));
                requestQueries.add(requestQuery);
            }
        } else if (hugoSymbol != null) {
            for (String symbol : hugoSymbol.split(",")) {
                Query requestQuery = new Query();
                requestQuery.setHugoSymbol(symbol);
                requestQueries.add(requestQuery);
            }
        } else {
            List<Evidence> evidenceList = new ArrayList<Evidence>(evidenceBo.findAll());
            evidences.add(evidenceList);
            return evidences;
        }

        if (evidenceType != null) {
            for (String type : evidenceType.split(",")) {
                EvidenceType et = EvidenceType.valueOf(type);
                evidenceTypes.add(et);
            }
        } else {
            evidenceTypes.add(EvidenceType.GENE_SUMMARY);
            evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
        }

        if (alteration != null) {
            String[] alts = alteration.split(",");
            String[] consequences = null;
            if (requestQueries.size() == alts.length) {
                Boolean consequenceLengthMatch = false;

                if (consequence != null) {
                    consequences = consequence.split(",");
                    if (consequences.length == alts.length) {
                        consequenceLengthMatch = true;
                    }
                }

                for (int i = 0; i < requestQueries.size(); i++) {
                    requestQueries.get(i).setTumorType(tumorType);
                    requestQueries.get(i).setAlteration(alts[i]);
                    requestQueries.get(i).setConsequence(consequenceLengthMatch ? consequences[i] : null);
                }

                evidenceQueries = processRequest(requestQueries, evidenceTypes, geneStatus, source);

                for (EvidenceQueryRes query : evidenceQueries) {
                    evidences.add(query.getEvidences());
                }
                return evidences;
            } else {
                return new ArrayList<>();
            }
        } else {
            evidenceQueries = processRequest(requestQueries, evidenceTypes, geneStatus, source);
            for (EvidenceQueryRes query : evidenceQueries) {
                evidences.add(query.getEvidences());
            }
            return evidences;
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<EvidenceQueryRes> getEvidence(
            @RequestBody EvidenceQueries body) {
        List<EvidenceQueryRes> result = new ArrayList<>();
        if (body.getQueries().size() > 0) {
            List<Query> requestQueries = body.getQueries();
            List<EvidenceType> evidenceTypes = new ArrayList<>();

            if (body.getEvidenceTypes() != null) {
                for (String type : body.getEvidenceTypes().split(",")) {
                    EvidenceType et = EvidenceType.valueOf(type);
                    evidenceTypes.add(et);
                }
            } else {
                evidenceTypes.add(EvidenceType.GENE_SUMMARY);
                evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
            }

            result = processRequest(requestQueries, evidenceTypes, body.getGeneStatus(), body.getSource());
        }

        return result;
    }

    private List<EvidenceQueryRes> processRequest(List<Query> requestQueries, List<EvidenceType> evidenceTypes, String geneStatus, String source) {
        Map<Integer, List<Alteration>> mappedGeneAlterations = new HashMap<>();
        Map<String, List<TumorType>> mappedTumorTypes = new HashMap<>();
        Map<String, List<Alteration>> mappedAlterations = new HashMap<>();
        List<EvidenceQueryRes> evidenceQueries = new ArrayList<>();
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();

        for (Query requestQuery : requestQueries) {
            EvidenceQueryRes query = new EvidenceQueryRes();

            query.setId(requestQuery.getId());
            query.setQueryGene(requestQuery.getHugoSymbol());;
            query.setQueryAlteration(requestQuery.getAlteration());;
            query.setQueryTumorType(requestQuery.getTumorType());
            query.setGene(getGene(requestQuery.getEntrezGeneId(), requestQuery.getHugoSymbol()));

            if (query.getGene() != null) {
                if (!mappedGeneAlterations.containsKey(query.getGene().getEntrezGeneId())) {
                    mappedGeneAlterations.put(query.getGene().getEntrezGeneId(), alterationBo.findAlterationsByGene(Collections.singleton(query.getGene())));
                }
                if (requestQuery.getAlteration() != null) {
                    String consequence = requestQuery.getConsequence();
                    String alteration = requestQuery.getAlteration();
                    Gene gene = query.getGene();
                    String id = gene.getHugoSymbol() + alteration + (consequence == null ? "" : consequence);
                    if (!mappedAlterations.containsKey(id)) {
                        mappedAlterations.put(id, getAlterations(query.getGene(), alteration, consequence, mappedGeneAlterations.get(gene.getEntrezGeneId())));
                    }
                    query.setAlterations(mappedAlterations.get(id));
                }

                String tumorType = requestQuery.getTumorType();
                if (!mappedTumorTypes.containsKey(tumorType)) {
                    mappedTumorTypes.put(tumorType, getTumorTypes(tumorType, source));
                }
                query.setTumorTypes(mappedTumorTypes.get(tumorType));

                evidenceQueries.add(query);
            }
        }

        return assignEvidence(getEvidence(evidenceQueries, evidenceTypes, geneStatus), evidenceQueries);
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

    private List<Alteration> getAlterations(Gene gene, String alteration, String consequence, List<Alteration> fullAlterations) {
        List<Alteration> alterations = new ArrayList<>();
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        VariantConsequence variantConsequence = null;

        if (gene != null && alteration != null) {
            if (consequence != null) {
                //Consequence format  a, b+c, d ... each variant pair (gene + alteration) could have one or multiple consequences. Multiple consequences are separated by '+'
                for (String con : consequence.split("\\+")) {
                    Alteration alt = new Alteration();
                    alt.setAlteration(alteration);
                    variantConsequence = VariantConsequenceUtils.findVariantConsequenceByTerm(con);
                    alt.setConsequence(variantConsequence);
                    alt.setAlterationType(AlterationType.MUTATION);
                    alt.setGene(gene);

                    AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                    List<Alteration> alts = alterationBo.findRelevantAlterations(alt, fullAlterations);
                    if (!alts.isEmpty()) {
                        alterations.addAll(alts);
                    }
                }
            } else {
                Alteration alt = new Alteration();
                alt.setAlteration(alteration);
                alt.setAlterationType(AlterationType.MUTATION);
                alt.setGene(gene);

                AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                List<Alteration> alts = alterationBo.findRelevantAlterations(alt, fullAlterations);
                if (!alts.isEmpty()) {
                    alterations.addAll(alts);
                }
            }
        }

        return alterations;
    }

    private List<TumorType> getTumorTypes(String tumorType, String source) {
        List<TumorType> tumorTypes = new ArrayList<>();
        if (tumorType != null) {
            if (source == null) {
                source = "quest";
            }
            switch (source) {
                case "cbioportal":
                    tumorTypes.addAll(TumorTypeUtils.fromCbioportalTumorType(tumorType));
                    break;
                default:
                    tumorTypes.addAll(TumorTypeUtils.fromQuestTumorType(tumorType));
                    break;
            }
        }

        return tumorTypes;
    }

    private List<EvidenceQueryRes> assignEvidence(List<Evidence> evidences, List<EvidenceQueryRes> evidenceQueries) {
        for (EvidenceQueryRes query : evidenceQueries) {
            query.setEvidences(filterEvidence(evidences, query));
        }
        return evidenceQueries;
    }

    private List<Evidence> filterEvidence(List<Evidence> evidences, EvidenceQueryRes evidenceQuery) {
        List<Evidence> filtered = new ArrayList<>();

        if (evidenceQuery.getGene() != null) {
            for (Evidence evidence : evidences) {
                Evidence tempEvidence = new Evidence(evidence);
                if (tempEvidence.getGene().equals(evidenceQuery.getGene())) {
                    //Add all gene specific evidences
                    if (tempEvidence.getAlterations().isEmpty()) {
                        filtered.add(tempEvidence);
                    } else {
                        if (!CollectionUtils.intersection(tempEvidence.getAlterations(), evidenceQuery.getAlterations()).isEmpty()) {
                            if (tempEvidence.getTumorType() == null) {
                                if(tempEvidence.getEvidenceType().equals(EvidenceType.ONCOGENIC)) {
                                    if (tempEvidence.getDescription() == null) {
                                        List<Alteration> alterations = new ArrayList<>();
                                        alterations.addAll(tempEvidence.getAlterations());
                                        tempEvidence.setDescription(SummaryUtils.variantSummary(Collections.singleton(tempEvidence.getGene()), alterations, evidenceQuery.getQueryAlteration(), Collections.singleton(tempEvidence.getTumorType()), evidenceQuery.getQueryTumorType()));
                                    }
                                }
                                filtered.add(tempEvidence);
                            } else {
                                if (evidenceQuery.getTumorTypes().contains(tempEvidence.getTumorType())) {
                                    filtered.add(tempEvidence);
                                } else {
                                    if (tempEvidence.getLevelOfEvidence() != null && tempEvidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_1)) {
                                        tempEvidence.setLevelOfEvidence(LevelOfEvidence.LEVEL_2B);
                                        filtered.add(tempEvidence);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return filtered;
    }

    private List<Evidence> getEvidence(List<EvidenceQueryRes> queries, List<EvidenceType> evidenceTypes, String geneStatus) {
        List<Evidence> evidences = new ArrayList<>();
        List<EvidenceType> filteredETs = new ArrayList<>();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        Map<Integer, Gene> genes = new HashMap<>(); //Get gene evidences
        Map<Integer, Alteration> alterations = new HashMap<>();
        Map<Integer, Alteration> alterationsME = new HashMap<>(); //Mutation effect only
        Map<String, TumorType> tumorTypes = new HashMap<>();

        for (EvidenceQueryRes query : queries) {
            if (query.getGene() != null) {
                int entrezGeneId = query.getGene().getEntrezGeneId();
                if (!genes.containsKey(entrezGeneId)) {
                    genes.put(entrezGeneId, query.getGene());
                }

                for (Alteration alt : query.getAlterations()) {
                    int altId = alt.getAlterationId();

                    if (geneStatus == null || geneStatus == "") {
                        geneStatus = "all";
                    }
                    geneStatus = geneStatus.toLowerCase();
                    if (geneStatus.equals("all") || query.getGene().getStatus().toLowerCase().equals(geneStatus)) {
                        if (!alterations.containsKey(altId)) {
                            alterations.put(altId, alt);
                        }

                        for (TumorType tumorType : query.getTumorTypes()) {
                            String tumorTypeId = tumorType.getTumorTypeId();
                            if (!tumorTypes.containsKey(tumorTypeId)) {
                                tumorTypes.put(tumorTypeId, tumorType);
                            }
                        }
                    } else {
                        if (!alterationsME.containsKey(altId)) {
                            alterationsME.put(altId, alt);
                        }
                    }
                }
            }
        }

        if (evidenceTypes.contains(EvidenceType.GENE_SUMMARY)) {
            filteredETs.add(EvidenceType.GENE_SUMMARY);
        }
        if (evidenceTypes.contains(EvidenceType.GENE_BACKGROUND)) {
            filteredETs.add(EvidenceType.GENE_BACKGROUND);
        }
        evidences.addAll(evidenceBo.findEvidencesByGene(genes.values(), filteredETs));

        List<Alteration> alts = new ArrayList<>();
        alts.addAll(alterations.values());
        alts.addAll(alterationsME.values());

        if (evidenceTypes.contains(EvidenceType.MUTATION_EFFECT)) {
            filteredETs.add(EvidenceType.MUTATION_EFFECT);
            evidences.addAll(evidenceBo.findEvidencesByAlteration(alts, Collections.singleton(EvidenceType.MUTATION_EFFECT)));
        }
        if (evidenceTypes.contains(EvidenceType.ONCOGENIC)) {
            filteredETs.add(EvidenceType.ONCOGENIC);
            evidences.addAll(evidenceBo.findEvidencesByAlteration(alts, Collections.singleton(EvidenceType.ONCOGENIC)));
        }
        if (evidenceTypes.contains(EvidenceType.VUS)) {
            filteredETs.add(EvidenceType.VUS);
            evidences.addAll(evidenceBo.findEvidencesByAlteration(alts, Collections.singleton(EvidenceType.VUS)));
        }
        if (evidenceTypes.size() != filteredETs.size()) {
            //Include all level 1 evidences
            evidences.addAll(evidenceBo.findEvidencesByAlteration(new ArrayList<Alteration>(alterations.values()), Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY)));

            evidenceTypes.remove(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
            evidences.addAll(evidenceBo.findEvidencesByAlteration(new ArrayList<Alteration>(alterations.values()), evidenceTypes, tumorTypes.isEmpty() ? null : new ArrayList<TumorType>(tumorTypes.values())));
        }
        return evidences;
    }

    private List<Evidence> filterAlteration(List<Evidence> evidences, List<Alteration> alterations) {
        for (Evidence evidence : evidences) {
            Set<Alteration> filterEvidences = new HashSet<>();
            for (Alteration alt : evidence.getAlterations()) {
                if (alterations.contains(alt)) {
                    filterEvidences.add(alt);
                }
            }
            evidence.getAlterations().clear();
            evidence.setAlterations(filterEvidences);
        }

        return evidences;
    }
}