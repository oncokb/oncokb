package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Query;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Created by Hongxin Zhang on 8/23/17.
 */
public class QueryUtils {
    public static String getAlterationName(Query query) {
        String name = "";
        if (query != null) {
            if (!StringUtils.isNullOrEmpty(query.getAlteration())) {
                name = query.getAlteration().trim();
            } else {
                AlterationType alterationType = AlterationType.getByName(query.getAlterationType());
                if (alterationType != null) {
                    if (alterationType.equals(AlterationType.FUSION) ||
                        (alterationType.equals(AlterationType.STRUCTURAL_VARIANT) &&
                            !StringUtils.isNullOrEmpty(query.getConsequence()) &&
                            query.getConsequence().equalsIgnoreCase("fusion"))) {
                        LinkedHashSet<String> genes = new LinkedHashSet<>(Arrays.asList(query.getHugoSymbol().split("-")));

                        if (genes.size() > 1) {
                            name = org.apache.commons.lang3.StringUtils.join(genes, "-") + " Fusion";
                        } else if (genes.size() == 1) {
                            name = "Fusions";
                        }
                    }
                }
            }
        }
        return name;
    }
}
