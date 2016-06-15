/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import java.io.IOException;
import java.util.*;

/**
 * @author zhangh2
 */

public class AlterationLevel {
    private AlterationLevel() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws IOException {
        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findAll();

        System.out.println("Gene\tAlteration\tLevel");
        for (Gene gene : genes) {
            List<Alteration> alterations = ApplicationContextSingleton.getAlterationBo().findAlterationsByGene(Collections.singleton(gene));
            Set<Alteration> alterationsWithoutVUS = AlterationUtils.excludeVUS(new HashSet<>(alterations));
            for (Alteration alteration : alterationsWithoutVUS) {
                List<Alteration> relevantAlts = ApplicationContextSingleton.getAlterationBo().findRelevantAlterations(alteration, alterations);
                List<Evidence> relevantEvidences = ApplicationContextSingleton.getEvidenceBo().findEvidencesByAlteration(relevantAlts);
//                LevelOfEvidence levelOfEvidence = LevelUtils.getHighestLevelFromEvidence(new HashSet<>(relevantEvidences));

                for (Evidence evidence : relevantEvidences) {
                    LevelOfEvidence level = evidence.getLevelOfEvidence();
                    TumorType tumorType = evidence.getTumorType();
                    String levelStr = "";

                    if (level != null && level.getLevel() != null) {
                        levelStr = level.getLevel().toUpperCase();
                    }

                    if (tumorType != null) {
                        String tumorTypeStr = tumorType.getName();
                        Set<Treatment> treatments = evidence.getTreatments();
                        List<String> treatmentNames = new ArrayList<>();

                        for (Treatment treatment : treatments) {
                            Set<Drug> drugs = treatment.getDrugs();
                            List<String> drugNames = new ArrayList<>();

                            for (Drug drug : drugs) {
                                if (drug.getDrugName() != null) {
                                    drugNames.add(drug.getDrugName());
                                }
                            }
                            String drugStr = StringUtils.join(drugNames, " + ");

                            treatmentNames.add(drugStr);
                        }

                        String treatmentStr = StringUtils.join(treatmentNames, ", ");
                        
                        if (treatmentStr != null && !treatmentStr.equals("")) {
                            System.out.println(gene.getHugoSymbol() + "\t" + alteration.getAlteration() + "\t" + tumorTypeStr + "\t" + levelStr + "\t" + treatmentStr);
                        }
                    }
                }

//                String level = "NA";
//
//                if (levelOfEvidence != null && levelOfEvidence.getLevel() != null) {
//                    level = levelOfEvidence.getLevel().toUpperCase();
//                }
            }
        }
    }
}
