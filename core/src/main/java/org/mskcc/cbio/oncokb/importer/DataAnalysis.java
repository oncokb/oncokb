
package org.mskcc.cbio.oncokb.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.NccnGuidelineBo;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.ClinicalTrial;
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
    
    private static Map<String, Integer> TUMORS = new HashMap<>();
    private static Map<String, List> DRUGS = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
	List<Gene> genes = geneBo.findAll();
        Map<String, Map> genesMap = new HashMap<>();
        
        for(Gene gene : genes){
            Map<String, Map> geneMap = parseGene(gene);
            genesMap.put(gene.getHugoSymbol(), geneMap);
        }
        
        JSONObject object = new JSONObject(genesMap);
        System.out.println(object.toString());
        
        JSONObject tumors = new JSONObject(TUMORS);
        System.out.println(tumors.toString());
        
        JSONObject drugs = new JSONObject(DRUGS);
        System.out.println(drugs.toString());
    }
    
    private static Map<String, Map> parseGene(Gene gene) {
        System.out.println("#Gene: " + gene.getHugoSymbol());
        
        AlterationBo altBo = ApplicationContextSingleton.getAlterationBo();
        Map<String, String> attrs = new HashMap<>();
        List<Alteration> alterations = altBo.findAlterationsByGene(Collections.singleton(gene));
        Map<String, Map> alterationsMap = new HashMap<>();
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> evidences = evidenceBo.findEvidencesByGene(Collections.singleton(gene));
        
        for(Evidence evidence : evidences) {
            if(evidence.getEvidenceType().equals(EvidenceType.GENE_SUMMARY)){
                if(evidence.getDescription().isEmpty()){
                    attrs.put("hasSummary", "FALSE");
                }else{
                    System.out.println("\t\thas summary");
                    attrs.put("hasSummary", "TRUE");
                }
            }
            if(evidence.getEvidenceType().equals(EvidenceType.GENE_BACKGROUND)){
                if(evidence.getDescription().isEmpty()){
                    attrs.put("hasBackground", "FALSE");
                }else{
                    System.out.println("\t\thas background");
                    attrs.put("hasBackground", "TRUE");
                }
            }
        }    
        alterationsMap.put("attrs", attrs);
        
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
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(alt), Collections.singleton(EvidenceType.MUTATION_EFFECT));
        Map<Integer, String> mutationEffect = new HashMap();
        for(Evidence evidence : evidences){
            mutationEffect.put(evidence.getEvidenceId(), evidence.getKnownEffect());
        }
        if(evidences.size() > 1){
            System.out.println("******* Warning**** multi mutation effects");
        }
        
        Map<String, Map> ttsMap = new HashMap<>();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("mutationEffect", mutationEffect);
        attrs.put("oncoGenic", alt.getOncogenic().toString());
        attrs.put("mutationType", alt.getAlterationType().name());
        ttsMap.put("attrs", attrs);
        
        for(TumorType tt : tts) {
            if(!TUMORS.containsKey(tt.getName())){
                TUMORS.put(tt.getName(), 0);
            }
            Map<String, Object> altMap = parseTumorType(tt, alt, gene);
            ttsMap.put(tt.getName(), altMap);
        }
        return ttsMap;
    }
    
    private static Map<String, Object> parseTumorType(TumorType tt, Alteration alt, Gene gene) {
        System.out.println("\t\tTumorType: " + tt.getName());
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        Map<String, Object> evidencesMap = new HashMap<>();
        
        List<EvidenceType> evidenceTypes = new ArrayList();
        evidenceTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE);
        evidenceTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
        evidenceTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE);
        evidenceTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        evidenceTypes.add(EvidenceType.PREVALENCE);
        evidenceTypes.add(EvidenceType.PROGNOSTIC_IMPLICATION);
        evidenceTypes.add(EvidenceType.NCCN_GUIDELINES);
        
        List types = new ArrayList();
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE);
        types.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE);
        types.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        
        List specialTypes = new ArrayList();
        specialTypes.add(EvidenceType.PREVALENCE);
        specialTypes.add(EvidenceType.PROGNOSTIC_IMPLICATION);
        specialTypes.add(EvidenceType.NCCN_GUIDELINES);
        
        for(EvidenceType type : evidenceTypes) {
            List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(alt), Collections.singleton(type), Collections.singleton(tt));
            List<Object> map = new ArrayList<>();
            
            if(types.contains(type)) {
                for(Evidence evidence : evidences) {
                    if(evidence.getLevelOfEvidence() != null) {
                        Map<String, Object> evidenceMap = parseEvidence(evidence, tt, gene);
                        map.add(evidenceMap);
                    }
                }
                evidencesMap.put(type.name(), map);
            }else if(specialTypes.contains(type)){
                List<Integer> evidenceIds = new ArrayList();
                for(Evidence evidence : evidences){
                    evidenceIds.add(evidence.getEvidenceId());
                }
                if(evidences.size() > 1){
                    System.out.println("******* Warning**** multi " + type.name());
                }
                evidencesMap.put(type.name(), evidenceIds);
            }
        }
        ClinicalTrialBo clinicalTrialBo = ApplicationContextSingleton.getClinicalTrialBo();
        List<ClinicalTrial> trials = clinicalTrialBo.findClinicalTrialByTumorTypeAndAlteration(Collections.singleton(tt), Collections.singleton(alt), true);
        List<String> trialIds = new ArrayList();
        for(ClinicalTrial trial : trials){
            trialIds.add(trial.getNctId());
        }
        evidencesMap.put("trials", trialIds);
            
        return evidencesMap;
    }
    
    private static Map<String, Object> parseEvidence(Evidence evidence, TumorType tt, Gene gene) {
        Map<String, Object> therapyMap = new HashMap<>();
        therapyMap.put("approvedIndications", false);

        //Get therapy level
        Set<Treatment> treatments = evidence.getTreatments();

        Map<String, String> level = new HashMap<>();
        String levelStr = "";
        if(evidence.getLevelOfEvidence() != null && evidence.getLevelOfEvidence().getLevel() != null) {
            levelStr = evidence.getLevelOfEvidence().getLevel();
        }
        level.put("level",  levelStr);
        therapyMap.put("level", level);
        therapyMap.put("name", "");

        //Get therapy treatments
        List<Map> treatmentsList = new ArrayList<>();
        List<String> approvedIndicationsList = new ArrayList<>();
        for(Treatment t : treatments) {
            Map<String, Object> treatmentMap = parseTreatment(t, tt, gene);

            Set<String> approvedIndications = t.getApprovedIndications();
            for(String s : approvedIndications) {
                if(!s.isEmpty()){
                    approvedIndicationsList.add(treatmentMap.get("name").toString());
                }
            }

            String therapyName = therapyMap.get("name").toString();
            therapyMap.put("name", therapyName + (therapyName.isEmpty()?"":",")+treatmentMap.get("name"));

            treatmentsList.add(treatmentMap);
        }
        therapyMap.put("approvedIndications", approvedIndicationsList);
        therapyMap.put("treatments", treatmentsList);

        return therapyMap;
    }
    
    private static Map<String, Object> parseTreatment(Treatment treatment, TumorType tt, Gene gene) {
//        System.out.println("\t\t\tTreatment");
        Map<String, Object> treatmentsMap = new HashMap<>();

        Set<Drug> drugs = treatment.getDrugs();
        if(drugs != null && !drugs.isEmpty()){
//            System.out.println("\t\t\t\tDrugs");
            List<String> drugList = new ArrayList<>();
            String ttName = tt.getName();

            for(Drug drug : drugs) {
                System.out.println("\t\t\t\t\t" + drug.getDrugName());
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
            treatmentsMap.put("name", StringUtils.join(drugList, "+"));
        }else{
//            System.out.println("\t\t\tNo Drug.");
        }
        
        return treatmentsMap;
    }
}
    
