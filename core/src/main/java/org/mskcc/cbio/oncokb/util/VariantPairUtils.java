package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.OncoTreeType;
import org.mskcc.cbio.oncokb.model.VariantQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public static List<VariantQuery> getGeneAlterationTumorTypeConsequence(
            String entrezGeneIdStr, String hugoSymbolStr, String alterationStr, String tumorTypeStr,
            String consequenceStr, String proteinStartStr, String proteinEndStr, String tumorTypeSource) {
        List<VariantQuery> pairs = new ArrayList<>();

        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();

        String[] genes = null;
        String[] alterations = null;
        String[] tumorTypes = null;
        String[] consequences = null;
        List<Integer> proteinStarts = new ArrayList<>();
        List<Integer> proteinEnds = new ArrayList<>();

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
        if(proteinStartStr != null) {
            for(String item : proteinStartStr.split(",")) {
                proteinStarts.add(Integer.parseInt(item));
            }
            if(proteinStarts.size() != genes.length) {
                return null;
            }
        }
        if(proteinEndStr != null) {
            for(String item : proteinEndStr.split(",")) {
                proteinEnds.add(Integer.parseInt(item));
            }
            if(proteinEnds.size() != genes.length) {
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

        if (proteinStartStr!=null) {
            for(int i = 0; i < proteinStarts.size(); i++) {
                pairs.get(i).setProteinStart(proteinStarts.get(i));
            }
        }

        if (proteinEndStr!=null) {
            for(int i = 0; i < proteinEnds.size(); i++) {
                pairs.get(i).setProteinEnd(proteinEnds.get(i));
            }
        }

        if (alterationStr!=null) {
            for(int i = 0; i < pairwiseLength; i++) {
                VariantQuery query = pairs.get(i);
                pairs.get(i).setQueryAlteration(alterations[i]);
                pairs.get(i).setAlterations(
                        AlterationUtils.getRelevantAlterations(query.getGene(), query.getQueryAlteration(),
                                query.getConsequence(), query.getProteinStart(), query.getProteinEnd())
                );
            }
        }

        for(int i = 0; i < pairwiseLength; i++) {
            String tumorType = tumorTypes == null? null : tumorTypes[i];
            List<OncoTreeType> relevantTumorTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(tumorType, tumorTypeSource);
            pairs.get(i).setQueryTumorType(tumorType);
            pairs.get(i).setTumorTypes(relevantTumorTypes);
        }

        return pairs;
    }
}
