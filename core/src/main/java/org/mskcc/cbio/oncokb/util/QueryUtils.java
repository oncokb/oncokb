package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.Query;

/**
 * Created by Hongxin on 4/14/17.
 */
public class QueryUtils {
    public static Boolean isFusionQuery(Query query) {
        if (query.getHugoSymbol() != null
            && query.getAlterationType() != null &&
            query.getAlterationType().equalsIgnoreCase("fusion")) {
            return true;
        }
        return false;
    }
}
