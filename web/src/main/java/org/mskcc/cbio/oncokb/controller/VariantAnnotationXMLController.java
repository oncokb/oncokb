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
import org.mskcc.cbio.oncokb.bo.EvidenceBlobBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Article;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceBlob;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.NccnGuideline;
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
        alt.setAlteration(alteration);
        
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
        EvidenceBlobBo evidenceBlobBo = ApplicationContextSingleton.getEvidenceBlobBo();
        List<EvidenceBlob> geneBgEbs = evidenceBlobBo.findEvidenceBlobsByGene(gene, EvidenceType.GENE_BACKGROUND);
        if (!geneBgEbs.isEmpty()) {
            EvidenceBlob eb = geneBgEbs.get(0);
            sb.append("<gene_annotation>\n");
            sb.append("<description>\n");
            sb.append(StringEscapeUtils.escapeXml(eb.getDescription())).append("\n");
            sb.append("</description>\n");
            if (!eb.getEvidences().isEmpty()) {
                exportRefereces(eb.getEvidences().iterator().next(), sb);
            }

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
        
        List<EvidenceBlob> mutationEffectEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.MUTATION_EFFECT);
        if (!mutationEffectEbs.isEmpty()) {
            EvidenceBlob eb = mutationEffectEbs.get(0);
            Evidence ev = null;
            if (!eb.getEvidences().isEmpty()) {
                ev = eb.getEvidences().iterator().next();
            }
            sb.append("<variant_effect>\n");
            sb.append("<effect>\n");
            if (ev!=null) {
                sb.append(ev.getKnownEffect());
            }
            sb.append("</effect>\n");
            sb.append("<description>\n");
            sb.append(StringEscapeUtils.escapeXml(eb.getDescription())).append("\n");
            sb.append("</description>\n");
            if (ev!=null) {
                exportRefereces(ev, sb);
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
            List<EvidenceBlob> prevalanceEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.PREVALENCE, tt);
            if (!prevalanceEbs.isEmpty()) {
                EvidenceBlob eb = prevalanceEbs.get(0);
                sb.append("<prevalence>\n");
                sb.append("<description>\n");
                sb.append(StringEscapeUtils.escapeXml(eb.getDescription())).append("\n");
                sb.append("</description>\n");
                if (!eb.getEvidences().isEmpty()) {
                    exportRefereces(eb.getEvidences().iterator().next(), sb);
                }
                
                sb.append("</prevalence>\n");
            }
            
            // find prognostic implication evidence blob
            List<EvidenceBlob> prognosticEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.PROGNOSTIC_IMPLICATION, tt);
            if (!prognosticEbs.isEmpty()) {
                EvidenceBlob eb = prevalanceEbs.get(0);
                sb.append("<prognostic_implications>\n");
                sb.append("<description>\n");
                sb.append(StringEscapeUtils.escapeXml(eb.getDescription())).append("\n");
                sb.append("</description>\n");
                if (!eb.getEvidences().isEmpty()) {
                    exportRefereces(eb.getEvidences().iterator().next(), sb);
                }
                
                sb.append("</prognostic_implications>\n");
            }
            
            // STANDARD_THERAPEUTIC_IMPLICATIONS
            List<EvidenceBlob> stdImpEbsSensitivity = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, tt);
            List<EvidenceBlob> stdImpEbsResisitance = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE, tt);
            if (!stdImpEbsSensitivity.isEmpty() || !stdImpEbsResisitance.isEmpty()) {
                sb.append("<standard_therapeutic_implications>\n");
                for (EvidenceBlob eb : stdImpEbsSensitivity) {
                    sb.append("<sensitive_to>\n");
                    exportDrugs(eb, sb);
                    sb.append("</sensitive_to>\n");
                }
                for (EvidenceBlob eb : stdImpEbsResisitance) {
                    sb.append("<resistant_to>\n");
                    exportDrugs(eb, sb);
                    sb.append("</resistant_to>\n");
                }
                sb.append("</standard_therapeutic_implications>\n");
            }
            
            // NCCN_GUIDELINES
            List<EvidenceBlob> nccnEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.NCCN_GUIDELINES, tt);
            for (EvidenceBlob eb : nccnEbs) {
                for (Evidence ev : eb.getEvidences()) {
                    for (NccnGuideline nccnGuideline : ev.getNccnGuidelines()) {
                        sb.append("<nccn_guidelines>\n");
                        sb.append("<disease>");
                        if (nccnGuideline.getDisease() != null) {
                            sb.append(nccnGuideline.getDisease());
                        }
                        sb.append("</disease>\n");
                        sb.append("<version>");
                        if (nccnGuideline.getVersion() != null) {
                            sb.append(nccnGuideline.getVersion());
                        }
                        sb.append("</version>\n");
                        sb.append("<page>");
                        if (nccnGuideline.getPages() != null) {
                            sb.append(nccnGuideline.getPages());
                        }
                        sb.append("</page>\n");
                        sb.append("</nccn_guidelines>\n");
                        
                    }
                }
            }
            
            // INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS
            List<EvidenceBlob> invImpEbsSensitivity = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, tt);
            List<EvidenceBlob> invImpEbsResisitance = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, tt);
            if (!invImpEbsSensitivity.isEmpty() || !invImpEbsResisitance.isEmpty()) {
                sb.append("<investigational_therapeutic_implications>\n");
                for (EvidenceBlob eb : invImpEbsSensitivity) {
                    sb.append("<sensitive_to>\n");
                    exportDrugs(eb, sb);
                    sb.append("</sensitive_to>\n");
                }
                for (EvidenceBlob eb : invImpEbsResisitance) {
                    sb.append("<resistant_to>\n");
                    exportDrugs(eb, sb);
                    sb.append("</resistant_to>\n");
                }
                sb.append("</investigational_therapeutic_implications>\n");
            }
            
            // CLINICAL_TRIAL
            List<EvidenceBlob> trialsEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alterations, EvidenceType.CLINICAL_TRIAL, tt);
            for (EvidenceBlob eb : trialsEbs) {
                for (Evidence ev : eb.getEvidences()) {
                    for (ClinicalTrial trial : ev.getClinicalTrials()) {
                        sb.append("<clinical_trial>\n");
                        
                        sb.append("<trial_id>");
                        if (trial.getNctId() != null) {
                            sb.append(trial.getNctId());
                        }
                        sb.append("</trial_id>\n");
                        
                        sb.append("<locations>");
                        if (trial.getLocation() != null) {
                            sb.append(StringEscapeUtils.escapeXml(trial.getLocation()));
                        }
                        sb.append("</locations>\n");
                        
                        sb.append("<mskcc_trial>");
                        if (trial.getIsMskccTrial() != null) {
                            sb.append(trial.getIsMskccTrial()?"Yes":"No");
                        }
                        sb.append("</mskcc_trial>");
                        
                        sb.append("<title>\n");
                        if (trial.getTitle() != null) {
                            sb.append(StringEscapeUtils.escapeXml(trial.getTitle())).append("\n");
                        }
                        sb.append("</title>\n");
                        
                        sb.append("<purpose>\n");
                        if (trial.getPurpose() != null) {
                            sb.append(StringEscapeUtils.escapeXml(trial.getPurpose())).append("\n");
                        }
                        sb.append("</purpose>\n");
                        
                        sb.append("<recruiting_status>");
                        if (trial.getRecuitingStatus() != null) {
                            sb.append(StringEscapeUtils.escapeXml(trial.getRecuitingStatus()));
                        }
                        sb.append("</recruiting_status>\n");
                        
                        sb.append("<eligibility_criteria>\n");
                        if (trial.getEligibilityCriteria() != null) {
                            sb.append(StringEscapeUtils.escapeXml(trial.getEligibilityCriteria())).append("\n");
                        }
                        sb.append("</eligibility_criteria>\n");
                        
                        sb.append("<phase>\n");
                        if (trial.getPhase() != null) {
                            sb.append(StringEscapeUtils.escapeXml(trial.getPhase()));
                        }
                        sb.append("</phase>\n");
                        
                        
                        sb.append("</clinical_trial>\n");
                    }
                }
            }
            
            sb.append("</cancer_type>\n");
        }
        
        sb.append("</xml>");
        
        return sb.toString();
    }
    
    private void exportDrugs(EvidenceBlob eb, StringBuilder sb) {
        for (Evidence ev : eb.getEvidences()) {
            sb.append("<drugs>\n");
            exportDrugSensitivity(ev, sb);
            sb.append("</drugs>\n");
        }
        sb.append("<description>\n");
        sb.append(StringEscapeUtils.escapeXml(eb.getDescription())).append("\n");
        sb.append("</description>\n");
    }
    
    private void exportDrugSensitivity(Evidence evidence, StringBuilder sb) {
        
        Set<Drug> drugs = evidence.getDrugs();
        for (Drug drug : drugs) {
            sb.append("<drug>");
            
            sb.append("<name>");
            String name = drug.getDrugName();
            if (name != null) {
                sb.append(StringEscapeUtils.escapeXml(name));
            }
            sb.append("</name>\n");
            
            Set<String> synonyms = drug.getSynonyms();
            for (String synonym : synonyms) {
                sb.append("<synonym>");
                sb.append(synonym);
                sb.append("</synonym>\n");
            }
            
            sb.append("<fda_approved>");
            Boolean fdaApproved = drug.isFdaApproved();
            if (fdaApproved!=null) {
                sb.append(fdaApproved ? "Yes" : "No");
            }
            sb.append("</fda_approved>\n");
            
//            sb.append("<description>");
//            String desc = drug.getDescription();
//            if (desc != null) {
//                sb.append(StringEscapeUtils.escapeXml(desc));
//            }
//            sb.append("</description>\n");
            
            sb.append("</drug>\n");
        
        }
        
        LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();
        if (levelOfEvidence!=null) {
            sb.append("<level_of_evidence>\n");
            sb.append("<level>");
            sb.append(levelOfEvidence.getLevel());
            sb.append("</level>\n");
            sb.append("<description>\n");
            sb.append(StringEscapeUtils.escapeXml(levelOfEvidence.getDescription())).append("\n");
            sb.append("</description>\n");
            sb.append("</level_of_evidence>\n");
        }
        
        exportRefereces(evidence, sb);
    }
    
    private void exportRefereces(Evidence evidence, StringBuilder sb) {
        Set<Article> articles = evidence.getArticles();
        for (Article article : articles) {
            sb.append("<reference>");
            sb.append("<pmid>");
            String pmid = article.getPmid();
            if (pmid != null) {
                sb.append(pmid);
            }
            sb.append("</pmid>\n");
            
            sb.append("<citation>");
            String reference = article.getReference();
            if (reference != null) {
                sb.append(StringEscapeUtils.escapeXml(reference));
            }
            sb.append("</citation>\n");
            
            sb.append("</reference>\n");
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
