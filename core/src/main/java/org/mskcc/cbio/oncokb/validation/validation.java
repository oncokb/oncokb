/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.validation;

import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author jiaojiao
 */
public class validation {

    public static void main(String[] args) throws IOException {
        Map<Gene, Set<Evidence>> allGeneBasedEvidences = EvidenceUtils.getAllGeneBasedEvidences();
        Set<Gene> genes = GeneUtils.getAllGenes();
        Integer count = 0;
        String result0 = "", result1 = "", result2 = "", result3 = "", result4 = "", result5 = "";
        Integer length0 = 0, length1 = 0, length2 = 0, length3 = 0, length4 = 0, length5 = 0;
        ArrayList<String> specialAlterations = new ArrayList<>(Arrays.asList("Inactivating Mutations", "Activating Mutations", "Fusions", "Inactivating", "Wildtype", "Amplification", "Fusions"));
        for (Gene gene : genes) {
            Set<Evidence> evidences = allGeneBasedEvidences.get(gene);
            Set<Alteration> VUSAlterations = AlterationUtils.findVUSFromEvidences(evidences);
            Map<Alteration, ArrayList<Alteration>> relevantAlterationsMapping = new HashMap<Alteration, ArrayList<Alteration>>();
            Map<Alteration, String> oncogenicityMapping = new HashMap<Alteration, String>();
            Map<Alteration, String> mutationEffectMapping = new HashMap<Alteration, String>();
            Map<Alteration, Set<Evidence>> multipleMutationEffects = new HashMap<>();
            Map<Alteration, Set<String>> referencesMapping = new HashMap<Alteration, Set<String>>();
            ArrayList<Alteration> altsWithDescriptions = new ArrayList<Alteration>();
            Set<Alteration> allVariants = new HashSet<Alteration>();
            Set<Alteration> allAlts = new HashSet<Alteration>();
            for (Evidence evidenceItem : evidences) {
                allVariants = evidenceItem.getAlterations();
                allAlts.addAll(allVariants);
                for (Alteration alterationItem : allVariants) {
                    relevantAlterationsMapping.put(alterationItem, new ArrayList<Alteration>(AlterationUtils.getRelevantAlterations(gene, alterationItem.getAlteration(), null, null, null)) );
                    if (evidenceItem.getEvidenceType().toString().equals("ONCOGENIC")) {
                        oncogenicityMapping.put(alterationItem, evidenceItem.getKnownEffect());
                    }
                    if (evidenceItem.getEvidenceType().toString().equals("MUTATION_EFFECT")) {
                        mutationEffectMapping.put(alterationItem, evidenceItem.getKnownEffect());
                    }
                    if (evidenceItem.getEvidenceType().toString().equals("MUTATION_EFFECT")) {
                        if (!multipleMutationEffects.containsKey(alterationItem)) {
                            multipleMutationEffects.put(alterationItem, new HashSet<Evidence>());
                        }
                        multipleMutationEffects.get(alterationItem).add(evidenceItem);
                    }
                    if(referencesMapping.containsKey(alterationItem)){
                        Set<String> oldPMIDs = referencesMapping.get(alterationItem);
                        Set<String> newPMIDs = EvidenceUtils.getPmids(new HashSet<Evidence>(Arrays.asList(evidenceItem)));
                        newPMIDs.addAll(oldPMIDs);
                        referencesMapping.put(alterationItem, newPMIDs);
                    }else{
                        referencesMapping.put(alterationItem, EvidenceUtils.getPmids(new HashSet<Evidence>(Arrays.asList(evidenceItem))));
                    }
                    if(!altsWithDescriptions.contains(alterationItem)){    
                        if(evidenceItem.getDescription() != null && !evidenceItem.getDescription().isEmpty() || evidenceItem.getShortDescription() != null && !evidenceItem.getShortDescription().isEmpty()){
                            altsWithDescriptions.add(alterationItem);
                        }
                    }
                }
            }
            for (Alteration alt : allAlts) {
                ArrayList<Alteration> relevantAlts = relevantAlterationsMapping.get(alt);
                for (Alteration relevantAlt : relevantAlts) {
                    if (oncogenicityMapping.containsKey(alt)
                        && oncogenicityMapping.get(alt) != null
                        && oncogenicityMapping.get(relevantAlt) != null
                        && oncogenicityMapping.containsKey(relevantAlt) 
                        && !oncogenicityMapping.get(alt).equals(oncogenicityMapping.get(relevantAlt))) {
                        result0 += alt.toString() + ": " + Oncogenicity.getByLevel(oncogenicityMapping.get(alt)).getDescription() + ". Relevant alteration: " +  relevantAlt.toString() + ": " + Oncogenicity.getByLevel(oncogenicityMapping.get(relevantAlt)).getDescription() + "\n";
                        length0++;
                        break;
                    }
                }
                for (Alteration relevantAlt : relevantAlts) {
                    if (mutationEffectMapping.containsKey(alt) && mutationEffectMapping.containsKey(relevantAlt) &&
                        mutationEffectMapping.get(alt) != null && mutationEffectMapping.get(relevantAlt) != null &&
                        !mutationEffectMapping.get(alt).equals(mutationEffectMapping.get(relevantAlt))) {
                        result1 += alt.toString() + ": " + mutationEffectMapping.get(alt) +
                            ". Relevant alteration: " + relevantAlt.toString() + ": " +
                            mutationEffectMapping.get(relevantAlt) +  "\n";
                        length1++;
                        break;
                    }
                }
                if (oncogenicityMapping.containsKey(alt)
                    && oncogenicityMapping.get(alt) != null
                    && oncogenicityMapping.get(alt).equals(Oncogenicity.UNKNOWN.getOncogenic()) 
                    && mutationEffectMapping.containsKey(alt) 
                    && mutationEffectMapping.get(alt) != null
                    && mutationEffectMapping.get(alt).equals(MutationEffect.UNKNOWN.getMutation_effect())) {
                    Integer relevantsSize = relevantAlts.size();
                    Integer relevantCount = 0;
                    for (Alteration relevantAlt : relevantAlts) {
                        relevantCount++;
                        if (relevantCount == relevantsSize - 1 && oncogenicityMapping.containsKey(alt) && oncogenicityMapping.get(relevantAlt).equals(Oncogenicity.UNKNOWN.getOncogenic()) 
                                && mutationEffectMapping.containsKey(alt) && mutationEffectMapping.get(relevantAlt).equals(MutationEffect.UNKNOWN.getMutation_effect())) {
                            result2 += relevantAlt.toString() + "\n";
                            length2++;
                        }
                    }
                }
                if (!oncogenicityMapping.containsKey(alt) && !mutationEffectMapping.containsKey(alt) && !VUSAlterations.contains(alt) && !specialAlterations.contains(alt.getAlteration()) && !altsWithDescriptions.contains(alt)) {
                    result3 += alt.toString() + "\n";
                    length3++;
                }
                if (oncogenicityMapping.containsKey(alt) && mutationEffectMapping.containsKey(alt) && referencesMapping.get(alt).size() == 0) {
                    result4 += alt.toString() + "\n";
                    length4++;
                }
                if (multipleMutationEffects.containsKey(alt) && multipleMutationEffects.get(alt).size() > 1) {
                    result5 += alt.toString() + "\n";
                    length5++;
                }

            }
            count++;
            System.out.println("Processing " + gene.getHugoSymbol() + "  " + 100 * count / genes.size() + "% finished");
        }
        String output = "Rule 1 check result: There are " + Integer.toString(length0) + " variants whose oncogenicty is different with its relevant variants.\n";
        output += result0;
        
        output += "******************************************************************************************\n";
        output += "Rule 2 check result: There are " + Integer.toString(length1) + " variants whose mutation effect is different with its relevant variants.\n";
        output += result1;

        output += "******************************************************************************************\n";
        output += "Rule 3 check result: There are " + Integer.toString(length2) + " variants that has unknown oncogenic and unknown mutation effect, and same for all its relevant variants.\n";
        output += result2;

        output += "******************************************************************************************\n";
        output += "Rule 4 check result: There are " + Integer.toString(length3) + " variants that don't have oncogenic, mutation effect and descriptions, and not in the VUS list.\n";
        output += result3;
        
        output += "******************************************************************************************\n";
        output += "Rule 5 check result: There are " + Integer.toString(length4) + " variants that have oncogenic and mutation effect, but don't have any PMIDs.\n";
        output += result4;

        output += "******************************************************************************************\n";
        output += "Rule 6 check result: There are " + Integer.toString(length5) + " variants that have multiple mutation effects (does not count relevant mutation effects)\n";
        output += result5;

        try {
            File file = new File("validation-result.txt");
            //if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file.getName());
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(output);
            bufferWritter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
