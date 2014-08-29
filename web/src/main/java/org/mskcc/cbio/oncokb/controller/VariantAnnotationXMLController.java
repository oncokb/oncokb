/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
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
    @RequestMapping(value="/var_annotation", produces="application/xml")//plain/text
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
        
        // gene background
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> geneBgEvs = evidenceBo.findEvidencesByGene(gene, EvidenceType.GENE_BACKGROUND);
        if (!geneBgEvs.isEmpty()) {
            Evidence ev = geneBgEvs.get(0);
            sb.append("<gene_annotation>\n");
            sb.append("    <description>");
            sb.append(StringEscapeUtils.escapeXml(ev.getDescription()).trim());
            sb.append("</description>\n");
            exportRefereces(ev, sb, "    ");
            sb.append("</gene_annotation>\n");
        }
        
        
        alt.setGene(gene);
        
        if (alterationType==null) {
            sb.append("<!-- no alteration type --></xml>");
            return sb.toString();
        }
        
        AlterationType type = AlterationType.valueOf(alterationType.toUpperCase());
        if (type == null) {
            sb.append("<!-- wrong alteration type --></xml>");
            return sb.toString();
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
        
        AlterationUtils.annotateAlteration(alt);
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        List<Alteration> alterations = alterationBo.findRelevantAlterations(alt);
        if (alterations.isEmpty()) {
            sb.append("<!-- There is no information about the function of this variant in the MSKCC OncoKB. --></xml>");
            return sb.toString();
        }
        
        List<Evidence> mutationEffectEbs = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.MUTATION_EFFECT);
        for (Evidence ev : mutationEffectEbs) {
            sb.append("<variant_effect>\n");
            sb.append("    <effect>");
            if (ev!=null) {
                sb.append(ev.getKnownEffect());
            }
            sb.append("</effect>\n");
            sb.append("    <description>");
            sb.append(StringEscapeUtils.escapeXml(ev.getDescription()).trim());
            sb.append("</description>\n");
            if (ev!=null) {
                exportRefereces(ev, sb, "    ");
            }

            sb.append("</variant_effect>\n");
        }
        
        // find tumor types
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        List<TumorType> tumorTypes = new LinkedList<TumorType>(tumorTypeBo.findTumorTypesWithEvidencesForAlterations(alterations));
        int nRelevantTumorTypes = sortTumorType(tumorTypes, tumorType);
        
        int iTumorTypes = 0;
        for (TumorType tt : tumorTypes) {
            sb.append("<cancer_type type=\"").append(tt.getName()).append("\" relevant_to_patient_disease=\""+((iTumorTypes++<nRelevantTumorTypes)?"Yes":"No")+"\">\n");
            
            // find prevalence evidence blob
            List<Evidence> prevalanceEbs = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.PREVALENCE, tt);
            for (Evidence ev : prevalanceEbs) {
                sb.append("    <prevalence>\n");
                sb.append("        <description>");
                sb.append(StringEscapeUtils.escapeXml(ev.getDescription()).trim());
                sb.append("</description>\n");
                exportRefereces(ev, sb, "        ");
                sb.append("    </prevalence>\n");
            }
            
            // find prognostic implication evidence blob
            List<Evidence> prognosticEbs = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.PROGNOSTIC_IMPLICATION, tt);
            for (Evidence ev : prognosticEbs) {
                sb.append("    <prognostic_implications>\n");
                sb.append("        <description>");
                sb.append(StringEscapeUtils.escapeXml(ev.getDescription()).trim());
                sb.append("</description>\n");
                exportRefereces(ev, sb, "        ");
                sb.append("    </prognostic_implications>\n");
            }
            
            // STANDARD_THERAPEUTIC_IMPLICATIONS
            List<Evidence> stdImpEbsSensitivity = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, tt);
            List<Evidence> stdImpEbsResisitance = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE, tt);
            exportTherapeuticImplications(stdImpEbsSensitivity, stdImpEbsResisitance, "standard_therapeutic_implications", sb, "    ");
            
            // NCCN_GUIDELINES
            List<Evidence> nccnEvs = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.NCCN_GUIDELINES, tt);
            for (Evidence ev : nccnEvs) {
                for (NccnGuideline nccnGuideline : ev.getNccnGuidelines()) {
                    sb.append("    <nccn_guidelines>\n");
                    sb.append("        <disease>");
                    if (nccnGuideline.getDisease() != null) {
                        sb.append(nccnGuideline.getDisease());
                    }
                    sb.append("</disease>\n");
                    sb.append("        <version>");
                    if (nccnGuideline.getVersion() != null) {
                        sb.append(nccnGuideline.getVersion());
                    }
                    sb.append("</version>\n");
                    sb.append("        <pages>");
                    if (nccnGuideline.getPages() != null) {
                        sb.append(nccnGuideline.getPages());
                    }
                    sb.append("</pages>\n");
                    sb.append("        <recommendation_category>");
                    if (nccnGuideline.getCategory()!= null) {
                        sb.append(nccnGuideline.getCategory());
                    }
                    sb.append("</recommendation_category>\n");
                    sb.append("        <description>");
                    if (nccnGuideline.getDescription()!= null) {
                        sb.append(nccnGuideline.getDescription());
                    }
                    sb.append("</description>\n");
                    sb.append("    </nccn_guidelines>\n");
                }
            }
            
            // INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS
            List<Evidence> invImpEbsSensitivity = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, tt);
            List<Evidence> invImpEbsResisitance = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, tt);
            exportTherapeuticImplications(invImpEbsSensitivity, invImpEbsResisitance, "investigational_therapeutic_implications", sb, "    ");
            
            // CLINICAL_TRIAL
            List<Evidence> trialsEvs = evidenceBo.findEvidencesByAlteration(alterations, EvidenceType.CLINICAL_TRIAL, tt);
            for (Evidence ev : trialsEvs) {
                for (ClinicalTrial trial : ev.getClinicalTrials()) {
                    sb.append("    <clinical_trial>\n");

                    sb.append("        <trial_id>");
                    if (trial.getNctId() != null) {
                        sb.append(trial.getNctId());
                    }
                    sb.append("</trial_id>\n");

                    sb.append("        <locations>");
                    if (trial.getLocation() != null) {
                        sb.append(StringEscapeUtils.escapeXml(trial.getLocation()));
                    }
                    sb.append("</locations>\n");

                    sb.append("        <title>\n");
                    if (trial.getTitle() != null) {
                        sb.append(StringEscapeUtils.escapeXml(trial.getTitle())).append("\n");
                    }
                    sb.append("</title>\n");

                    sb.append("        <purpose>\n");
                    if (trial.getPurpose() != null) {
                        sb.append(StringEscapeUtils.escapeXml(trial.getPurpose())).append("\n");
                    }
                    sb.append("</purpose>\n");

                    sb.append("        <recruiting_status>");
                    if (trial.getRecuitingStatus() != null) {
                        sb.append(StringEscapeUtils.escapeXml(trial.getRecuitingStatus()));
                    }
                    sb.append("</recruiting_status>\n");

                    sb.append("        <eligibility_criteria>\n");
                    if (trial.getEligibilityCriteria() != null) {
                        sb.append(StringEscapeUtils.escapeXml(trial.getEligibilityCriteria())).append("\n");
                    }
                    sb.append("</eligibility_criteria>\n");

                    sb.append("        <phase>\n");
                    if (trial.getPhase() != null) {
                        sb.append(StringEscapeUtils.escapeXml(trial.getPhase()));
                    }
                    sb.append("</phase>\n");


                    sb.append("</clinical_trial>\n");
                }
            }
            
            sb.append("</cancer_type>\n");
        }
        
        sb.append("</xml>");
        
        return sb.toString();
    }
    
    private void exportTherapeuticImplications(List<Evidence> evSensitivity, List<Evidence> evResisitance, String tagTherapeuticImp, StringBuilder sb, String indent) {
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
                exportTherapeuticImplications(ev, sb, indent+"            ");
                sb.append(indent).append("        </sensitivity>\n");
            }
            for (Evidence ev : evsResisitance.get(0)) {
                sb.append(indent).append("        <resistance>\n");
                exportTherapeuticImplications(ev, sb, indent+"            ");
                sb.append(indent).append("        </resistance>\n");
            }
            sb.append(indent).append("    </general_statement>\n");
        }
        
        // specific evs
        if (!evsSensitivity.get(1).isEmpty() || !evsResisitance.get(1).isEmpty()) {
            for (Evidence ev : evsSensitivity.get(1)) {
                sb.append(indent).append("    <sensitive_to>\n");
                exportTherapeuticImplications(ev, sb, indent+"        ");
                sb.append(indent).append("    </sensitive_to>\n");
            }
            for (Evidence ev : evsResisitance.get(1)) {
                sb.append(indent).append("    <resistant_to>\n");
                exportTherapeuticImplications(ev, sb, indent+"        ");
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
    
    private void exportTherapeuticImplications(Evidence evidence, StringBuilder sb, String indent) {
        for (Treatment t : evidence.getTreatments()) {
            sb.append(indent).append("<treatment>\n");
            exportTreatment(t, sb, indent+"    ");
            sb.append(indent).append("</treatment>\n");
        }
        
        LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();
        if (levelOfEvidence!=null) {
            sb.append(indent).append("<level_of_evidence>\n");
            sb.append(indent).append("    <level>");
            sb.append(levelOfEvidence.getLevel());
            sb.append("</level>\n");
            sb.append(indent).append("    <description>");
            sb.append(StringEscapeUtils.escapeXml(levelOfEvidence.getDescription()).trim());
            sb.append("</description>\n");
            sb.append(indent).append("</level_of_evidence>\n");
        }
        
        sb.append(indent).append("<description>");
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
            
//            sb.append(indent).append("    <citation>");
//            String reference = article.getReference();
//            if (reference != null) {
//                sb.append(StringEscapeUtils.escapeXml(reference));
//            }
//            sb.append("</citation>\n");
            
            sb.append(indent).append("</reference>\n");
        }
    }
    
    private static final String TUMOR_TYPE_ALL_TUMORS = "all tumors";
    private static Map<String, List<TumorType>> questTumorTypeMap = null;
    private static List<TumorType> fromQuestTumorType(String questTumorType) {
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
                return Collections.singletonList(tumorTypeAll);
            }
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                String questType = parts[0].toLowerCase();
                TumorType oncokbType = tumorTypeBo.findTumorTypeByName(parts[1]);
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
        
        return new ArrayList<TumorType>(ret);
    }
    
    /**
     * 
     * @param tumorTypes
     * @param patientTumorType
     * @return the number of relevant tumor types
     */
    private int sortTumorType(List<TumorType> tumorTypes, String patientTumorType) {
        List<TumorType> relevantTumorTypes = fromQuestTumorType(patientTumorType);
        relevantTumorTypes.retainAll(tumorTypes); // only tumor type with evidence
        tumorTypes.removeAll(relevantTumorTypes); // other tumor types
        tumorTypes.addAll(0, relevantTumorTypes);
        
        return relevantTumorTypes.size();
    }
}
