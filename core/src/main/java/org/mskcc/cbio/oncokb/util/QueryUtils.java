package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin Zhang on 8/23/17.
 */
public class QueryUtils {
    public static String getAlterationName(Query query) {
        String name = "";
        if (query != null) {
            if (!StringUtils.isNullOrEmpty(query.getAlteration())) {
                name = query.getAlteration().trim();
            }
            AlterationType alterationType = AlterationType.getByName(query.getAlterationType());
            if (alterationType != null) {
                if (alterationType.equals(AlterationType.FUSION) ||
                    (alterationType.equals(AlterationType.STRUCTURAL_VARIANT) &&
                        !StringUtils.isNullOrEmpty(query.getConsequence()) &&
                        query.getConsequence().equalsIgnoreCase("fusion"))) {
                    List<Gene> genes = FusionUtils.getGenes(query.getHugoSymbol());

                    if (genes.size() > 1) {
                        List<String> symbols = new ArrayList<>();
                        for (Gene gene : genes) {
                            symbols.add(gene.getHugoSymbol());
                        }
                        name = org.apache.commons.lang3.StringUtils.join(symbols, "-") + " Fusion";
                    } else if (genes.size() == 1) {
                        name = "Fusions";
                    }
                }
            }
        }
        return name;
    }
}
