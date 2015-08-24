package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;
/**
 * Created by Hongxin on 8/10/15.
 */
public class VariantPairUtils {

    /**
     * All four input should be separated by comma. If multi-variants will be included in single search, please use plus symbol(Only will be supported in consequence)
     *
     * @param geneStr
     * @param alterationStr
     * @param tumorTypeStr
     * @param consequenceStr
     * @return Pairwise variants
     */
    public static List<Map<String, Object>> getGeneAlterationTumorTypeConsequence(String geneStr, String alterationStr, String tumorTypeStr, String consequenceStr, String tumorTypeSource) {
        List<Map<String, Object>> pairs = new ArrayList<>();

        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();

        String[] genes = null;
        String[] alterations = null;
        String[] tumorTypes = null;
        String[] consequences = null;

        int pairwiseLength = 0;

        //Number of variants should be exactly matched with each other.
        if(geneStr != null) {
            genes = geneStr.split(",");
        }
        if(alterationStr != null) {
            alterations = alterationStr.split(",");
            if(alterations.length != genes.length) {
                return null;
            }
        }
        if(tumorTypeStr != null) {
            tumorTypes = tumorTypeStr.split(",");
            if(tumorTypes.length == 1) {
                String tumorType = tumorTypes[0];
                tumorTypes = new String[genes.length];
                Arrays.fill(tumorTypes, tumorType);
            } else if(tumorTypes.length != genes.length) {
                return null;
            }
        }
        if(consequenceStr != null) {
            consequences = consequenceStr.split(",");
            if(consequences.length != genes.length) {
                return null;
            }
        }

        if(tumorTypeSource == null) {
            tumorTypeSource = "quest";
        }

        pairwiseLength = genes.length;

        //Organise data into List Map structure
        if (geneStr!=null) {
            for (String symbol : genes) {
                Map<String, Object> variantPair = new HashMap<>();
                Gene gene = geneBo.findGeneByHugoSymbol(symbol);
                variantPair.put("queryGene", symbol);
                variantPair.put("gene", gene);
                pairs.add(variantPair);
            }
        }

        if (consequenceStr!=null) {
            for(int i = 0; i < pairwiseLength; i++) {
                pairs.get(i).put("consequence", consequences[i]);
            }
        }

        if (alterationStr!=null) {
            for(int i = 0; i < pairwiseLength; i++) {
                Alteration alteration = AlterationUtils.getAlteration((String) pairs.get(i).get("queryGene"), alterations[i], "MUTATION", (String) pairs.get(i).get("consequence"), null, null);
                pairs.get(i).put("queryAlt", alteration == null? alterations[i]: alteration.getAlteration());
                pairs.get(i).put("alterations",alterationBo.findRelevantAlterations(alteration));
            }
        }

        for(int i = 0; i < pairwiseLength; i++) {
            String tumorType = tumorTypes == null? null : tumorTypes[i];
            Set<TumorType> relevantTumorTypes = new HashSet<>();
            if (tumorTypeSource.equals("cbioportal")) {
                relevantTumorTypes.addAll(TumorTypeUtils.fromCbioportalTumorType(tumorType));
            } else if (tumorTypeSource.equals("quest")) {
                relevantTumorTypes.addAll(TumorTypeUtils.fromQuestTumorType(tumorType));
            }
            pairs.get(i).put("queryTumorType", tumorType);
            pairs.get(i).put("tumorTypes", relevantTumorTypes);
        }

        return pairs;
    }
}
