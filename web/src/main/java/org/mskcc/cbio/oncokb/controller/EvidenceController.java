/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.*;

import com.google.api.client.repackaged.com.google.common.base.Strings;
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
 *
 * @author jgao
 */
@Controller
@RequestMapping(value="/evidence.json")
public class EvidenceController {

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody List<List<Evidence>> getEvidence(
            HttpMethod method,
            @RequestParam(value="entrezGeneId", required=false) String entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol,
            @RequestParam(value="alteration", required=false) String alteration,
            @RequestParam(value="tumorType", required=false) String tumorType,
            @RequestParam(value="evidenceType", required=false) String evidenceType,
            @RequestParam(value="consequence", required=false) String consequence,
            @RequestParam(value="geneStatus", required=false) String geneStatus,
            @RequestParam(value="source", required=false) String source) {

        List<List<Evidence>> evidences = new ArrayList<>();
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        List<EvidenceQuery> queries = new ArrayList<>();

        if (entrezGeneId!=null) {
            for (String id : entrezGeneId.split(",")) {
                EvidenceQuery query = new EvidenceQuery();
                query.setEntrezGeneId(id);
                queries.add(query);
            }
        } else if (hugoSymbol!=null) {
            for (String symbol : hugoSymbol.split(",")) {
                EvidenceQuery query = new EvidenceQuery();
                query.setHugoSymbol(symbol);
                queries.add(query);
            }
        } else {
            List<Evidence> evidenceList = new ArrayList<Evidence>(evidenceBo.findAll());
            evidences.add(evidenceList);
            return evidences;
        }

        if (evidenceType!=null) {
            for(EvidenceQuery query : queries) {
                query.setEvidenceType(evidenceType);
            }
        }

        if(alteration != null){
            String[] alts = alteration.split(",");
            String[] consequences = null;
            if(queries.size() == alts.length) {
                Boolean consequenceLengthMatch = false;

                if(consequence != null){
                    consequences = consequence.split(",");
                    if(consequences.length == alts.length) {
                        consequenceLengthMatch = true;
                    }
                }

                for(int i = 0 ; i < queries.size(); i++){
                    queries.get(i).setConsequence(consequenceLengthMatch?consequences[i]:null);
                    queries.get(i).setAlteration(alts[i]);
                    evidences.add(getEvidence(queries.get(i)));
                }

                return evidences;
            }else{
                return new ArrayList<>();
            }
        }else{
            for(EvidenceQuery query : queries){
                evidences.add(getEvidence(query));
            }
            return evidences;
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody List<EvidenceQuery> getEvidence(
            @RequestBody String body) {
        List<EvidenceQuery> result = new ArrayList<>();
        if(body != null && !body.isEmpty()){
            JSONObject querys = new JSONObject(body);
            if(querys.has("query")) {
                JSONArray params = new JSONArray(querys.getString("query"));

                for(int i = 0; i < params.length(); i++) {
                    JSONObject pair = params.getJSONObject(i);
                    EvidenceQuery query = new EvidenceQuery();

                    if(pair.has("id")) {
                        query.setId(pair.getString("id"));
                    }
                    if(pair.has("entrezGeneId")) {
                        query.setEntrezGeneId(pair.getString("entrezGeneId"));
                    }
                    if(pair.has("hugoSymbol")) {
                        query.setHugoSymbol(pair.getString("hugoSymbol"));
                    }
                    if(pair.has("alteration")) {
                        query.setAlteration(pair.getString("alteration"));
                    }
                    if(pair.has("tumorType")) {
                        query.setTumorType(pair.getString("tumorType"));
                    }
                    if(pair.has("evidenceType")) {
                        query.setEvidenceType(pair.getString("evidenceType"));
                    }
                    if(pair.has("consequence")) {
                        query.setConsequence(pair.getString("consequence"));
                    }
                    if(pair.has("geneStatus")) {
                        query.setGeneStatus(pair.getString("geneStatus"));
                    }
                    if(pair.has("source")) {
                        query.setSource(pair.getString("source"));
                    }

                    query.setEvidences(getEvidence(query));

                    result.add(query);
                }
            }
        }

        return result;
    }

    private List<Evidence> getEvidence(EvidenceQuery query) {
        List<Evidence> evidences = new ArrayList<>();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Gene gene = null;
        if (query.getEntrezGeneId()!=null) {
            gene = geneBo.findGeneByEntrezGeneId(Integer.parseInt(query.getEntrezGeneId()));
        } else if (query.getHugoSymbol()!=null) {
            gene = geneBo.findGeneByHugoSymbol(query.getHugoSymbol());
        }
        if(gene != null) {
            List<EvidenceType> evidenceTypes = null;
            if (query.getEvidenceType() != null) {
                evidenceTypes = new ArrayList<EvidenceType>();
                for (String type : query.getEvidenceType().split(",")) {
                    EvidenceType et = EvidenceType.valueOf(type);
                    evidenceTypes.add(et);
                }
            }
            if (evidenceTypes == null) {
                List<EvidenceType> et = new ArrayList<>();
                // add a few Strings to it
                et.add(EvidenceType.GENE_SUMMARY);
                et.add(EvidenceType.GENE_BACKGROUND);

                evidences.addAll(evidenceBo.findEvidencesByGene(Collections.singleton(gene), et));
            } else {
                List<EvidenceType> et = new ArrayList<>();
                if(evidenceTypes.contains(EvidenceType.GENE_SUMMARY)){
                    et.add(EvidenceType.GENE_SUMMARY);
                }
                if(evidenceTypes.contains(EvidenceType.GENE_SUMMARY)){
                    et.add(EvidenceType.GENE_BACKGROUND);
                }
                evidences.addAll(evidenceBo.findEvidencesByGene(Collections.singleton(gene), et));
            }

            if (query.getAlteration() != null) {
                AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
                List<Alteration> alterations = new ArrayList<Alteration>();
                VariantConsequence variantConsequence = null;

                if (query.getConsequence() != null) {
                    //Consequence format  a, b+c, d ... each variant pair (gene + alteration) could have one or multiple consequences. Multiple consequences are separated by '+'
                    for (String con : query.getConsequence().split("\\+")) {
                        Alteration alt = new Alteration();
                        if (con.toLowerCase().contains("fusion")) {
                            alt.setAlteration("fusions");
                        } else {
                            alt.setAlteration(query.getAlteration());
                            variantConsequence = ApplicationContextSingleton.getVariantConsequenceBo().findVariantConsequenceByTerm(con);
                            alt.setConsequence(variantConsequence);
                        }
                        alt.setAlterationType(AlterationType.MUTATION);
                        alt.setGene(gene);

                        AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                        List<Alteration> alts = alterationBo.findRelevantAlterations(alt);
                        if (!alts.isEmpty()) {
                            alterations.addAll(alts);
                        }
                    }
                } else {
                    Alteration alt = new Alteration();
                    alt.setAlteration(query.getAlteration());
                    alt.setAlterationType(AlterationType.MUTATION);
                    alt.setGene(gene);

                    AlterationUtils.annotateAlteration(alt, alt.getAlteration());

                    List<Alteration> alts = alterationBo.findRelevantAlterations(alt);
                    if (!alts.isEmpty()) {
                        alterations.addAll(alts);
                    }
                }

                //Only return mutation effect if the gene status is not matched
                if(query.getGeneStatus() != null && !gene.getStatus().toLowerCase().equals(query.getGeneStatus().toLowerCase())) {
                    evidences.addAll(filterAlteration(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT)), alterations));
                }else{
                    if (query.getTumorType() != null && query.getSource() != null) {
                        Set<TumorType> relevantTumorTypes = new HashSet<>();
                        if (query.getSource().equals("cbioportal")) {
                            relevantTumorTypes.addAll(TumorTypeUtils.fromCbioportalTumorType(query.getTumorType()));
                        } else if (query.getSource().equals("quest")) {
                            relevantTumorTypes.addAll(TumorTypeUtils.fromQuestTumorType(query.getTumorType()));
                        }
                        if (relevantTumorTypes != null) {
                            List<Evidence> tumorTypeEvidences = new ArrayList<>();
                            tumorTypeEvidences.addAll(getTumorTypeEvidence(alterations, new ArrayList<TumorType>(relevantTumorTypes), evidenceTypes));
                            evidences.addAll(filterAlteration(tumorTypeEvidences, alterations));
                        }else{
                            //If no relevant tumor type has been found, return gene summary, background and mutation effect
                            evidences.addAll(filterAlteration(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT)), alterations));
                        }
                    } else {
                        if (evidenceTypes == null) {
                            evidences.addAll(filterAlteration(evidenceBo.findEvidencesByAlteration(alterations), alterations));
                        } else {
                            evidences.addAll(filterAlteration(evidenceBo.findEvidencesByAlteration(alterations, evidenceTypes), alterations));
                        }
                    }
                }
            } else {
                if(evidenceTypes == null) {
                    evidences.addAll(evidenceBo.findEvidencesByGene(Collections.singleton(gene)));
                }else{
                    evidences.addAll(evidenceBo.findEvidencesByGene(Collections.singleton(gene), evidenceTypes));
                }
            }
        }
        return evidences;
    }

    private List<Evidence> filterAlteration(List<Evidence> evidences, List<Alteration> alterations){
        for(Evidence evidence : evidences) {
            Set<Alteration> filterEvidences = new HashSet<>();
            for(Alteration alt : evidence.getAlterations()) {
                if(alterations.contains(alt)) {
                    filterEvidences.add(alt);
                }
            }
            evidence.getAlterations().clear();
            evidence.setAlterations(filterEvidences);
        }

        return evidences;
    }

    private Set<Evidence> getTumorTypeEvidence(List<Alteration> alterations, List<TumorType> tumorTypes, List<EvidenceType> evidenceTypes) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        Set<Evidence> evidences = new HashSet<Evidence>();
        Set<Evidence> evidencesFromOtherTumroTypes = new HashSet<Evidence>();

        evidencesFromOtherTumroTypes.addAll(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY)));

        //Include all level 1 evidences from other tumor types
        for(Evidence evidence : evidencesFromOtherTumroTypes) {
            if(evidence.getLevelOfEvidence()!=null && evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_1)){
                if(!tumorTypes.contains(evidence.getTumorType())){
                    evidence.setLevelOfEvidence(LevelOfEvidence.LEVEL_2B);
                    evidences.add(evidence);
                }
            }
        }
        evidences.addAll(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT)));
        evidences.addAll(evidenceBo.findEvidencesByAlterationAndTumorTypes(alterations, tumorTypes, evidenceTypes));
        return evidences;
    }
}