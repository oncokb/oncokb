package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.GeneNumber;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class NumberUtils {
    public static Set<GeneNumber> getGeneNumberList(Set<Gene> genes) {
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getEvidenceByGenes(genes);
        Set<GeneNumber> geneNumbers = new HashSet<>();

        Iterator it = evidences.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Gene, Set<Evidence>> pair = (Map.Entry) it.next();
            GeneNumber geneNumber = new GeneNumber();

            geneNumber.setShortGene(ShortGeneUtils.getShortGeneFromGene(pair.getKey()));

            LevelOfEvidence highestLevel = LevelUtils.getHighestLevelFromEvidence(pair.getValue());
            geneNumber.setHighestLevel(highestLevel != null ? highestLevel.name() : null);

            geneNumber.setAlteration(AlterationUtils.getAllAlterations(pair.getKey()).size());
            geneNumbers.add(geneNumber);
        }
        return geneNumbers;
    }

    public static Set<GeneNumber> getGeneNumberList() {
        return getGeneNumberList(GeneUtils.getAllGenes());
    }
}
