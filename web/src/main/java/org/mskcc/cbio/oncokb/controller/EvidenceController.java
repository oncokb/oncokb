/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.*;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import org.mskcc.cbio.oncokb.util.TumorTypeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author jgao
 */
@Controller
@RequestMapping(value = "/evidence.json")
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
            @RequestParam(value = "geneStatus", required = false) String geneStatus,
            @RequestParam(value = "source", required = false) String source) {

        List<List<Evidence>> evidences = new ArrayList<>();
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        List<EvidenceQuery> queries = new ArrayList<>();
        List<EvidenceType> evidenceTypes = new ArrayList<>();

        if (entrezGeneId != null) {
            for (String id : entrezGeneId.split(",")) {
                EvidenceQuery query = new EvidenceQuery();
                query.setGene(geneBo.findGeneByEntrezGeneId(Integer.parseInt(id)));
                queries.add(query);
            }
        } else if (hugoSymbol != null) {
            for (String symbol : hugoSymbol.split(",")) {
                EvidenceQuery query = new EvidenceQuery();
                query.setGene(geneBo.findGeneByHugoSymbol(symbol));
                queries.add(query);
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
            if (queries.size() == alts.length) {
                Boolean consequenceLengthMatch = false;

                if (consequence != null) {
                    consequences = consequence.split(",");
                    if (consequences.length == alts.length) {
                        consequenceLengthMatch = true;
                    }
                }

                for (int i = 0; i < queries.size(); i++) {
                    queries.get(i).setAlterations(getAlterations(queries.get(i).getGene(), alts[i], consequenceLengthMatch ? consequences[i] : null, null));
                }
                queries = assignEvidence(getEvidence(queries, evidenceTypes, geneStatus), queries);

                for(EvidenceQuery query : queries) {
                    evidences.add(query.getEvidences());
                }
                return evidences;
            } else {
                return new ArrayList<>();
            }
        } else {
            evidences.add(getEvidence(queries, evidenceTypes, geneStatus));
            return evidences;
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<EvidenceQuery> getEvidence(
            @RequestBody String body) {
        List<EvidenceQuery> result = new ArrayList<>();
        if (body != null && !body.isEmpty()) {
            JSONObject querys = new JSONObject(body);
            if (querys.has("query")) {
                JSONArray params = querys.getJSONArray("query");
                String source = null;
                String geneStatus = null;
                AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
                List<EvidenceType> evidenceTypes = new ArrayList<>();
                Map<Integer, List<Alteration>> mappedGeneAlterations = new HashMap<>();
                Map<String, List<TumorType>> mappedTumorTypes = new HashMap<>();
                Map<String, List<Alteration>> mappedAlterations = new HashMap<>();

                if (querys.has("evidenceTypes")) {
                    for (String type : querys.getString("evidenceTypes").split(",")) {
                        EvidenceType et = EvidenceType.valueOf(type);
                        evidenceTypes.add(et);
                    }
                } else {
                    evidenceTypes.add(EvidenceType.GENE_SUMMARY);
                    evidenceTypes.add(EvidenceType.GENE_BACKGROUND);
                }

                List<EvidenceQuery> evidenceQueries = new ArrayList<>();

                if (querys.has("source")) {
                    source = querys.getString("source");
                }

                if (querys.has("geneStatus")) {
                    geneStatus = querys.getString("geneStatus");
                }

                for (int i = 0; i < params.length(); i++) {
                    JSONObject pair = params.getJSONObject(i);
                    EvidenceQuery query = new EvidenceQuery();

                    if (pair.has("id")) {
                        query.setId(pair.getString("id"));
                    }
                    if (pair.has("entrezGeneId")) {
                        query.setGene(getGene(pair.getString("entrezGeneId"), null));
                    } else if (pair.has("hugoSymbol")) {
                        query.setGene(getGene(null, pair.getString("hugoSymbol")));
                    }

                    if (query.getGene() != null) {
                        if(!mappedGeneAlterations.containsKey(query.getGene().getEntrezGeneId())){
                            mappedGeneAlterations.put(query.getGene().getEntrezGeneId(), alterationBo.findAlterationsByGene(Collections.singleton(query.getGene())));
                        }
                        if (pair.has("alteration")) {
                            String consequence = null;
                            String alteration = pair.getString("alteration");
                            Gene gene = query.getGene();
                            String id = gene.getHugoSymbol() + alteration;
                            if (pair.has("consequence")) {
                                consequence = pair.getString("consequence");
                                id += consequence;
                            }
                            if(!mappedAlterations.containsKey(id)){
                                mappedAlterations.put(id, getAlterations(query.getGene(), pair.getString("alteration"), consequence, mappedGeneAlterations.get(gene.getEntrezGeneId())));
                            }
                            query.setAlterations(mappedAlterations.get(id));
                        }

                        if (pair.has("tumorType")) {
                            String tumorType = pair.getString("tumorType");
                            if(!mappedTumorTypes.containsKey(tumorType)) {
                                mappedTumorTypes.put(tumorType, getTumorTypes(pair.getString("tumorType"), source));
                            }
                            query.setTumorTypes(mappedTumorTypes.get(tumorType));
                        }

                        evidenceQueries.add(query);
                    }
                }

                result = assignEvidence(getEvidence(evidenceQueries, evidenceTypes, geneStatus), evidenceQueries);
            }
        }

        return result;
    }

    private Gene getGene(String entrezGeneId, String hugoSymbol) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Gene gene = null;

        if (entrezGeneId != null) {
            gene = geneBo.findGeneByEntrezGeneId(Integer.parseInt(entrezGeneId));
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
                    if (con.toLowerCase().contains("fusion")) {
                        alt.setAlteration("fusions");
                    } else {
                        alt.setAlteration(alteration);
                        variantConsequence = ApplicationContextSingleton.getVariantConsequenceBo().findVariantConsequenceByTerm(con);
                        alt.setConsequence(variantConsequence);
                    }
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

    private List<EvidenceQuery> assignEvidence(List<Evidence> evidences, List<EvidenceQuery> evidenceQueries) {
        for (EvidenceQuery query : evidenceQueries) {
            query.setEvidences(filterEvidence(evidences, query));
        }
        return evidenceQueries;
    }

    private List<Evidence> filterEvidence(List<Evidence> evidences, EvidenceQuery evidenceQuery) {
        List<Evidence> filtered = new ArrayList<>();

        if (evidenceQuery.getGene() != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getGene().equals(evidenceQuery.getGene())) {
                    //Add all gene specific evidences
                    if (evidence.getAlterations().isEmpty()) {
                        filtered.add(evidence);
                    } else {
                        if (!CollectionUtils.intersection(evidence.getAlterations(), evidenceQuery.getAlterations()).isEmpty()) {
                            if (evidence.getTumorType() == null) {
                                filtered.add(evidence);
                            } else {
                                if (evidenceQuery.getTumorTypes().contains(evidence.getTumorType())) {
                                    filtered.add(evidence);
                                } else {
                                    if (evidence.getLevelOfEvidence() != null && evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_1)) {
                                        evidence.setLevelOfEvidence(LevelOfEvidence.LEVEL_2B);
                                        filtered.add(evidence);
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

    private List<Evidence> getEvidence(List<EvidenceQuery> queries, List<EvidenceType> evidenceTypes, String geneStatus) {
        List<Evidence> evidences = new ArrayList<>();
        List<EvidenceType> filteredETs = new ArrayList<>();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        Map<Integer, Gene> genes = new HashMap<>(); //Get gene evidences
        Map<Integer, Alteration> alterations = new HashMap<>();
        Map<Integer, Alteration> alterationsME = new HashMap<>(); //Mutation effect only
        Map<String, TumorType> tumorTypes = new HashMap<>();

        for (EvidenceQuery query : queries) {
            if (query.getGene() != null) {
                int entrezGeneId = query.getGene().getEntrezGeneId();
                if (!genes.containsKey(entrezGeneId)) {
                    genes.put(entrezGeneId, query.getGene());
                }

                for (Alteration alt : query.getAlterations()) {
                    int altId = alt.getAlterationId();

                    if(geneStatus == null || geneStatus == "") {
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

        evidences.addAll(evidenceBo.findEvidencesByAlteration(alterationsME.values(), Collections.singleton(EvidenceType.MUTATION_EFFECT)));

        //Include all level 1 evidences
        evidences.addAll(evidenceBo.findEvidencesByAlteration(new ArrayList<Alteration>(alterations.values()), Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY)));

        evidences.addAll(evidenceBo.findEvidencesByAlteration(new ArrayList<Alteration>(alterations.values()), evidenceTypes, tumorTypes.isEmpty() ? null : new ArrayList<TumorType>(tumorTypes.values())));
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