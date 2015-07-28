/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.*;

import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.mskcc.cbio.oncokb.controller.VariantAnnotationXMLController.fromCbioportalTumorType;
import static org.mskcc.cbio.oncokb.controller.VariantAnnotationXMLController.fromQuestTumorType;

/**
 *
 * @author jgao
 */
@Controller
public class EvidenceController {
    
    @RequestMapping(value="/evidence.json")
    public @ResponseBody List<List<Evidence>> getEvidence(
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

        ArrayList<Gene> genes = new ArrayList<Gene>();
        String[] alterations = null;
        String[] consequences = null;

        if (entrezGeneId!=null) {
            for (String id : entrezGeneId.split(",")) {
                Gene gene = geneBo.findGeneByEntrezGeneId(Integer.parseInt(id));
//                if(geneStatus != null && gene != null) {
//                    if(gene.getStatus().toLowerCase().equals(geneStatus.toLowerCase())){
//                        genes.add(gene);
//                    }else{
//                        genes.add(null);
//                    }
//                }else{
                    genes.add(gene);
//                }
            }
        } else if (hugoSymbol!=null) {
            for (String symbol : hugoSymbol.split(",")) {
                Gene gene = geneBo.findGeneByHugoSymbol(symbol);
//                if(geneStatus != null && gene != null) {
//                    if(gene.getStatus().toLowerCase().equals(geneStatus.toLowerCase())){
//                        genes.add(gene);
//                    }else{
//                        genes.add(null);
//                    }
//                }else{
                    genes.add(gene);
//                }
            }
        } else {
            List<Evidence> evidenceList = new ArrayList<Evidence>(evidenceBo.findAll());
            evidences.add(evidenceList);
            return evidences;
        }
        
        List<EvidenceType> evidenceTypes = null;
        if (evidenceType!=null) {
            evidenceTypes = new ArrayList<EvidenceType>();
            for (String type : evidenceType.split(",")) {
                EvidenceType et = EvidenceType.valueOf(type);
                evidenceTypes.add(et);
            }
        }

        if(alteration != null){
            alterations = alteration.split(",");
            if(genes.size() == alterations.length) {
                List<Gene> geneCopies = new ArrayList<Gene>(genes);
                Boolean consequenceLengthMatch = false;

                if(consequence != null){
                    consequences = consequence.split(",");
                    if(consequences.length == alterations.length) {
                        consequenceLengthMatch = true;
                    }
                }

                for(int i = 0 ; i < genes.size(); i++){
                    List<Evidence> evidencesDatum = new ArrayList<>();

                    if(genes.get(i) == null) {
                        evidences.add(evidencesDatum);
                    }else{
                        evidences.add(getEvidence(genes.get(i), alterations[i], tumorType, source, evidenceTypes, consequenceLengthMatch?consequences[i]:null, geneStatus));
                    }
                }

                geneCopies.removeAll(Collections.singleton(null));


                return evidences;
            }else{
                return new ArrayList<>();
            }
        }else{
            for(int i = 0 ; i < genes.size(); i++){
                List<Evidence> evidencesDatum = new ArrayList<>();

                if(genes.get(i) != null) {
                    evidencesDatum = getEvidence(genes.get(i), null, null, null, null,null, null);
                }
                evidences.add(evidencesDatum);
            }
            return evidences;
        }
    }

    private List<Evidence> getEvidence(Gene gene, String alteration, String tumorType, String tumorTypeSource, List<EvidenceType> evidenceTypes, String consequence, String geneStatus) {
        List<Evidence> evidences = new ArrayList<>();
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        if(gene != null) {
            if (evidenceTypes == null) {
                List<EvidenceType> et = new ArrayList<>();
                // add a few Strings to it
                et.add(EvidenceType.GENE_SUMMARY);
                et.add(EvidenceType.GENE_BACKGROUND);

                evidences.addAll(evidenceBo.findEvidencesByGene(Collections.singleton(gene), et));
            } else {
                evidences.addAll(evidenceBo.findEvidencesByGene(Collections.singleton(gene), evidenceTypes));
            }

            if (alteration != null) {
                AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
                List<Alteration> alterations = new ArrayList<Alteration>();
                VariantConsequence variantConsequence = null;

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

                        List<Alteration> alts = alterationBo.findRelevantAlterations(alt);
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

                    List<Alteration> alts = alterationBo.findRelevantAlterations(alt);
                    if (!alts.isEmpty()) {
                        alterations.addAll(alts);
                    }
                }

                if(geneStatus != null && !gene.getStatus().toLowerCase().equals(geneStatus.toLowerCase())) {
                    evidences.addAll(filterAlteration(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT)), alterations));
                }else{
                    if (tumorType != null && tumorTypeSource != null) {
                        Set<TumorType> relevantTumorTypes = new HashSet<>();
                        if (tumorTypeSource.equals("cbioportal")) {
                            relevantTumorTypes.addAll(fromCbioportalTumorType(tumorType));
                        } else if (tumorTypeSource.equals("quest")) {
                            relevantTumorTypes.addAll(fromQuestTumorType(tumorType));
                        }
                        if (relevantTumorTypes != null) {
                            if (evidenceTypes == null) {
                                List<Evidence> tumorTypeEvidences = new ArrayList<>();
                                tumorTypeEvidences.addAll(getTumorTypeEvidence(alterations, new ArrayList<TumorType>(relevantTumorTypes)));
                                evidences.addAll(filterAlteration(tumorTypeEvidences, alterations));
                            } else {
                                //Need to be implemented
                            }
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
                evidences.addAll(evidenceBo.findEvidencesByGene(Collections.singleton(gene)));
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

    private Set<Evidence> getTumorTypeEvidence(List<Alteration> alterations, List<TumorType> tumorTypes) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        Set<Evidence> evidences = new HashSet<Evidence>();
        Set<Evidence> evidencesFromOtherTumroTypes = new HashSet<Evidence>();

        evidencesFromOtherTumroTypes.addAll(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY)));

        for(Evidence evidence : evidencesFromOtherTumroTypes) {
            if(evidence.getLevelOfEvidence()!=null && evidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_1)){
                for(TumorType tumorType : tumorTypes) {
                    if(!evidence.getTumorType().equals(tumorType)){
                        evidences.add(evidence);
                    }
                }
            }
        }
        evidences.addAll(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT)));
        evidences.addAll(evidenceBo.findEvidencesByAlterationAndTumorTypes(alterations, tumorTypes));
        return evidences;
    }
}
