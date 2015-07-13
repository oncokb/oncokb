/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.controller;

import java.io.IOException;
import java.util.*;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.StringEscapeUtils;
import org.mskcc.cbio.oncokb.bo.*;
import org.mskcc.cbio.oncokb.model.*;
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

        //Gene + mutation name
        String variantName = "";

        if(alteration.toLowerCase().contains(gene.getHugoSymbol().toLowerCase())) {
            variantName = alteration;
        }else {
            variantName = gene+" "+alteration;
        }

        if(alteration.toLowerCase().contains("fusion")){
//            variantName = variantName.concat(" event");
        }else if(alteration.toLowerCase().contains("deletion") || alteration.toLowerCase().contains("amplification")){
            //Keep the variant name
        }else{
            variantName = variantName.concat(" mutation");
        }

        // summary
        exportSummary(gene, alterations.isEmpty()?Collections.singletonList(alt):alterations, variantName, relevantTumorTypes, tumorType, sb);

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

            //Remove level_R3
            stdImpEbsResisitance = filterResistanceEvidence(stdImpEbsResisitance);

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
                    sbTumorType.append(StringEscapeUtils.escapeXml(nccnGuideline.getDescription()));
                }
                sbTumorType.append("</description>\n");
                sbTumorType.append("    </nccn_guidelines>\n");
            }

            // INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS
            List<Evidence> invImpEbsSensitivity = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY), Collections.singleton(tt));
            List<Evidence> invImpEbsResisitance = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE), Collections.singleton(tt));

            //Remove level_R3
            invImpEbsResisitance = filterResistanceEvidence(invImpEbsResisitance);

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
                    ClinicalTrialMappingBo clinicalTrialMappingBo = ApplicationContextSingleton.getClinicalTrialMappingBo();
                    Set<ClinicalTrialMapping> mappings = clinicalTrialMappingBo.findMappingByAlterationTumorType(alterations, tumorTypesForTrials);;
                    List<ClinicalTrial> clinicalTrials = clinicalTrialBo.findClinicalTrialByMapping(mappings, true);
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

    private List<Evidence> filterResistanceEvidence(List<Evidence> resistanceEvidences){
        if(resistanceEvidences != null) {
            Iterator<Evidence> i = resistanceEvidences.iterator();
            while (i.hasNext()) {
                Evidence resistanceEvidence = i.next(); // must be called before you can call i.remove()
                if (resistanceEvidence.getLevelOfEvidence() != null && resistanceEvidence.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_R3)) {
                    i.remove();
                }
            }
        }
        return resistanceEvidences;
    }

    private void exportSummary(Gene gene, List<Alteration> alterations, String queryAlteration, Set<TumorType> relevantTumorTypes, String queryTumorType, StringBuilder sb) {
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();

        queryTumorType = queryTumorType.toLowerCase();

        Boolean appendThe = true;
        Boolean isPlural = false;

        if(queryAlteration.toLowerCase().contains("deletion") || queryAlteration.toLowerCase().contains("amplification") || queryAlteration.toLowerCase().contains("fusion") ){
            appendThe = false;
        }

        if(queryAlteration.toLowerCase().contains("fusions")) {
            isPlural = true;
        }

        sb.append("<annotation_summary>");
        List<Evidence> geneSummaryEvs = evidenceBo.findEvidencesByGene(Collections.singleton(gene), Collections.singleton(EvidenceType.GENE_SUMMARY));
        if (!geneSummaryEvs.isEmpty()) {
            Evidence ev = geneSummaryEvs.get(0);
            String geneSummary = StringEscapeUtils.escapeXml(ev.getDescription()).trim();
            sb.append(geneSummary)
                    .append(" ");
        }

        //Mutation summary
        List<Evidence> mutationSummaryEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.MUTATION_SUMMARY));
        if (!mutationSummaryEvs.isEmpty()) {
            Evidence ev = mutationSummaryEvs.get(0);
            String mutationSummary = StringEscapeUtils.escapeXml(ev.getDescription()).trim();
            sb.append(mutationSummary)
                    .append(" ");
        }

        //Tumor type summary
        List<Evidence> tumorTypeSummaryEvs = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.TUMOR_TYPE_SUMMARY), relevantTumorTypes);
        if (!tumorTypeSummaryEvs.isEmpty()) {
            Evidence ev = tumorTypeSummaryEvs.get(0);
            String tumorTypeSummary = StringEscapeUtils.escapeXml(ev.getDescription()).trim();
            sb.append(tumorTypeSummary)
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
            int oncogenic = -1;
            for (Alteration a : alterations) {
                if (a.getOncogenic() > 0) {
                    oncogenic = a.getOncogenic();
                    break;
                }
            }

            if (oncogenic > 0) {
                if(appendThe){
                    sb.append("The ");
                }
                sb.append(queryAlteration);

                if(isPlural){
                    sb.append(" are");
                }else{
                    sb.append(" is");
                }

                if(oncogenic == 2) {
                    sb.append(" likely");
                }else if (oncogenic == 1){
                    sb.append(" known");
                }

                sb.append(" to be oncogenic. ");
            } else {
                sb.append("It is not known whether ");
                if(appendThe){
                    sb.append("the ");
                }

                sb.append(queryAlteration);

                if(isPlural){
                    sb.append(" are");
                }else{
                    sb.append(" is");
                }
                sb.append(" oncogenic. ");
            }

//            List<Evidence> evidencesResistence = evidenceBo.findEvidencesByAlteration(alterations, Collections.singleton(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE));
//            if (!evidencesResistence.isEmpty()) {
//                // if resistance evidence is available in any tumor type
//                sb.append("It confers resistance to ")
//                        .append(treatmentsToString(evidencesResistence, null, null, false, false, false))
//                        .append(" ");
//            }

            Set<EvidenceType> sensitivityEvidenceTypes =
                    EnumSet.of(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY,
                            EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
            Map<LevelOfEvidence, List<Evidence>> evidencesByLevel = groupEvidencesByLevel(
                    evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes, relevantTumorTypes)
            );
            List<Evidence> evidences = new ArrayList<>();
            if(!evidencesByLevel.get(LevelOfEvidence.LEVEL_0).isEmpty()){
                evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_0));
            }
            if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                // if there are FDA approved drugs in the patient tumor type with the variant
                evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_1));
                sb.append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType, true, true, false, false))
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
                evidences.addAll(evidencesByLevel.get(LevelOfEvidence.LEVEL_2A));
                sb.append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType, true, false, true, false))
                        .append(". ");
            } else if (!evidencesByLevel.get(LevelOfEvidence.LEVEL_0).isEmpty()) {
                sb.append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType, true, true, false, false))
                        .append(". ");
            } else {
                // no FDA or NCCN in the patient tumor type with the variant
                Map<LevelOfEvidence, List<Evidence>> evidencesByLevelOtherTumorType = groupEvidencesByLevel(
                        evidenceBo.findEvidencesByAlteration(alterations, sensitivityEvidenceTypes)
                );
                evidences.clear();
                if(!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_0).isEmpty()){
                    evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_0));
                }

                if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                    // if there are FDA approved drugs in other tumor types with the variant
                    evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_1));
                    sb.append("While ")
                            .append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType,false, true, false, true))
                            .append(", the clinical utility for patients with ")
                            .append(queryTumorType == null ? "" : " " + queryTumorType)
                            .append(" harboring the " + queryAlteration)
                            .append(" is not known. ");
                } else if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                    // if there are NCCN drugs in other tumor types with the variant
                    evidences.addAll(evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_2A));
                    sb.append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType,true, false, true, true))
                            .append(", the clinical utility for patients with ")
                            .append(queryTumorType == null ? "" : " " + queryTumorType)
                            .append(" harboring the " + queryAlteration)
                            .append(" is not known. ");
                } else if (!evidencesByLevelOtherTumorType.get(LevelOfEvidence.LEVEL_0).isEmpty()) {
                    // if there are NCCN drugs in other tumor types with the variant0
                    sb.append("While ")
                            .append(treatmentsToStringByTumorType(evidences, queryAlteration, queryTumorType,false, true, false, true))
                            .append(", the clinical utility for patients with ")
                            .append(queryTumorType == null ? "" : " " + queryTumorType)
                            .append(" harboring the " + queryAlteration)
                            .append(" is not known. ");
                }else {
                    // no FDA or NCCN drugs for the variant in any tumor type
                    Map<LevelOfEvidence, List<Evidence>> evidencesByLevelGene = groupEvidencesByLevel(
                            evidenceBo.findEvidencesByGene(Collections.singleton(gene), sensitivityEvidenceTypes)
                    );
                    evidences.clear();
                    if(!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_0).isEmpty()){
                        evidences.addAll(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_0));
                    }
                    if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_1).isEmpty()) {
                        // if there are FDA approved drugs for different variants in the same gene (either same tumor type or different ones) .. e.g. BRAF K601E 
                        evidences.addAll(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_1));
                        sb.append("While ")
                                .append(treatmentsToStringByTumorType(evidences, null, queryTumorType, false, true, false, true))
                                .append(", the clinical utility for patients with ")
                                .append(queryTumorType == null ? "" : " " + queryTumorType)
                                .append(" harboring the " + queryAlteration)
                                .append(" is not known. ");
                    } else if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_2A).isEmpty()) {
                        // if there are NCCN drugs for different variants in the same gene (either same tumor type or different ones) .. e.g. BRAF K601E 
                        evidences.addAll(evidencesByLevelGene.get(LevelOfEvidence.LEVEL_1));
                        sb.append(treatmentsToStringByTumorType(evidences, null, queryTumorType, true, false, true, true))
                                .append(", the clinical utility for patients with ")
                                .append(queryTumorType == null ? "" : " " + queryTumorType)
                                .append(" harboring the " + queryAlteration)
                                .append(" is not known. ");
                    } else if (!evidencesByLevelGene.get(LevelOfEvidence.LEVEL_0).isEmpty()) {
                        // if there are NCCN drugs for different variants in the same gene (either same tumor type or different ones) .. e.g. BRAF K601E
                        sb.append("While ")
                                .append(treatmentsToStringByTumorType(evidences, null, queryTumorType, false, true, false, true))
                                .append(", the clinical utility for patients with ")
                                .append(queryTumorType == null ? "" : " " + queryTumorType)
                                .append(" harboring the " + queryAlteration)
                                .append(" is not known. ");
                    } else {
                        // if there is no FDA or NCCN drugs for the gene at all
                        sb.append("There are no FDA-approved or NCCN-compendium listed treatments specifically for patients with ")
                                .append(queryTumorType)
                                .append(" harboring ");
                        if(appendThe){
                            sb.append("the ");
                        }
                        sb.append(queryAlteration)
                                .append(". ");
                    }
                }

//                sb.append("Please refer to the clinical trials section. ");
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
                    return 2 * Integer.parseInt(phase.substring(6));
                }
                if (phase.matches("Phase [0-4]/Phase [0-4]")) {
                    return Integer.parseInt(phase.substring(6, 7)) + Integer.parseInt(phase.substring(14));
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
        LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();

        for (Treatment treatment : evidence.getTreatments()) {
            sb.append(indent).append("<treatment>\n");
            exportTreatment(treatment, sb, indent+"    ", levelOfEvidence);
            sb.append(indent).append("</treatment>\n");
        }

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
    
    private void exportTreatment(Treatment treatment, StringBuilder sb, String indent, LevelOfEvidence levelOfEvidence) {
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

            //FDA approved info based on evidence level. Temporaty solution. The info should be pulled up from database
            //by using PI-helper
//            Boolean fdaApproved = drug.isFdaApproved();
//            if (fdaApproved!=null) {
//                sb.append(fdaApproved ? "Yes" : "No");
//            }

            Boolean fdaApproved = levelOfEvidence == LevelOfEvidence.LEVEL_1 || levelOfEvidence == LevelOfEvidence.LEVEL_2A;
            sb.append(fdaApproved ? "Yes" : "No");

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

    //According to following ruls
//    IF ≤2 SAME drug for ≤2 different cancer types
//            include
//
//    e.g. While the drugs dabrafenib, trametinib and vemurafenib are FDA-approved for patients with BRAF V600E mutant melanoma, bladder or breast cancer, the clinical utility for these agents in patients with BRAF V600E mutant low grade gliomas is not known.
//
//    IF >2 SAME drug for >2 different cancer types
//            include
//
//    While there are FDA-approved drugs for patients with specific cancers harboring the BRAF V600E mutation (please refer to FDA-approved drugs in Other Tumor types section), the clinical utility for these agents in patients with BRAF V600E mutant low grade gliomas is not known.
//
//    IF <2 DIFFERENT drugs for <2 different tumor types
//
//    While there are FDA-approved drugs for patients with lung and colorectal cancers harboring the EGFR L858R mutation (please refer to FDA-approved drugs in Other Tumor types section), the clinical utility for these agents in patients with EGFR L858R mutant low grade gliomas is not known.
    private String treatmentsToStringByTumorType(List<Evidence> evidences, String queryAlteration, String queryTumorType, boolean capFirstLetter, boolean fda, boolean nccn, boolean inOtherTumorType) {
        // Tumor type -> drug -> LevelOfEvidence and alteration set
        Map<String, Map<String, Map<String, Object>>> map = new TreeMap<>();
        Set<String> drugs= new HashSet<>();
        Map<String, Set<String>> levelZeroDrugs= new HashMap<>();
        List<String> list = new ArrayList<String>();

        for (Evidence ev : evidences) {
            String tt = ev.getTumorType().getName().toLowerCase();
            Map<String, Map<String, Object>> ttMap = map.get(tt);
            if (ttMap == null && !ev.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_0)) {
                ttMap = new TreeMap<String, Map<String, Object>>();
                map.put(tt, ttMap);
            }

            for (Treatment t : ev.getTreatments()) {
                for (Drug drug : t.getDrugs()) {
                    String drugName = drug.getDrugName().toLowerCase();
                    if(ev.getLevelOfEvidence().equals(LevelOfEvidence.LEVEL_0)){
                        if(!levelZeroDrugs.containsKey(drugName)){
                            levelZeroDrugs.put(drugName, new HashSet<String>());
                        }
                        if(!levelZeroDrugs.get(drugName).contains(tt)){
                            levelZeroDrugs.get(drugName).add(tt);
                        }
                    }else{
                        Map<String, Object> drugMap = ttMap.get(drugName);
                        if(!drugs.contains(drugName)){
                            drugs.add(drugName);
                        }
                        if(drugMap == null){
                            drugMap = new TreeMap<>();
                            ttMap.put(drugName, drugMap);
                            drugMap.put("approvedIndications", t.getApprovedIndications());
                            drugMap.put("level", ev.getLevelOfEvidence());
                            drugMap.put("alteration", ev.getAlterations());
                        }
                    }
                }
            }
        }

        if(map.size() > 2){
            list.add(treatmentsToStringAboveLimit(drugs, capFirstLetter, fda, nccn, null));
        }else{
            boolean first = true;
            for (Map.Entry<String, Map<String, Map<String, Object>>> entry : map.entrySet()) {
                String tt = entry.getKey();
                list.add(treatmentsToString(entry.getValue(), tt, queryAlteration, first & capFirstLetter, fda, nccn));
                first = false;
            }
        }
        if(levelZeroDrugs.size() > 0) {
            list.add(treatmentsToStringLevelZero(levelZeroDrugs, list.size()==0 & capFirstLetter));
        }
        return listToString(list);
    }

    private String treatmentsToStringLevelZero(Map<String, Set<String>> drugs, Boolean capFirstLetter){
        StringBuilder sb = new StringBuilder();
        Set<String> tumorTypes = new HashSet<>();
        boolean sameDrugs = true;

        for(String drugName : drugs.keySet()){
            if(tumorTypes.isEmpty()){
                tumorTypes = drugs.get(drugName);
            }else{
                if(tumorTypes.size() != drugs.get(drugName).size()){
                    sameDrugs = false;
                    break;
                }
                for(String tt : drugs.get(drugName)){
                    if(!tumorTypes.contains(tt)){
                        sameDrugs = false;
                        break;
                    }
                }
            }
        }

        if(sameDrugs) {
            sb.append(drugStr(drugs.keySet(), capFirstLetter, true, false, null));
        }else{
            sb.append(capFirstLetter ? "T" : "t")
                .append("here are multiple FDA-approved agents");
        }
        sb.append(" for treatment of patients with ");
        sb.append(tumorTypes.size()>2?"different tumor types":listToString(new ArrayList<String>(tumorTypes)))
                .append(" irrespective of mutation status");
        return sb.toString();
    }

    private String drugStr(Set<String> drugs, boolean capFirstLetter, boolean fda, boolean nccn, String approvedIndication) {
        int drugLimit = 3;

        StringBuilder sb = new StringBuilder();

        if(drugs.size() > drugLimit){
            sb.append(capFirstLetter?"T":"t").append("here");
        }else{
            sb.append(capFirstLetter?"T":"t").append("he drug");
            if (drugs.size()>1) {
                sb.append("s");
            }
            sb.append(" ");
            sb.append(listToString(new ArrayList<String>(drugs)));
        }
        if (fda || nccn) {
            sb.append(" ");
            if (drugs.size()>1) {
                sb.append("are ");
            } else {
                sb.append("is ");
            }
        }

        if (fda) {
            sb.append(" FDA-approved");
        } else if (nccn){
            if(approvedIndication != null){
                sb.append("FDA-approved for the treatment of ")
                        .append(approvedIndication)
                        .append(" and");
            }

            if (drugs.size() > drugLimit || approvedIndication != null) {
                sb.append(" NCCN-compendium listed");
            }else if(drugs.size() <= drugLimit) {
                sb.append(" listed by NCCN-compendium");
            }
        }

        if (drugs.size() > drugLimit) {
            sb.append(" drugs");
        }

        return sb.toString();
    }

    private String treatmentsToStringAboveLimit(Set<String> drugs, boolean capFirstLetter, boolean fda, boolean nccn, String approvedIndication) {
        StringBuilder sb = new StringBuilder();
        sb.append(drugStr(drugs, capFirstLetter, fda, nccn, null));
        sb.append(" for treatment of patients with different tumor types harboring specific mutations");
        return sb.toString();
    }

    private Map<String, Object> drugsAreSameByAlteration(Map<String, Map<String, Object>> drugs){
        Set<Alteration> alterations = new HashSet<>();
        Map<String, Object> map = new HashMap<>();

        map.put("isSame", true);
        map.put("alterations", alterations);

        for (String drugName : drugs.keySet()) {
            Map<String, Object> drug = drugs.get(drugName);
            Set<Alteration> alts = (Set<Alteration>)drug.get("alteration");
            if(alterations.isEmpty()){
                alterations = alts;
            }else{
                if(alterations.size() != alts.size()) {
                    map.put("isSame", false);
                    return map;
                }

                for(Alteration alt : alts){
                    if(!alterations.contains(alt)){
                        map.put("isSame", false);
                        return map;
                    }
                }
            }
        }
        map.put("alterations", alterations);
        return map;
    }

    private String treatmentsToString(Map<String, Map<String, Object>> map, String tumorType, String alteration, boolean capFirstLetter, boolean fda, boolean nccn) {
        Set<String> drugs = map.keySet();
        Map<String, Object> drugAltMap = drugsAreSameByAlteration(map);
        StringBuilder sb = new StringBuilder();
        Map<String, Object> drugMap = map.get(drugs.iterator().next());
        Set<String> approvedIndications = (Set<String>)drugMap.get("approvedIndications");
        String aiStr = null;

        for(String ai : approvedIndications){
            if(ai !=null && !ai.isEmpty()){
                aiStr = ai;
                break;
            }
        }

        sb.append(drugStr(drugs, capFirstLetter, fda, nccn, aiStr))
            .append(" for treatment of patients ")
            .append(tumorType == null ? "" : ("with " + tumorType + " "))
                .append("harboring ");

        if (alteration!=null) {
            sb.append("the ").append(alteration);
        } else if ((Boolean)drugAltMap.get("isSame")){
            Set<Alteration> alterations = (Set<Alteration>)drugAltMap.get("alterations");

            if (alterations.size() <= 2){
                sb.append("the ").append(alterationsToString(alterations));
            }else {
                sb.append("specific mutations");
            }

        } else{
            sb.append("specific mutations");
        }
        return sb.toString();
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

        return ret + " mutation" + (alterations.size()>1?"s":"");
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

            if(tumorTypeAll != null) {
                for (List<TumorType> list : questTumorTypeMap.values()) {
                    list.add(tumorTypeAll);
                }
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
