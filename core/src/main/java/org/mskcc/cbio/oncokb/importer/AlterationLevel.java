/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.LevelUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                LevelOfEvidence levelOfEvidence = LevelUtils.getHighestLevelFromEvidence(new HashSet<>(relevantEvidences));
                String level = "NA";

                if (levelOfEvidence != null && levelOfEvidence.getLevel() != null) {
                    level = levelOfEvidence.getLevel().toUpperCase();
                }
                System.out.println(gene.getHugoSymbol() + "\t" + alteration.getAlteration() + "\t" + level);
            }
        }
    }
}
