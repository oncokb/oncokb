/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringEscapeUtils;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Article;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.NccnGuideline;
import org.mskcc.cbio.oncokb.model.Treatment;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.model.VariantConsequence;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author jgao
 */
@Controller
public class VariantAnnotationXMLController {
    @RequestMapping(value="/var_annotation", produces="application/xml;charset=UTF-8")//plain/text
    public @ResponseBody String getVariantAnnotation(
            @RequestParam(value="entrezGeneId", required=false) Integer entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol,
            @RequestParam(value="alterationType", required=false) String alterationType,
            @RequestParam(value="alteration", required=false) String alteration,
            @RequestParam(value="consequence", required=false) String consequence,
            @RequestParam(value="proteinStart", required=false) Integer proteinStart,
            @RequestParam(value="proteinEnd", required=false) Integer proteinEnd,
            @RequestParam(value="tumorType", required=false) String tumorType) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<xml>\n");
        
        Alteration alt = new Alteration();
        if (alteration!=null) {
            if (alteration.startsWith("p.")) {
                alteration = alteration.substring(2);
            }
            alt.setAlteration(alteration);
        }
        
        // find alteration
        if (entrezGeneId == null && hugoSymbol == null) {
            sb.append("<!-- no gene was specified --></xml>");
            return sb.toString();
        }
        
        Gene gene = null;
        if (entrezGeneId!=null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        } else if (hugoSymbol!=null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }
        
        if (gene == null) {
            sb.append("<!-- cound not find gene --></xml>");
            return sb.toString();
        }
        
        Set<TumorType> relevantTumorTypes = fromQuestTumorType(tumorType);
        
        alt.setGene(gene);
        
        AlterationType type = AlterationType.valueOf(alterationType.toUpperCase());
        if (type == null) {
            type = AlterationType.MUTATION;
        }
        alt.setAlterationType(type);
        
        VariantConsequence variantConsequence = null;
        if (consequence!=null) {
            variantConsequence = ApplicationContextSingleton.getVariantConsequenceBo().findVariantConsequenceByTerm(consequence);
            if (variantConsequence==null) {
                sb.append("<!-- could not find the specified variant consequence --></xml>");
                return sb.toString();
            }
        }
        alt.setConsequence(variantConsequence);
        
        if (proteinEnd==null) {
            proteinEnd = proteinStart;
        }
        alt.setProteinStart(proteinStart);
        alt.setProteinEnd(proteinEnd);
        
        AlterationUtils.annotateAlteration(alt, alt.getAlteration());
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        List<Alteration> alterations = alterationBo.findRelevantAlterations(alt);
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        // find all drugs
        //List<Drug> drugs = evidenceBo.findDrugsByAlterations(alterations);
        
        // find tumor types
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        List<TumorType> tumorTypes = new LinkedList<TumorType>(tumorTypeBo.findTumorTypesWithEvidencesForAlterations(alterations));
        sortTumorType(tumorTypes, tumorType);
        Set<ClinicalTrial> allTrails = new HashSet<ClinicalTrial>();
        
        // summary
        exportSummary(gene, alterations.isEmpty()?Collections.singletonList(alt):alterations, gene+" "+alteration+" mutation", relevantTumorTypes, tumorType, sb);
        
        // gene background
        List<Evidence> geneBgEvs = evidenceBo.findEvidencesByGene(Collections.singleton(gene), Collections.singleton(EvidenceType.GENE_BACKGROUND));
        if (!geneBgEvs.isEmpty()) {
            Evidence ev = geneBgEvs.get(0);
            sb.append("<gene_annotation>\n");
            sb.append("    <description>");
            sb.append(StringEscapeUtils.escapeXml(ev.getDescription()).trim());
            sb.append("</description>\n");
            exportRefereces(ev, sb, "    ");
            sb.append("</gene_annotation>\n");
        }
        
        if (alterations.isEmpty()) {
            sb.append("<!-- There is no information about the function of this variant in the MSKCC OncoKB. --></xml>");
            return sb.toString();
        }
        
        List<Evidence> mutationEffectEbs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_EFFECT));
        for (Evidence ev : mutationEffectEbs) {
            sb.append("<variant_effect>\n");
            sb.append("    <effect>");
            if (ev!=null) {
                sb.append(ev.getKnownEffect());
            }
            sb.append("</effect>\n");
            sb.append("    <description>");
            if (ev.getDescription()!=null)
                sb.append(StringEscapeUtils.escapeXml(ev.getDescription()).trim());
            sb.append("</description>\n");
            if (ev!=null) {
                exportRefereces(ev, sb, "    ");
            }

            sb.append("</variant_effect>\n");
        }
        
        for (TumorType tt : tumorTypes) {
            boolean isRelevant = relevantTumorTypes.contains(tt);
            
            StringBuilder sbTumorType = new StringBuilder();
            sbTumorType.append("<cancer_type type=\"").append(tt.getName()).append("\" relevant_to_patient_disease=\"").append(isRelevant?"Yes":"No").append("\">\n");
            int nEmp = sbTumorType.length();
            
            // find prevalence evidence blob
            Set<Evidence> prevalanceEbs = new HashSet<Evidence>(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.PREVALENCE), Collections.singleton(tt)));
            if (!prevalanceEbs.isEmpty()) {
                sbTumorType.append("    <prevalence>\n");
                sbTumorType.append("        <description>\n");
                for (Evidence ev : prevalanceEbs) {
                    sbTumorType.append("        ").append(StringEscapeUtils.escapeXml(ev.getDescription()).trim()).append("\n");
                }
                
                sbTumorType.append("</description>\n");
                for (Evidence ev : prevalanceEbs) {
                    exportRefereces(ev, sbTumorType, "        ");
                }
                sbTumorType.append("    </prevalence>\n");
            }
            
            
            // find prognostic implication evidence blob
            Set<Evidence> prognosticEbs = new HashSet<Evidence>(evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.PROGNOSTIC_IMPLICATION), Collections.singleton(tt)));
            if (!prognosticEbs.isEmpty()) {
                sbTumorType.append("    <prognostic_implications>\n");
                sbTumorType.append("        <description>\n");
                for (Evidence ev : prognosticEbs) {
                    sbTumorType.append("        ").append(StringEscapeUtils.escapeXml(ev.getDescription()).trim()).append("\n");
                }
                    sbTumorType.append("</description>\n");

                for (Evidence ev : prognosticEbs) {
                    exportRefereces(ev, sbTumorType, "        ");
                }
                sbTumorType.append("    </prognostic_implications>\n");
            }
            
            // STANDARD_THERAPEUTIC_IMPLICATIONS
            List<Evidence> stdImpEbsSensitivity = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY), Collections.singleton(tt));
            List<Evidence> stdImpEbsResisitance = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE), Collections.singleton(tt));
            exportTherapeuticImplications(relevantTumorTypes, stdImpEbsSensitivity, stdImpEbsResisitance, "standard_therapeutic_implications", sbTumorType, "    ");
            
            // NCCN_GUIDELINES
            List<Evidence> nccnEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.NCCN_GUIDELINES), Collections.singleton(tt));
            Set<NccnGuideline> nccnGuidelines = new LinkedHashSet<NccnGuideline>();
            for (Evidence ev : nccnEvs) {
                nccnGuidelines.addAll(ev.getNccnGuidelines());
            }
            
            for (NccnGuideline nccnGuideline : nccnGuidelines) {
                sbTumorType.append("    <nccn_guidelines>\n");
                sbTumorType.append("        <disease>");
                if (nccnGuideline.getDisease() != null) {
                    sbTumorType.append(nccnGuideline.getDisease());
                }
                sbTumorType.append("</disease>\n");
                sbTumorType.append("        <version>");
                if (nccnGuideline.getVersion() != null) {
                    sbTumorType.append(nccnGuideline.getVersion());
                }
                sbTumorType.append("</version>\n");
                sbTumorType.append("        <pages>");
                if (nccnGuideline.getPages() != null) {
                    sbTumorType.append(nccnGuideline.getPages());
                }
                sbTumorType.append("</pages>\n");
                sbTumorType.append("        <recommendation_category>");
                if (nccnGuideline.getCategory()!= null) {
                    sbTumorType.append(nccnGuideline.getCategory());
                }
                sbTumorType.append("</recommendation_category>\n");
                sbTumorType.append("        <description>");
                if (nccnGuideline.getDescription()!= null) {
                    sbTumorType.append(nccnGuideline.getDescription());
                }
                sbTumorType.append("</description>\n");
                sbTumorType.append("    </nccn_guidelines>\n");
            }
            
            // INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS
            List<Evidence> invImpEbsSensitivity = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY), Collections.singleton(tt));
            List<Evidence> invImpEbsResisitance = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE), Collections.singleton(tt));
            exportTherapeuticImplications(relevantTumorTypes, invImpEbsSensitivity, invImpEbsResisitance, "investigational_therapeutic_implications", sbTumorType, "    ");
            
            // CLINICAL_TRIAL
            {
//                Set<Drug> drugs = new HashSet<Drug>();
//                for (Evidence ev : stdImpEbsSensitivity) {
//                    for (Treatment treatment : ev.getTreatments()) {
//                        drugs.addAll(treatment.getDrugs());
//                    }
//                }
//                for (Evidence ev : invImpEbsSensitivity) {
//                    for (Treatment treatment : ev.getTreatments()) {
//                        drugs.addAll(treatment.getDrugs());
//                    }
//                }

                List<TumorType> tumorTypesForTrials;
                if (isRelevant) { // if relevant to pateint disease, find trials that match the tumor type
                    tumorTypesForTrials = Collections.singletonList(tt);
                } else if (relevantTumorTypes.size()==1) { // if no relevant disease, find trials that match the tumor type
                    tumorTypesForTrials = Collections.singletonList(tt);
                } else { // for irrelevant diseases, find trials that match the relavant tumor types
                    tumorTypesForTrials = null;
                }

                if (tumorTypesForTrials!=null) {
                    ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
                    List<ClinicalTrial> clinicalTrials = clinicalTrialBo.findClinicalTrialByTumorTypeAndAlteration(tumorTypesForTrials, alterations, true);
                    clinicalTrials.removeAll(allTrails); // remove duplication
                    allTrails.addAll(clinicalTrials);

                    exportClinicalTrials(clinicalTrials, sbTumorType,  "    ");
                }
            }
            
            if (sbTumorType.length()>nEmp) {
                sbTumorType.append("</cancer_type>\n");
                sb.append(sbTumorType);
            }
        }
        
        sb.append("</xml>");
        
        return sb.toString();
    }
    
    private void exportSummary(Gene gene, List<Alteration> alterations, String queryAlteration, Set<TumorType> relevantTumorTypes, String queryTumorType, StringBuilder sb) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        sb.append("<annotation_summary>");
        List<Evidence> geneSummaryEvs = evidenceBo.findEvidencesByGene(Collections.singleton(gene), Collections.singleton(EvidenceType.GENE_SUMMARY));
        if (!geneSummaryEvs.isEmpty()) {
            Evidence ev = geneSummaryEvs.get(0);
            String geneSummary = StringEscapeUtils.escapeXml(ev.getDescription()).trim();
            sb.append(geneSummary)
                    .append(" ");
        }
//            List<Evidence> cancerSummaryEvs = evidenceBo.findEvidencesByGene(gene, EvidenceType.GENE_TUMOR_TYPE_SUMMARY);
//            Map<TumorType, Evidence> mapTumorTypeSummaryEvs = new LinkedHashMap<TumorType, Evidence>();
//            for (Evidence ev : cancerSummaryEvs) {
//                mapTumorTypeSummaryEvs.put(ev.getTumorType(), ev);
//            }
//            if (!Collections.disjoint(relevantTumorTypes, mapTumorTypeSummaryEvs.entrySet())) { // if matched tumor is found
//                mapTumorTypeSummaryEvs.keySet().retainAll(relevantTumorTypes);
//            }
//            for (Evidence ev : mapTumorTypeSummaryEvs.values()) {
//                String cancerSummary = StringEscapeUtils.escapeXml(ev.getDescription()).trim();
//                sb.append(" ").append(cancerSummary);
//            }

        if (alterations.isEmpty()) {
            sb.append("The oncogenic activity of this variant is unknown. ");
        } else {
            boolean oncogenic = false;
            for (Alteration a : alterations) {
                if (a.getOncogenic()) {
                    oncogenic = true;
                    break;
                }
            }

            if (oncogenic) {
                sb.append("The ")
                        .append(queryAlteration)
                        .append(" is known to be oncogenic. ");
            } else {
                sb.append("It is not known whether the ")
                        .append(queryAlteration)
                        .append(" is oncogenic. ");
            }

            List<Evidence> evidencesResistence = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE));
            if (!evidencesResistence.isEmpty()) {
                // if resistance evidence is available in any tumor type
                sb.append("It confers resistance to ")
                        .append(treatmentsToString(evidencesResistence, null, null))
                        .append(" ");
            }

            Set<EvidenceType> sensitivityEvidenceTypes = 
                    EnumSet.of(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY,
                            EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
            Map<LevelOfEvidence, List<Evidence>> evidencesByLevel = groupEvidencesByLevel(
                    evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes, relevantTumorTypes)
            );
            if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                // if there are FDA approved drugs in the patient tumor type with the variant
                sb.append("There are FDA approved drugs ")
                        .append(treatmentsToStringbyTumorType(evidencesByLevel.get(LevelOfEvidence.LEVEL_1), queryAlteration))
                        .append(". ");
            } else if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                // if there are NCCN guidelines in the patient tumor type with the variant
//                Map<LevelOfEvidence, List<Evidence>> otherEvidencesByLevel = groupEvidencesByLevel(
//                        evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes)
//                );
//                if (!otherEvidencesByLevel.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
//                    // FDA approved drugs in other tumor type with the variant
//                    sb.append("There are FDA approved drugs ")
//                        .append(treatmentsToStringbyTumorType(otherEvidencesByLevel.get(LevelOfEvidence.LEVEL_1), queryAlteration))
//                        .append(". ");
//                }
                sb.append("NCCN guidelines recommend ")
                        .append(treatmentsToStringbyTumorType(evidencesByLevel.get(LevelOfEvidence.LEVEL_2A), queryAlteration))
                        .append(". ");
            } else {
                // no FDA or NCCN in the patient tumor type with the variant
                Map<LevelOfEvidence, List<Evidence>> evidencesByLevelOtherTumorType = groupEvidencesByLevel(
                        evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes)
                );
                if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                    // if there are FDA approved drugs in other tumor types with the variant
                    sb.append("While there are FDA approved drugs ")
                            .append(treatmentsToStringbyTumorType(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_1), queryAlteration))
                            .append(", ")
                            .append(" the clinical utility in ")
                            .append(queryTumorType==null?"other":queryTumorType)
                            .append(" patients with ")
                            .append(queryAlteration)
                            .append(" is not known. ");
                } else if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                    // if there are NCCN drugs in other tumor types with the variant
                    sb.append("While NCCN recommend drugs ")
                            .append(treatmentsToStringbyTumorType(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_2A), queryAlteration))
                            .append(", ")
                            .append(" the clinical utility in ")
                            .append(queryTumorType==null?"other":queryTumorType)
                            .append(" patients with ")
                            .append(queryAlteration)
                            .append(" is not known. ");
                } else {
                    // no FDA or NCCN drugs for the variant in any tumor type
                    Map<LevelOfEvidence, List<Evidence>> evidencesByLevelGene = groupEvidencesByLevel(
                            evidenceBo.findEvidencesByGene(Collections.singleton(gene), sensitivityEvidenceTypes)
                    );
                    if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                        // if there are FDA approved drugs for different variants in the same gene (either same tumor type or different ones) .. e.g. BRAF K601E 
                        sb.append("While there are FDA approved drugs ")
                                .append(treatmentsToStringbyTumorType(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_1), null))
                                .append(", ")
                                .append(" the clinical utility for patients with ")
                                .append(queryAlteration)
                                .append(" is not known. ");
                    } else if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                        // if there are NCCN drugs for different variants in the same gene (either same tumor type or different ones) .. e.g. BRAF K601E 
                        sb.append("While NCCN recommend ")
                                .append(treatmentsToStringbyTumorType(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_2A), null))
                                .append(", ")
                                .append(" the clinical utility for patients with ")
                                .append(queryAlteration)
                                .append(" is not known. ");
                    } else {
                        // if there is no FDA or NCCN drugs for the gene at all
                        sb.append("There are no FDA approved or NCCN recommended treatments specifically for ")
                                .append(queryTumorType)
                                .append(" patients with ")
                                .append(queryAlteration)
                                .append(". ");
                    }
                }
                    
                sb.append("Please refer to the clinical trials section. ");
            }
        }

        sb.append("</annotation_summary>\n");
    }
    
    private void exportTherapeuticImplications(Set<TumorType> relevantTumorTypes, List<Evidence> evSensitivity, List<Evidence> evResisitance, String tagTherapeuticImp, StringBuilder sb, String indent) {
        if (evSensitivity.isEmpty() && evResisitance.isEmpty()) {
            return;
        }
        
        sb.append(indent).append("<").append(tagTherapeuticImp).append(">\n");
        
        List<List<Evidence>> evsSensitivity = seperateGeneralAndSpecificEvidencesForTherapeuticImplications(evSensitivity);
        List<List<Evidence>> evsResisitance = seperateGeneralAndSpecificEvidencesForTherapeuticImplications(evResisitance);
        
        // general evs
        if (!evsSensitivity.get(0).isEmpty() || !evsResisitance.get(0).isEmpty()) {
            sb.append(indent).append("    <general_statement>\n");
            for (Evidence ev : evsSensitivity.get(0)) {
                sb.append(indent).append("        <sensitivity>\n");
                exportTherapeuticImplications(null, ev, sb, indent+"            ");
                sb.append(indent).append("        </sensitivity>\n");
            }
            for (Evidence ev : evsResisitance.get(0)) {
                sb.append(indent).append("        <resistance>\n");
                exportTherapeuticImplications(null, ev, sb, indent+"            ");
                sb.append(indent).append("        </resistance>\n");
            }
            sb.append(indent).append("    </general_statement>\n");
        }
        
        // specific evs
        //boolean isInvestigational = tagTherapeuticImp.equals("investigational_therapeutic_implications");
        if (!evsSensitivity.get(1).isEmpty() || !evsResisitance.get(1).isEmpty()) {
            for (Evidence ev : evsSensitivity.get(1)) {
                sb.append(indent).append("    <sensitive_to>\n");
                exportTherapeuticImplications(relevantTumorTypes, ev, sb, indent+"        ");
                sb.append(indent).append("    </sensitive_to>\n");
            }
            for (Evidence ev : evsResisitance.get(1)) {
                sb.append(indent).append("    <resistant_to>\n");
                exportTherapeuticImplications(relevantTumorTypes, ev, sb, indent+"        ");
                sb.append(indent).append("    </resistant_to>\n");
            }
        }

        sb.append(indent).append("</").append(tagTherapeuticImp).append(">\n");
    }
    
    private List<List<Evidence>> seperateGeneralAndSpecificEvidencesForTherapeuticImplications (List<Evidence> evs) {
        List<List<Evidence>> ret = new ArrayList<List<Evidence>>();
        ret.add(new ArrayList<Evidence>());
        ret.add(new ArrayList<Evidence>());
        
        for (Evidence ev : evs) {
            if (ev.getTreatments().isEmpty()) {
                ret.get(0).add(ev);
            } else {
                ret.get(1).add(ev);
            }
        }
        
        return ret;
    }
    
    private void exportClinicalTrials(List<ClinicalTrial> clinicalTrials, StringBuilder sb, String indent) {
        Collections.sort(clinicalTrials, new Comparator<ClinicalTrial>() {
            public int compare(ClinicalTrial trial1, ClinicalTrial trial2) {
                return phase2int(trial2.getPhase()) - phase2int(trial1.getPhase());
            }
            
            private int phase2int(String phase) {
                if (phase.matches("Phase [0-4]")) {
                    return 2*Integer.parseInt(phase.substring(6));
                }
                if (phase.matches("Phase [0-4]/Phase [0-4]")) {
                    return Integer.parseInt(phase.substring(6,7)) + Integer.parseInt(phase.substring(14));
                }
                return -1;
            }
        });
        
        for (ClinicalTrial clinicalTrial : clinicalTrials) {
            if (filterClinicalTrials(clinicalTrial)) {
                exportClinicalTrial(clinicalTrial, sb, indent);
            }
        }
    }
    
    private boolean filterClinicalTrials(ClinicalTrial clinicalTrial) {
//        if (!clinicalTrial.isInUSA()) {
//            return false;
//        }
//        
//        if (!clinicalTrial.isOpen()) {
//            return false;
//        }
//        
//        String phase = clinicalTrial.getPhase().toLowerCase();
//        return phase.contains("phase 1") || 
//                phase.contains("phase 2") || 
//                phase.contains("phase 3") || 
//                phase.contains("phase 4") || 
//                phase.contains("phase 5");
        
        return true;
    }
    
    private void exportClinicalTrial(ClinicalTrial trial, StringBuilder sb, String indent) {
        sb.append(indent).append("<clinical_trial>\n");

        sb.append(indent).append("    <trial_id>");
        if (trial.getNctId() != null) {
            sb.append(trial.getNctId());
        }
        sb.append("</trial_id>\n");

        sb.append(indent).append("    <title>");
        if (trial.getTitle() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getTitle()));
        }
        sb.append("</title>\n");

        sb.append(indent).append("    <purpose>");
        if (trial.getPurpose() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getPurpose()));
        }
        sb.append("</purpose>\n");

        sb.append(indent).append("    <recruiting_status>");
        if (trial.getRecruitingStatus() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getRecruitingStatus()));
        }
        sb.append("</recruiting_status>\n");

        sb.append(indent).append("    <eligibility_criteria>");
        if (trial.getEligibilityCriteria() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getEligibilityCriteria()));
        }
        sb.append("</eligibility_criteria>\n");

        sb.append(indent).append("    <phase>");
        if (trial.getPhase() != null) {
            sb.append(StringEscapeUtils.escapeXml(trial.getPhase()));
        }
        sb.append("</phase>\n");
        
        for (Alteration alteration : trial.getAlterations()) {
            sb.append(indent).append("    <biomarker>");
            sb.append(StringEscapeUtils.escapeXml(alteration.toString()));
            sb.append("</biomarker>\n");
        }
        
        for (Drug drug : trial.getDrugs()) {
            sb.append(indent).append("    <intervention>");
            sb.append(StringEscapeUtils.escapeXml(drug.getDrugName()));
            sb.append("</intervention>\n");
        }
        
        for (TumorType tumorType : trial.getTumorTypes()) {
            sb.append(indent).append("    <condition>");
            sb.append(StringEscapeUtils.escapeXml(tumorType.getName()));
            sb.append("</condition>\n");
        }

        sb.append(indent).append("</clinical_trial>\n");
    }
    
    private void exportTherapeuticImplications(Set<TumorType> relevantTumorTypes, Evidence evidence, StringBuilder sb, String indent) {
        for (Treatment treatment : evidence.getTreatments()) {
            sb.append(indent).append("<treatment>\n");
            exportTreatment(treatment, sb, indent+"    ");
            sb.append(indent).append("</treatment>\n");
        }
        
        LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();
        if (levelOfEvidence!=null) {
            if (levelOfEvidence==LevelOfEvidence.LEVEL_1 &&
                    !relevantTumorTypes.contains(evidence.getTumorType())) {
                levelOfEvidence = LevelOfEvidence.LEVEL_2B;
            }
            sb.append(indent).append("<level_of_evidence_for_patient_indication>\n");
            sb.append(indent).append("    <level>");
            sb.append(levelOfEvidence.getLevel());
            sb.append("</level>\n");
            sb.append(indent).append("    <description>");
            sb.append(StringEscapeUtils.escapeXml(levelOfEvidence.getDescription()).trim());
            sb.append("</description>\n");
            if (levelOfEvidence==LevelOfEvidence.LEVEL_1 || 
                    levelOfEvidence==LevelOfEvidence.LEVEL_2A || 
                    levelOfEvidence==LevelOfEvidence.LEVEL_2B) {
                sb.append(indent).append("<approved_indication>");
                sb.append("</approved_indication>\n");
            }
            sb.append(indent).append("</level_of_evidence_for_patient_indication>\n");
        }
        
        sb.append(indent).append("<description>");
        if (evidence.getDescription()!=null)
            sb.append(StringEscapeUtils.escapeXml(evidence.getDescription()).trim());
        sb.append("</description>\n");
        
        exportRefereces(evidence, sb, indent);
    }
    
    private void exportTreatment(Treatment treatment, StringBuilder sb, String indent) {
        Set<Drug> drugs = treatment.getDrugs();
        for (Drug drug : drugs) {
            sb.append(indent).append("<drug>\n");
            
            sb.append(indent).append("    <name>");
            String name = drug.getDrugName();
            if (name != null) {
                sb.append(StringEscapeUtils.escapeXml(name));
            }
            sb.append("</name>\n");
            
            Set<String> synonyms = drug.getSynonyms();
            for (String synonym : synonyms) {
                sb.append(indent).append("    <synonym>");
                sb.append(synonym);
                sb.append("</synonym>\n");
            }
            
            sb.append(indent).append("    <fda_approved>");
            Boolean fdaApproved = drug.isFdaApproved();
            if (fdaApproved!=null) {
                sb.append(fdaApproved ? "Yes" : "No");
            }
            sb.append("</fda_approved>\n");
            
//            sb.append(indent).append("    <description>");
//            String desc = drug.getDescription();
//            if (desc != null) {
//                sb.append(StringEscapeUtils.escapeXml(desc));
//            }
//            sb.append("</description>\n");
            
            sb.append(indent).append("</drug>\n");
        
        }
    }
    
    private void exportRefereces(Evidence evidence, StringBuilder sb, String indent) {
        Set<Article> articles = evidence.getArticles();
        for (Article article : articles) {
            sb.append(indent).append("<reference>\n");
            sb.append(indent).append("    <pmid>");
            String pmid = article.getPmid();
            if (pmid != null) {
                sb.append(pmid);
            }
            sb.append("</pmid>\n");
            
            sb.append(indent).append("    <authors>");
            if (article.getAuthors()!=null) {
                sb.append(article.getAuthors());
            }
            sb.append("</authors>\n");
            
            sb.append(indent).append("    <title>");
            if (article.getTitle()!=null) {
                sb.append(article.getTitle());
            }
            sb.append("</title>\n");
            
            sb.append(indent).append("    <journal>");
            if (article.getJournal()!=null) {
                sb.append(article.getJournal());
            }
            sb.append("</journal>\n");
            
            sb.append(indent).append("    <pub_date>");
            if (article.getPubDate()!=null) {
                sb.append(article.getPubDate());
            }
            sb.append("</pub_date>\n");
            
            sb.append(indent).append("    <volume>");
            if (article.getVolume()!=null) {
                sb.append(article.getVolume());
            }
            sb.append("</volume>\n");
            
            sb.append(indent).append("    <issue>");
            if (article.getIssue()!=null) {
                sb.append(article.getIssue());
            }
            sb.append("</issue>\n");
            
            sb.append(indent).append("    <pages>");
            if (article.getPages()!=null) {
                sb.append(article.getPages());
            }
            sb.append("</pages>\n");
            
            sb.append(indent).append("    <elocation_id>");
            if (article.getElocationId()!=null) {
                sb.append(article.getElocationId());
            }
            sb.append("</elocation_id>\n");
            
            sb.append(indent).append("</reference>\n");
        }
    }
    
    private Map<LevelOfEvidence, List<Evidence>> groupEvidencesByLevel(List<Evidence> evidences) {
        Map<LevelOfEvidence, List<Evidence>> map = new EnumMap<LevelOfEvidence, List<Evidence>>(LevelOfEvidence.class);
        for (LevelOfEvidence level : LevelOfEvidence.values()) {
            map.put(level, new ArrayList<Evidence>());
        }
        for (Evidence ev : evidences) {
            if (ev.getLevelOfEvidence()==null || ev.getTreatments().isEmpty()) continue;
            map.get(ev.getLevelOfEvidence()).add(ev);
        }
        return map;
    }
    
    private String treatmentsToStringbyTumorType(List<Evidence> evidences, String alteration) {
        Map<String, Set<Evidence>> map = new TreeMap<String, Set<Evidence>>();
        for (Evidence ev : evidences) {
            String tt = ev.getTumorType().getName();
            Set<Evidence> set = map.get(tt);
            if (set==null) {
                set = new HashSet<Evidence>();
                map.put(tt, set);
            }
            set.add(ev);
        }
        
        List<String> list = new ArrayList<String>();
        for (Map.Entry<String, Set<Evidence>> entry : map.entrySet()) {
            String tt = entry.getKey();
            Set<Evidence> evs = entry.getValue();
            list.add(treatmentsToString(evs, tt, alteration));
        }
        
        return listToString(list);
    }
    
    private String treatmentsToString(Collection<Evidence> evidences, String tumorType, String alteration) {
        Set<String> drugs = new TreeSet<String>();
        Set<Alteration> alterations = new LinkedHashSet<Alteration>();
        for (Evidence ev : evidences) {
            for (Treatment t : ev.getTreatments()) {
                for (Drug drug : t.getDrugs()) {
                    drugs.add(drug.getDrugName().toLowerCase());
                }
            }
            alterations.addAll(ev.getAlterations());
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(listToString(new ArrayList<String>(drugs)));
        
        sb.append(" for ")
                .append(tumorType==null?"":(tumorType+" "))
                .append("patients with ")
                .append(alteration==null?(alterations.size()>2?"specific mutations":alterationsToString(alterations)):alteration);
        
        String ret = sb.toString();
        String retLow = ret.toLowerCase();
        if (retLow.endsWith("mutation")||retLow.endsWith("mutations")) {
            return ret;
        }
        
        return ret + " mutations";
    }
    
    private String listToString(List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        
        int n = list.size();
        StringBuilder sb = new StringBuilder();
        sb.append(list.get(0));
        if (n==1) {
            return sb.toString();
        }
        
        for (int i=1; i<n-1; i++) {
            sb.append(", ").append(list.get(i));
        }
        
        sb.append(" and ").append(list.get(n-1));
        
        return sb.toString();
    }
    
    private String alterationsToString(Collection<Alteration> alterations) {
        Map<String,Set<String>> mapGeneVariants = new TreeMap<String,Set<String>>();
        for (Alteration alteration : alterations) {
            String gene = alteration.getGene().getHugoSymbol();
            Set<String> variants = mapGeneVariants.get(gene);
            if (variants==null) {
                variants = new TreeSet<String>();
                mapGeneVariants.put(gene, variants);
            }
            variants.add(alteration.getName());
        }
        
        List<String> list = new ArrayList<String>();
        for (Map.Entry<String,Set<String>> entry : mapGeneVariants.entrySet()) {
            list.add(entry.getKey()+" "+listToString(new ArrayList<String>(entry.getValue())));
        }
        
        String gene = alterations.iterator().next().getGene().getHugoSymbol();
        
        String ret = listToString(list);
        
        if(!ret.startsWith(gene)) {
            ret =  gene + " " + ret;
        }
        
        String retLow = ret.toLowerCase();
        if (retLow.endsWith("mutation")||retLow.endsWith("mutations")) {
            return ret;
        }
        
        return ret + " mutation" + (list.size()>1?"s":"");
    }
    /**
     * 
     * @param tumorTypes
     * @param patientTumorType
     * @return the number of relevant tumor types
     */
    private void sortTumorType(List<TumorType> tumorTypes, String patientTumorType) {
        Set<TumorType> relevantTumorTypes = fromQuestTumorType(patientTumorType);
//        relevantTumorTypes.retainAll(tumorTypes); // only tumor type with evidence
        tumorTypes.removeAll(relevantTumorTypes); // other tumor types
        tumorTypes.addAll(0, relevantTumorTypes);
    }
    
//    private static Set<Drug> allTargetedDrugs = new HashSet<Drug>();
//    static {
//        TreatmentBo treatmentBo = ApplicationContextSingleton.getTreatmentBo();
//        for (Treatment treatment : treatmentBo.findAll()) {
//            allTargetedDrugs.addAll(treatment.getDrugs());
//        }
//    }
    
    private static final String TUMOR_TYPE_ALL_TUMORS = "all tumors";
    private static Map<String, List<TumorType>> questTumorTypeMap = null;
    private static Set<TumorType> fromQuestTumorType(String questTumorType) {
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        if (questTumorTypeMap==null) {
            questTumorTypeMap = new HashMap<String, List<TumorType>>();
            
            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);
            
            List<String> lines;
            try {
                lines = FileUtils.readTrimedLinesStream(
                VariantAnnotationXMLController.class.getResourceAsStream("/data/quest-tumor-types.txt"));
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.singleton(tumorTypeAll);
            }
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                String questType = parts[0].toLowerCase();
                TumorType oncokbType = tumorTypeBo.findTumorTypeByName(parts[1]);
                if (oncokbType==null) {
                    System.err.println("no "+parts[1]+" as tumor type in oncokb");
                    continue;
                }
                
                List<TumorType> types = questTumorTypeMap.get(questType);
                if (types==null) {
                    types = new LinkedList<TumorType>();
                    questTumorTypeMap.put(questType, types);
                }
                types.add(oncokbType);
            }
            
            for (List<TumorType> list : questTumorTypeMap.values()) {
                list.add(tumorTypeAll);
            }
        }
        
        questTumorType = questTumorType==null ? null : questTumorType.toLowerCase();
        
        List<TumorType> ret = questTumorTypeMap.get(questTumorType);
        if (ret == null) {
            System.out.print("not in our mapping file");
            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);
            ret = new LinkedList<TumorType>();
            ret.add(tumorTypeAll);
        }
        
        TumorType extactMatchedTumorType = tumorTypeBo.findTumorTypeByName(questTumorType);
        if(extactMatchedTumorType!=null && !ret.contains(extactMatchedTumorType)) {
            ret.add(0, extactMatchedTumorType);
        }
        
        return new LinkedHashSet<TumorType>(ret);
    }
}
