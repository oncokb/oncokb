package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.VariantQuery;
import org.mskcc.oncotree.model.TumorType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin on 8/10/15.
 */
public class VariantPairUtils {

    /**
     * All four input should be separated by comma. If multi-variants will be included in single search, please use plus symbol(Only will be supported in consequence)
     *
     * @param entrezGeneId
     * @param hugoSymbolStr
     * @param alterationStr
     * @param tumorTypeStr
     * @param consequenceStr
     * @param tumorTypeSource
     * @return
     */
    public static List<VariantQuery> getGeneAlterationTumorTypeConsequence(
        Integer entrezGeneId, String hugoSymbolStr, String alterationStr, String tumorTypeStr,
        String consequenceStr, Integer proteinStartStr, Integer proteinEndStr, String tumorTypeSource) {
        List<VariantQuery> pairs = new ArrayList<>();

        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();

        if (tumorTypeSource == null) {
            tumorTypeSource = "quest";
        }

        VariantQuery query = new VariantQuery();
        Gene gene = GeneUtils.getGene(entrezGeneId, hugoSymbolStr);
        query.setGene(gene);

        query.setQueryAlteration(alterationStr);

        query.setQueryTumorType(tumorTypeStr);
        query.setConsequence(consequenceStr);
        query.setProteinEnd(proteinEndStr);
        query.setProteinStart(proteinStartStr);


        Alteration alt = AlterationUtils.getAlteration(query.getGene().getHugoSymbol(), query.getQueryAlteration(),
            null, query.getConsequence(), query.getProteinStart(), query.getProteinEnd());
        Alteration matchedAlt = alterationBo.findAlteration(alt.getGene(), alt.getAlterationType(), alt.getAlteration());
        query.setExactMatchAlteration(matchedAlt);
        query.setAlterations(
            new ArrayList<>(AlterationUtils.getRelevantAlterations(alt)
            )
        );

        List<TumorType> relevantTumorTypes = new ArrayList<>();
        if (tumorTypeStr != null) {
            relevantTumorTypes = TumorTypeUtils.getMappedOncoTreeTypesBySource(tumorTypeStr, tumorTypeSource);
        }
        query.setTumorTypes(relevantTumorTypes);

        pairs.add(query);

        return pairs;
    }
}
