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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

    public static void main(String[] args) throws IOException {
        Map<Gene, Set<Evidence>> allGeneBasedEvidences = EvidenceUtils.getAllGeneBasedEvidences();
        Set<Gene> genes = GeneUtils.getAllGenes();
        Integer count = 0;
        String result1 = "", result2 = "", result3 = "";
        Integer length1 = 0, length2 = 0, length3 = 0;
        ArrayList<String> specialAlterations = new ArrayList<>(Arrays.asList("Inactivating Mutations", "Activating Mutations", "Fusions", "Inactivating", "Wildtype", "Amplification", "Fusions"));
        for (Gene gene : genes) {
            Set<Evidence> evidences = allGeneBasedEvidences.get(gene);
            Set<Alteration> VUSAlterations = AlterationUtils.findVUSFromEvidences(evidences);
            Map<Alteration, ArrayList<Alteration>> relevantAlterationsMapping = new HashMap<Alteration, ArrayList<Alteration>>();
            Map<Alteration, String> oncogenicityMapping = new HashMap<Alteration, String>();
            Map<Alteration, String> mutationEffectMapping = new HashMap<Alteration, String>();
            Set<Alteration> allVariants = new HashSet<Alteration>();
            Set<Alteration> allAlts = new HashSet<Alteration>();
            for (Evidence evidenceItem : evidences) {
                allVariants = evidenceItem.getAlterations();
                allAlts.addAll(allVariants);
                for (Alteration alterationItem : allVariants) {
                    relevantAlterationsMapping.put(alterationItem, (ArrayList<Alteration>) AlterationUtils.getRelevantAlterations(gene, alterationItem.getAlteration(), null, null, null));
                    if (evidenceItem.getEvidenceType().toString().equals("ONCOGENIC")) {
                        oncogenicityMapping.put(alterationItem, evidenceItem.getKnownEffect());
                    }
                    if (evidenceItem.getEvidenceType().toString().equals("MUTATION_EFFECT")) {
                        mutationEffectMapping.put(alterationItem, evidenceItem.getKnownEffect());
                    }
                }
            }
            for (Alteration alt : allAlts) {
                ArrayList<Alteration> relevantAlts = relevantAlterationsMapping.get(alt);
                for (Alteration relevantAlt : relevantAlts) {
                    if (oncogenicityMapping.containsKey(alt) && oncogenicityMapping.containsKey(relevantAlt) && !oncogenicityMapping.get(alt).equals(oncogenicityMapping.get(relevantAlt)) 
                            || mutationEffectMapping.containsKey(alt) && mutationEffectMapping.containsKey(relevantAlt) && !mutationEffectMapping.get(alt).equals(mutationEffectMapping.get(relevantAlt))) {
                        result1 += alt.toString() + "\n";
                        length1++;
                        break;
                    }
                }
                if (oncogenicityMapping.containsKey(alt) && oncogenicityMapping.get(alt).equals(Oncogenicity.UNKNOWN.getOncogenic()) 
                        && mutationEffectMapping.containsKey(alt) && mutationEffectMapping.get(alt).equals(MutationEffect.UNKNOWN.getMutation_effect())) {
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
                if (!oncogenicityMapping.containsKey(alt) && !mutationEffectMapping.containsKey(alt) && !VUSAlterations.contains(alt) && !specialAlterations.contains(alt.getAlteration())) {
                    result3 += alt.toString() + "\n";
                    length3++;
                }

            }
            count++;
            System.out.println("Processing " + gene.getHugoSymbol() + "  " + 100 * count / genes.size() + "% finished");
        }
        String output = "Rule 1 check result: There are " + Integer.toString(length1) + " variants whose oncogenicty or mutation effect is different with its relevant variants.\n";
        output += result1;

        output += "******************************************************************************************\n";
        output += "Rule 2 check result: There are " + Integer.toString(length2) + " variants that has unknown oncogenic and unknown mutation effect, and same for all its relevant variants.\n";
        output += result2;

        output += "******************************************************************************************\n";
        output += "Rule 3 check result: There are " + Integer.toString(length3) + " variants that don't have oncogenic and mutation effect, and not in the VUS list.\n";
        output += result3;

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
