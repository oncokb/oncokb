
package org.mskcc.cbio.oncokb.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.Treatment;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

/**
 *
 * @author jgao
 */
public final class DataAnalysis {
    
    private DataAnalysis() {
        throw new AssertionError();
    }
    
    private static final String QUEST_CURATION_FOLDER = "/Users/zhangh2/Desktop/INFO_SITES/oncokb/annotations_sample";
    private static final String QUEST_CURATION_FILE = "/data/quest-curations.txt";
    
    private static Map<String, Map> DATA = new HashMap<>();
    private static String[] branch = {
        EvidenceType.PREVALENCE.name(), 
        EvidenceType.PROGNOSTIC_IMPLICATION.name(), 
        EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY.name(), 
        EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE.name(), 
        EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY.name(), 
        EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE.name()};
    
    private static List BRANCH3 = Arrays.asList(branch);
    private static Map<String, Integer> TUMORS = new HashMap<>();
    private static Map<String, List> DRUGS = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
	List<Gene> genes = geneBo.findAll();
        Map<String, Map> genesMap = new HashMap<>();
        
        for(Gene gene : genes){
            Map<String, Integer> datum = new HashMap<>();
            datum.put("alts", 0);
            datum.put("ss", 0);
            datum.put("sr", 0);
            datum.put("is", 0);
            datum.put("ir", 0);
            datum.put("branch3", 0);
            datum.put("l1", 0);
            datum.put("l2a", 0);
            datum.put("l2b", 0);
            datum.put("l3", 0);
            datum.put("l4", 0);
            DATA.put(gene.getHugoSymbol(), datum);
            Map<String, Map> geneMap = parseGene(gene);
            genesMap.put(gene.getHugoSymbol(), geneMap);
        }
        
        JSONObject object = new JSONObject(genesMap);
        System.out.println(object.toString());
        
        JSONObject data = new JSONObject(DATA);
        System.out.println(data.toString());
        
        JSONObject tumors = new JSONObject(TUMORS);
        System.out.println(tumors.toString());
        
        JSONObject drugs = new JSONObject(DRUGS);
        System.out.println(drugs.toString());
    }
    
    private static Map<String, Map> parseGene(Gene gene) {
        AlterationBo altBo = ApplicationContextSingleton.getAlterationBo();
        
        System.out.println("#Gene: " + gene.getHugoSymbol());
        
        List<Alteration> alterations = altBo.findAlterationsByGene(Collections.singleton(gene));
        Map<String, Map> alterationsMap = new HashMap<>();
        
        DATA.get(gene.getHugoSymbol()).put("alts", alterations.size());
        
        for(Alteration alt : alterations) {
            Map<String, Map> altMap = parseAlteration(alt, gene);
            alterationsMap.put(alt.getName(), altMap);
        }
        
        return alterationsMap;
    }
    
    private static Map<String, Map> parseAlteration(Alteration alt, Gene gene) {
        TumorTypeBo ttBo = ApplicationContextSingleton.getTumorTypeBo();
        Set<TumorType> tts = ttBo.findTumorTypesWithEvidencesForAlterations(Collections.singleton(alt));
        System.out.println("\tAlteration:" + alt.getName());
        
        Map<String, Map> ttsMap = new HashMap<>();
        
        for(TumorType tt : tts) {
            if(!TUMORS.containsKey(tt.getName())){
                TUMORS.put(tt.getName(), 0);
            }
            Map<String, List> altMap = parseTumorType(tt, alt, gene);
            ttsMap.put(tt.getName(), altMap);
        }
        return ttsMap;
    }
    
    private static Map<String, List> parseTumorType(TumorType tt, Alteration alt, Gene gene) {
        System.out.println("\t\tTumorType: " + tt.getName());
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        Map<String, List> evidencesMap = new HashMap<>();
        
        for(EvidenceType type : EvidenceType.values()) {
            List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(alt), Collections.singleton(type), Collections.singleton(tt));
            List<Map> map = new ArrayList<>();
            Integer implications = 0;
            
            if(type.name().equals(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY.name())) {
                implications = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("is").toString());
                DATA.get(gene.getHugoSymbol()).put("is", implications + evidences.size());
            }
            
            if(type.name().equals(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE.name())) {
                implications = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("ir").toString());
                DATA.get(gene.getHugoSymbol()).put("ir", implications + evidences.size());
            }
            
            if(type.name().equals(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY.name())) {
                implications = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("ss").toString());
                DATA.get(gene.getHugoSymbol()).put("ss", implications + evidences.size());
            }
            
            if(type.name().equals(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE.name())) {
                implications = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("sr").toString());
                DATA.get(gene.getHugoSymbol()).put("sr", implications + evidences.size());
            }
            
            if(BRANCH3.contains(type.name())) {
                Integer branch3 = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("branch3").toString());
                DATA.get(gene.getHugoSymbol()).put("branch3", branch3 + evidences.size());
            }
            
            for(Evidence evidence : evidences) {
                Map<String, Map> evidenceMap = parseEvidence(evidence, tt, gene);
                map.add(evidenceMap);
            }
            evidencesMap.put(type.name(), map);
        }
        
        return evidencesMap;
    }
    
    private static Map<String, Map> parseEvidence(Evidence evidence, TumorType tt, Gene gene) {
//        System.out.println("\t\tEvidence Type:" + evidence.getEvidenceType());
        
        Set<Treatment> treatments = evidence.getTreatments();
        Map<String, String> level = new HashMap<>();
        String levelStr = "";
        if(evidence.getLevelOfEvidence() != null && evidence.getLevelOfEvidence().getLevel() != null) {
            levelStr = evidence.getLevelOfEvidence().getLevel();
        }
        level.put("level",  levelStr);
        if(treatments != null){
            if(treatments.isEmpty()) {
//                System.out.println("\t\t\tNo Treatment.");
                return null;
            }else {
                Map<String, Map> treatmentsMap = new HashMap<>();
                for(Treatment t : treatments) {
                    Map<String, List> treatmentMap = parseTreatment(t, tt, gene);
                    treatmentsMap.put("treatment", treatmentMap);
                    treatmentsMap.put("level", level);
                    
                    Integer levelNum = 0;
                    
                    switch(levelStr){
                        case "1":
                            Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("l1").toString());
                            DATA.get(gene.getHugoSymbol()).put("l1", ++levelNum);
                            break;
                        case "2a":
                            levelNum = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("l2a").toString());
                            DATA.get(gene.getHugoSymbol()).put("l2a", ++levelNum);
                            break;
                        case "2b":
                            levelNum = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("l2b").toString());
                            DATA.get(gene.getHugoSymbol()).put("l2b", ++levelNum);
                            break;
                        case "3":
                            levelNum = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("l3").toString());
                            DATA.get(gene.getHugoSymbol()).put("l3", ++levelNum);
                            break;
                        case "4":
                            levelNum = Integer.parseInt(DATA.get(gene.getHugoSymbol()).get("l4").toString());
                            DATA.get(gene.getHugoSymbol()).put("l4", ++levelNum);
                            break;
                        default:
                            break;
                    }
                }
                return treatmentsMap;
            }
        }else{
//            System.out.println("\t\t\tNo Treatment Info");
            return null;
        }
    }
    
    private static Map<String, List> parseTreatment(Treatment treatment, TumorType tt, Gene gene) {
//        System.out.println("\t\t\tTreatment");
        Map<String, List> treatmentsMap = new HashMap<>();
        Set<String> approvedIndications = treatment.getApprovedIndications();
        if(approvedIndications != null && !approvedIndications.isEmpty()){
            List<String> approvedIndicationsMap = new ArrayList<>();
//            System.out.println("\t\t\t\tIndications");
            for(String s : approvedIndications) {
//                System.out.println("\t\t\t\t\t ---");
                approvedIndicationsMap.add("1");
            }
            treatmentsMap.put("approvedIndications",  approvedIndicationsMap);
        }
        
        Set<Drug> drugs = treatment.getDrugs();
        if(drugs != null && !drugs.isEmpty()){
//            System.out.println("\t\t\t\tDrugs");
            List<String> drugList = new ArrayList<>();
            String ttName = tt.getName();
            for(Drug drug : drugs) {
//                System.out.println("\t\t\t\t\t" + drug.getDrugName());
                String drugName = drug.getDrugName();
                
                Integer associations = Integer.parseInt(TUMORS.get(ttName).toString());
                TUMORS.put(ttName, ++associations);
                
                if(!DRUGS.containsKey(drugName)){
                    DRUGS.put(drugName, new ArrayList<>());
                }
                
                if(!DRUGS.get(drugName).contains(gene.getHugoSymbol())){
                    DRUGS.get(drugName).add(gene.getHugoSymbol());
                }
                
                drugList.add(drugName);
                
            }
            treatmentsMap.put("drugs", drugList);
        }else{
//            System.out.println("\t\t\tNo Drug.");
        }
        
        return treatmentsMap;
    }
}
    
