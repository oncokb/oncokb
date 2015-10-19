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
     * @param entrezGeneIdStr
     * @param hugoSymbolStr
     * @param alterationStr
     * @param tumorTypeStr
     * @param consequenceStr
     * @param tumorTypeSource
     * @return
     */
    public static List<VariantQuery> getGeneAlterationTumorTypeConsequence(String entrezGeneIdStr, String hugoSymbolStr, String alterationStr, String tumorTypeStr, String consequenceStr, String tumorTypeSource) {
        List<VariantQuery> pairs = new ArrayList<>();

        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();

        String[] genes = null;
        String[] alterations = null;
        String[] tumorTypes = null;
        String[] consequences = null;

        int pairwiseLength = 0;

        //Number of variants should be exactly matched with each other.
        if(entrezGeneIdStr != null) {
            genes = entrezGeneIdStr.split(",");
        }else if(hugoSymbolStr != null){
            genes = hugoSymbolStr.split(",");
        }else {
            return pairs;
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
        for (String symbol : genes) {
            VariantQuery query = new VariantQuery();
            Gene gene = entrezGeneIdStr==null?geneBo.findGeneByHugoSymbol(symbol):geneBo.findGeneByEntrezGeneId(Integer.parseInt(symbol));
            query.setGene(gene);
            query.setQueryGene(symbol);
            pairs.add(query);
        }

        if (consequenceStr!=null) {
            for(int i = 0; i < pairwiseLength; i++) {
                pairs.get(i).setConsequence(consequences[i]);
            }
        }

        if (alterationStr!=null) {
            for(int i = 0; i < pairwiseLength; i++) {
                Alteration alteration = AlterationUtils.getAlteration((String) pairs.get(i).getQueryGene(), alterations[i], "MUTATION", (String) pairs.get(i).getConsequence(), null, null);
                pairs.get(i).setQueryAlteration(alteration == null ? alterations[i] : alteration.getAlteration());
                pairs.get(i).setAlterations(alterationBo.findRelevantAlterations(alteration, null));
            }
        }

        for(int i = 0; i < pairwiseLength; i++) {
            String tumorType = tumorTypes == null? null : tumorTypes[i];
            List<TumorType> relevantTumorTypes = new ArrayList<>();
            if (tumorTypeSource.equals("cbioportal")) {
                relevantTumorTypes.addAll(TumorTypeUtils.fromCbioportalTumorType(tumorType));
            } else {
                //By default, use quest tumor types
                relevantTumorTypes.addAll(TumorTypeUtils.fromQuestTumorType(tumorType));
            }
            pairs.get(i).setQueryTumorType(tumorType);
            pairs.get(i).setTumorTypes(relevantTumorTypes);
        }

        return pairs;
    }
}
