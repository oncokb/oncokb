/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.scripts;

import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author zhangh2
 */

public class AllOncogenicMutationEffect {
    private AllOncogenicMutationEffect() {
        throw new AssertionError();
    }

    public static void main(String[] args) throws IOException {
//        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findGenesByHugoSymbol(Collections.singleton("ACVR1"));
        List<Gene> genes = ApplicationContextSingleton.getGeneBo().findAll();

//        System.out.println("Gene\tAlteration\tMutation Effect\tOncogenic");
        System.out.println("Gene\tAlteration\tEvidence type\tKnown effect\tShort Description\tDescription");
        for (Gene gene : genes) {
            List<Alteration> alterations = ApplicationContextSingleton.getAlterationBo().findAlterationsByGene(Collections.singleton(gene));
            List<Alteration> alterationsWithoutVUS = AlterationUtils.excludeVUS(alterations);
            for (Alteration alteration : alterationsWithoutVUS) {
                LinkedHashSet<Alteration> relevantAlts = ApplicationContextSingleton.getAlterationBo().findRelevantAlterations(alteration, alterations);
                Set<EvidenceType> evidenceTypes = new HashSet<>();
                evidenceTypes.add(EvidenceType.MUTATION_EFFECT);
                evidenceTypes.add(EvidenceType.ONCOGENIC);

                List<Evidence> relevantEvidences = EvidenceUtils.getEvidence(new ArrayList<>(relevantAlts), evidenceTypes, null);

                for(Evidence evidence : relevantEvidences) {
                    String knownEffect = evidence.getKnownEffect();

                    System.out.println(gene.getHugoSymbol() + "\t" + alteration.getAlteration() + "\t" + evidence.getEvidenceType() + "\t" + knownEffect + "\t" + evidence.getDescription());
                }
            }
        }
    }
}
