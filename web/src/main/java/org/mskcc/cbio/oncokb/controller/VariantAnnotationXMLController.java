/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.controller;

import java.util.List;
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
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
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
    @RequestMapping(value="/var_annotation", produces="application/xml")
    public @ResponseBody String getVariantAnnotation(
            @RequestParam(value="entrezGeneId", required=false) Integer entrezGeneId,
            @RequestParam(value="hugoSymbol", required=false) String hugoSymbol,
            @RequestParam(value="alterationType", required=false) String alterationType,
            @RequestParam(value="alteration", required=false) String alteration,
            @RequestParam(value="cancerType", required=false) String cancerType) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        // find alteration
        if (entrezGeneId == null && hugoSymbol == null) {
            return "<!-- no gene was specified --><xml/>";
        }
        
        Gene gene = null;
        if (entrezGeneId!=null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezGeneId);
        } else if (hugoSymbol!=null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }
        
        if (gene == null) {
            return "<!-- cound not find gene --><xml></xml>";
        }
        
        if (alterationType==null) {
            return "<!-- no alteration type --><xml></xml>";
        }
        
        AlterationType type = AlterationType.valueOf(alterationType.toUpperCase());
        if (type == null) {
            return "<!-- wrong alteration type --><xml></xml>";
        }
        
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        Alteration alt = alterationBo.findAlteration(gene, type, alteration);
        if (alt == null) {
            return "<!-- cound not find variant --><xml></xml>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>\n");
        
        // find tumor types
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        List<TumorType> tumorTypes = tumorTypeBo.findTumorTypesWithEvidencesForAlteration(alt);
        
        for (TumorType tumorType : tumorTypes) {
            sb.append("<cancer_type type=\"").append(tumorType.getName()).append("\">\n");
            
            // find prevalence evidence blob
            EvidenceBlobBo evidenceBlobBo = ApplicationContextSingleton.getEvidenceBlobBo();
            List<EvidenceBlob> prevalanceEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alt, EvidenceType.PREVALENCE, tumorType);
            for (EvidenceBlob eb : prevalanceEbs) {
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
            List<EvidenceBlob> prognosticEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alt, EvidenceType.PROGNOSTIC_IMPLICATION, tumorType);
            for (EvidenceBlob eb : prognosticEbs) {
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
            List<EvidenceBlob> stdImpEbsSensitivity = evidenceBlobBo.findEvidenceBlobsByAlteration(alt, EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY, tumorType);
            List<EvidenceBlob> stdImpEbsResisitance = evidenceBlobBo.findEvidenceBlobsByAlteration(alt, EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE, tumorType);
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
            List<EvidenceBlob> nccnEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alt, EvidenceType.NCCN_GUIDELINES, tumorType);
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
            List<EvidenceBlob> invImpEbsSensitivity = evidenceBlobBo.findEvidenceBlobsByAlteration(alt, EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY, tumorType);
            List<EvidenceBlob> invImpEbsResisitance = evidenceBlobBo.findEvidenceBlobsByAlteration(alt, EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE, tumorType);
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
            List<EvidenceBlob> trialsEbs = evidenceBlobBo.findEvidenceBlobsByAlteration(alt, EvidenceType.CLINICAL_TRIAL, tumorType);
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
            
            sb.append("</drug>\n");
        
        }
        
        LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();
        if (levelOfEvidence!=null) {
            sb.append("<level_of_evidence>");
            sb.append(StringEscapeUtils.escapeXml(levelOfEvidence.getDescription()));
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
}
