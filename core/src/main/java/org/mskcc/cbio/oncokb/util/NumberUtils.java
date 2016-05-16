package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.*;

import java.util.*;
import java.util.logging.Level;

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

    public static Set<LevelNumber> getLevelNumberList() {
        Set<Gene> genes = GeneUtils.getAllGenes();
        Map<Gene, Set<Evidence>> evidences = EvidenceUtils.getEvidenceByGenes(genes);
        
        Map<LevelOfEvidence, Set<Gene>> levelNumbers = new HashMap<>();
        Set<LevelNumber> levelList = new HashSet<>();
        
        for(Map.Entry<Gene, Set<Evidence>> entry : evidences.entrySet()) {
            LevelOfEvidence levelOfEvidence = LevelUtils.getHighestLevelFromEvidence(entry.getValue());
            
            if(!levelNumbers.containsKey(levelOfEvidence)) {
                levelNumbers.put(levelOfEvidence, new HashSet<Gene>());
            }
            levelNumbers.get(levelOfEvidence).add(entry.getKey());
        }
            
        for(Map.Entry<LevelOfEvidence, Set<Gene>> entry : levelNumbers.entrySet()) {
            LevelNumber levelNumber = new LevelNumber();
            levelNumber.setLevel(entry.getKey());
            levelNumber.setGenes(entry.getValue());
            levelList.add(levelNumber);
        }
        
        return levelList;
    }
    public static Set<GeneNumber> getGeneNumberList() {
        return getGeneNumberList(GeneUtils.getAllGenes());
    }
}
