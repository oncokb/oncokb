/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.MutationEffect;
import org.mskcc.cbio.oncokb.model.Oncogenicity;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;

/**
 *
 * @author jiaojiao
 */
public class validation {
    public static void main(String[] args) throws IOException{
        String rule1Result = "";
        ArrayList<Alteration> unknownVariants = new ArrayList<Alteration>();
        Map<Gene, Set<Evidence>> allGeneBasedEvidences = EvidenceUtils.getAllGeneBasedEvidences();
        Set<Gene> genes = GeneUtils.getAllGenes();
        for(Gene gene : genes){
            Set<Evidence> evidences = allGeneBasedEvidences.get(gene);
            ArrayList<Alteration> unknownOncogenicAlts = new ArrayList<Alteration>();
            ArrayList<Alteration> unknownMutationEffectAlts = new ArrayList<Alteration>();
            ArrayList<Alteration> bothUnknownList = new ArrayList<Alteration>();
            
            ArrayList<Alteration> allVariants = new ArrayList<Alteration>();
            
            for(Evidence evidenceItem : evidences){
                allVariants.addAll(evidenceItem.getAlterations());
                if(evidenceItem.getEvidenceType().toString().equals("ONCOGENIC") && evidenceItem.getKnownEffect().equals(Oncogenicity.UNKNOWN.getOncogenic())){
                   unknownOncogenicAlts.addAll(evidenceItem.getAlterations());
                }
                if(evidenceItem.getEvidenceType().toString().equals("MUTATION_EFFECT") && evidenceItem.getKnownEffect().equals(MutationEffect.UNKNOWN.getMutation_effect())){
                    unknownMutationEffectAlts.addAll(evidenceItem.getAlterations());
                }
            }
            unknownOncogenicAlts.retainAll(unknownMutationEffectAlts);
            bothUnknownList = unknownOncogenicAlts;
            //check if a variant has identical oncogenicty and mutation effect value with all its relevant variants
            for(int i = 0;i < allVariants.size();i++){
                Alteration alt = allVariants.get(i);
                ArrayList<Alteration> relevantAlts = new ArrayList<Alteration>(AlterationUtils.getRelevantAlterations(gene, alt.getAlteration(), null, null, null));
//                String firstOncogenicty = relevantAlts.get(0);
                for(int j = 1;j < relevantAlts.size();j++){
                    Alteration relevantAlt = relevantAlts.get(j);
//                    if()
                }
            }
            
            //check if all relevant alterations are also unknown
            for(int i = 0;i < bothUnknownList.size();i++){
                Alteration alt = bothUnknownList.get(i);
                ArrayList<Alteration> relevantAlts = new ArrayList<Alteration>(AlterationUtils.getRelevantAlterations(gene, alt.getAlteration(), null, null, null));
                Integer allRelevantsLength = relevantAlts.size();
                relevantAlts.retainAll(bothUnknownList);
                //check if all relevant variants are unknown in both oncogenic and mutation effect
                if(relevantAlts.size() == allRelevantsLength){
                    unknownVariants.add(alt);
                    rule1Result += alt.toString() + "\n";
                }
            }
        }
       
        try{
    		File file = new File("validation-result.txt");
    		//if file doesnt exists, then create it
    		if(!file.exists()){
                    file.createNewFile();
    		}
                String data = "******************************************************************************************\n";
    		data += "Rule 1 check result: There are " + unknownVariants.size() + " variants and all its relevant variants with unknown oncogenic and unknown mutation effect \n";
    		data += rule1Result;
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName());
    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	        bufferWritter.write(data);
    	        bufferWritter.close();
	        
    	}catch(IOException e){
    		e.printStackTrace();
    	}
        
}
}
