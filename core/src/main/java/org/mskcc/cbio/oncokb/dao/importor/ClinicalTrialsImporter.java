/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.dao.importor;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;

/**
 *
 * @author jgao
 */
public class ClinicalTrialsImporter {
    private static final String NCI_CLINICAL_TRIAL_FOLDER = "/Users/jgao/projects/oncokb-data/CTGovProtocol";
    
    public static void main(String[] args) throws IOException {
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        List<TumorType> tumorTypes = tumorTypeBo.findAll();
        
        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
        List<String> files = FileUtils.getFilesInFolder(NCI_CLINICAL_TRIAL_FOLDER, "xml");
        int n = files.size();
        System.out.println("Found "+n+" trials");
        for (int i=0; i<n; i++) {
            String file = files.get(i);
            System.out.print("Process... "+(i+1)+"/"+n);
            ClinicalTrial trial = null;
            try {
                trial = parseNci(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (trial!=null) {
                matchTumorTypes(trial, tumorTypes);
                clinicalTrialBo.save(trial);
            }
            System.out.println();
        }
        
    }
    
    private static void matchTumorTypes(ClinicalTrial trial, List<TumorType> tumorTypes) {
        String condition = trial.getDiseaseCondition().toLowerCase();
        Set<TumorType> matched = new HashSet<TumorType>();
        for (TumorType tumorType : tumorTypes) {
            for (String keyword : tumorType.getClinicalTrialKeywords()) {
                if (condition.contains(keyword.toLowerCase())) {
                    matched.add(tumorType);
                }
            }
        }
        trial.setTumorTypes(matched);
    }
    
    private static ClinicalTrial parseNci(String file) throws DocumentException {        
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        
//        String phase = doc.selectSingleNode("CTGovProtocol/ProtocolPhase").getText();
//        if (!phase.equalsIgnoreCase("Phase III") || !phase.equalsIgnoreCase("Phase IV")) return null;
        
        String status = doc.selectSingleNode("CTGovProtocol/CurrentProtocolStatus").getText();
        if (!isTrialOpen(status)) return null;
        
        String nctId = doc.selectSingleNode("CTGovProtocol/IDInfo/NCTID").getText();
        
        System.out.print(" "+nctId);
        
        return parseClinicalTrialsGov(nctId);
    }
    
    private static ClinicalTrial parseClinicalTrialsGov(String nctId) throws DocumentException {
        String url = "http://clinicaltrials.gov/show/"+nctId+"?displayxml=true";
        
        SAXReader reader = new SAXReader();
        Document doc = reader.read(url);
        
        if (!isLocationUsa(doc)) return null;
        
        System.out.print(" in US");
        
        String briefTitle = doc.selectSingleNode("clinical_study/brief_title").getText();
//        String officialTitle = doc.selectSingleNode("clinical_study/official_title").getText();
        String briefSummary = doc.selectSingleNode("clinical_study/brief_summary/textblock").getText();
//        String detailedDesc = doc.selectSingleNode("clinical_study/detailed_description/textblock").getText();
        String status = doc.selectSingleNode("clinical_study/overall_status").getText();
        String phase = doc.selectSingleNode("clinical_study/phase").getText();
        String condition = doc.selectSingleNode("clinical_study/condition").getText();
        String eligibility = doc.selectSingleNode("clinical_study/eligibility/criteria/textblock").getText();
        
        if (!isTrialOpen(status)) {
            return null;
        }
        
        ClinicalTrial trial = new ClinicalTrial();
        trial.setNctId(nctId);
        trial.setTitle(briefTitle);
        trial.setPhase(phase);
        trial.setPurpose(briefSummary);
        trial.setRecuitingStatus(status);
        trial.setDiseaseCondition(condition);
        trial.setEligibilityCriteria(eligibility);
        trial.setDrugs(parseDrugs(doc));
        
        return trial;
    }
    
    private static Set<Drug> parseDrugs(Document doc) {
        List<Node> nodes = doc.selectNodes("clinical_study/intervention_browse/mesh_term");
        Set<Drug> drugs = new HashSet<Drug>();
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();

        for (Node node : nodes) {
            String drugName = node.getText();
            Drug drug = drugBo.guessUnambiguousDrug(drugName);
            if (drug!=null) {
                drugs.add(drug);
            }
        }
        
        return drugs;
    }
    
    private static boolean isLocationUsa(Document doc) {
        List<Node> nodes = doc.selectNodes("clinical_study/location_countries/country");
        for (Node node : nodes) {
            if (node.getText().equalsIgnoreCase("United States")) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isTrialOpen(String status) {
        return !status.equalsIgnoreCase("Terminated") &&
                !status.equalsIgnoreCase("Suspended") &&
                !status.equalsIgnoreCase("Completed") &&
                !status.equalsIgnoreCase("Closed") &&
                !status.equalsIgnoreCase("Withdrawn");
    }
}
