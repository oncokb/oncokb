
package org.mskcc.cbio.oncokb.importer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.ArticleBo;
import org.mskcc.cbio.oncokb.bo.ClinicalTrialBo;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.NccnGuidelineBo;
import org.mskcc.cbio.oncokb.bo.TreatmentBo;
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
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneAnnotatorMyGeneInfo2;
import org.mskcc.cbio.oncokb.util.NcbiEUtils;

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
    
    public static void main(String[] args) throws Exception {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        
        List<Gene> genes = geneBo.findAll();
        
        for(Gene gene : genes){
            parseGene(gene);
        }
    }
    
    private static void parseGene(Gene gene) {
        AlterationBo altBo = ApplicationContextSingleton.getAlterationBo();
        
        System.out.println("#Gene: " + gene.getHugoSymbol());
        
        List<Alteration> alterations = altBo.findAlterationsByGene(Collections.singleton(gene));
        
        for(Alteration alt : alterations) {
            parseAlteration(alt);
        }
    }
    
    private static void parseAlteration(Alteration alt) {
        TumorTypeBo ttBo = ApplicationContextSingleton.getTumorTypeBo();
        Set<TumorType> tts = ttBo.findTumorTypesWithEvidencesForAlterations(Collections.singleton(alt));
//        List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(alt));
        System.out.println("\tAlteration:" + alt.getName());
//        for(Evidence evidence : evidences) {
//            parseEvidence(evidence);
//        }
        
        EvidenceType.values();
        for(TumorType tt : tts){
            parseTumorType(tt, alt);
        }
    }
    
    private static void parseTumorType(TumorType tt, Alteration alt) {
        System.out.println("\t\tTumorType: " + tt.getName());
        
        EvidenceBo evidenceBo = ApplicationContextSingleton.getEvidenceBo();
        
        
        for(EvidenceType type : EvidenceType.values()) {
            List<Evidence> evidences = evidenceBo.findEvidencesByAlteration(Collections.singleton(alt), Collections.singleton(type), Collections.singleton(tt));
            
            for(Evidence evidence : evidences) {
                parseEvidence(evidence);
            }
        }
    }
    
    private static void parseEvidence(Evidence evidence) {
        System.out.println("\t\tEvidence Type:" + evidence.getEvidenceType());
        evidence.getEvidenceType();
        
        Set<Treatment> treatments = evidence.getTreatments();
        
        if(treatments != null){
            if(treatments.size() == 0) {
                System.out.println("\t\t\tNo Treatment.");
            }else {
                for(Treatment t : treatments) {
                    parseTreatment(t);
                }
            }
        }else{
            System.out.println("\t\t\tNo Treatment Info");
        }
        
    }
    
    private static void parseTreatment(Treatment treatment) {
        System.out.println("\t\t\tTreatment");
        
        Set<String> approvedIndications = treatment.getApprovedIndications();
        if(approvedIndications != null && !approvedIndications.isEmpty()){
            System.out.println("\t\t\t\tIndications");
            for(String s : approvedIndications) {
                System.out.println("\t\t\t\t\t ---");
            }
        }
        
        Set<Drug> drugs = treatment.getDrugs();
        if(drugs != null && !drugs.isEmpty()){
            System.out.println("\t\t\t\tDrugs");
            for(Drug drug : drugs) {
                System.out.println("\t\t\t\t\t" + drug.getDrugName());
            }
        }else{
            System.out.println("\t\t\tNo Drug.");
        }
    }
}
    
