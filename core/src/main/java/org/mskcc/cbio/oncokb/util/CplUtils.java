package org.mskcc.cbio.oncokb.util;

import static org.mskcc.cbio.oncokb.util.SummaryUtils.*;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.TumorType;

// This file stores all logics related to OncoKB Curation Programming Language
// See https://sop.oncokb.org/ Chapter 6, Protocol 8.
public class CplUtils {

    /**
     * Annotate CPL on gene level
     *
     * @param text       Text to annotate
     * @param hugoSymbol Gene hugo symbol which will be used to replace [[gene]]
     * @return
     */
    public static String annotateGene(String text, String hugoSymbol) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        return text.replace("[[gene]]", hugoSymbol);
    }

    /**
     * Annotate CPL
     *
     * @param text             Text to annotate
     * @param queryHugoSymbol  hugo symbol in query
     * @param queryAlteration  alteration in query
     * @param queryCancerType  cancer type in query
     * @param referenceGenome  reference genome this alteration belongs to. null if no preference
     * @param gene             Gene model that can be linked to queryHugoSymbol
     * @param matchedTumorType TumorType model that can be linked to queryCancerType
     * @return
     */
    public static String annotate(String text, String queryHugoSymbol, String queryAlteration, String queryCancerType, ReferenceGenome referenceGenome, Gene gene, TumorType matchedTumorType) {
        if (StringUtils.isEmpty(text))
            return "";

        // normalize queries
        if (queryHugoSymbol == null)
            queryHugoSymbol = "";
        if (queryAlteration == null)
            queryAlteration = "";
        if (queryCancerType == null)
            queryCancerType = "";


        String altName = getGeneMutationNameInTumorTypeSummary(gene, referenceGenome, queryHugoSymbol, queryAlteration);
        String alterationName = getGeneMutationNameInVariantSummary(gene, referenceGenome, queryHugoSymbol, queryAlteration);
        String tumorTypeName = convertTumorTypeNameInSummary(matchedTumorType == null ? queryCancerType : (StringUtils.isEmpty(matchedTumorType.getSubtype()) ? matchedTumorType.getMainType() : matchedTumorType.getSubtype()));

        if (tumorTypeName == null)
            tumorTypeName = "";

        String variantStr = altName;
        if (StringUtils.isNotEmpty(tumorTypeName)) {
            if (queryAlteration.contains("deletion")) {
                variantStr = tumorTypeName + " harboring a " + altName;
            } else {
                variantStr += " " + tumorTypeName;
            }
        }
        text = text.replace("[[variant]]", variantStr);
        text = text.replace("[[gene]] [[mutation]] [[[mutation]]]", alterationName);

        // In case of miss typed
        text = text.replace("[[gene]] [[mutation]] [[mutation]]", alterationName);
        text = text.replace("[[gene]] [[mutation]] [[mutant]]", altName);
        text = text.replace("[[gene]] [[mutation]] [[[mutant]]]", altName);

        // If the mutation already includes the gene name, we should skip the gene
        if (text.contains("[[gene]] [[mutation]]") && queryAlteration.toLowerCase().contains(queryHugoSymbol.toLowerCase())) {
            text = text.replace("[[gene]]", "");
        }

        text = text.replace("[[gene]]", queryHugoSymbol);

        // Improve false tolerance. Curators often use hugoSymbol directly instead of [[gene]]
        String specialLocationAlt = queryHugoSymbol + " [[mutation]] [[[mutation]]]";
        if (text.contains(specialLocationAlt)) {
            text = text.replace(specialLocationAlt, alterationName);
        }
        specialLocationAlt = queryHugoSymbol + " [[mutation]] [[mutation]]";
        if (text.contains(specialLocationAlt)) {
            text = text.replace(specialLocationAlt, alterationName);
        }
        specialLocationAlt = queryHugoSymbol + " [[mutation]] [[mutant]]";
        if (text.contains(specialLocationAlt)) {
            text = text.replace(specialLocationAlt, altName);
        }

        text = text.replace("[[mutation]] [[mutant]]", altName);
        text = text.replace("[[mutation]] [[[mutation]]]", alterationName);
        // In case of miss typed
        text = text.replace("[[mutation]] [[mutation]]", queryAlteration);
        text = text.replace("[[mutation]]", queryAlteration);
        if (StringUtils.isNotEmpty(tumorTypeName)) {
            text = text.replace("[[tumorType]]", tumorTypeName);
            text = text.replace("[[tumor type]]", tumorTypeName);
        }
        text = text.replace("[[fusion name]]", altName);
        text = text.replace("[[fusion name]]", altName);
        // Replace all whitespace except newlines
        return text.trim().replaceAll("[^\\S\\n]+", " ");
    }
}
