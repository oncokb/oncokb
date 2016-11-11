package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Drug;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Hongxin on 11/09/16.
 */
public class DrugUtils {
    public static Set<Drug> getDrugsByNames(Set<String> names, Boolean fuzzy) {
        Set<Drug> result = new HashSet<>();
        if (fuzzy == null) {
            fuzzy = false;
        }
        if (names != null) {
            Set<Drug> drugs = getAllDrugs();
            for (Drug drug : drugs) {
                for (String name : names) {
                    if (stringInSet(Collections.singleton(drug.getDrugName()), name, fuzzy)) {
                        result.add(drug);
                    }
                }
            }
        }
        return result;
    }

    public static Set<Drug> getDrugsBySynonyms(Set<String> synonyms, Boolean fuzzy) {
        Set<Drug> result = new HashSet<>();
        if (fuzzy == null) {
            fuzzy = false;
        }
        if (synonyms != null) {
            Set<Drug> drugs = getAllDrugs();
            for (Drug drug : drugs) {
                for (String synonym : synonyms) {
                    if (stringInSet(drug.getSynonyms(), synonym, fuzzy)) {
                        result.add(drug);
                    }
                }
            }
        }
        return result;
    }

    public static Set<Drug> getDrugsBySAtcCodes(Set<String> atcCodes, Boolean fuzzy) {
        Set<Drug> result = new HashSet<>();
        if (fuzzy == null) {
            fuzzy = false;
        }
        if (atcCodes != null) {
            Set<Drug> drugs = getAllDrugs();
            for (Drug drug : drugs) {
                for (String actCode : atcCodes) {
                    if (stringInSet(drug.getAtcCodes(), actCode, fuzzy)) {
                        result.add(drug);
                    }
                }
            }
        }
        return result;
    }

    public static Set<Drug> getAllDrugs() {
        Set<Drug> drugs = new HashSet<>();
        if (CacheUtils.isEnabled()) {
            drugs = CacheUtils.getAllDrugs();
        } else {
            List<Drug> databaseDrugs = ApplicationContextSingleton.getDrugBo().findAll();
            if (databaseDrugs != null) {
                drugs = new HashSet<>(databaseDrugs);
            }
        }
        return drugs;
    }

    private static Boolean stringInSet(Set<String> strings, String search, Boolean fuzzy) {
        if (strings != null) {
            for (String string : strings) {
                if (fuzzy) {
                    if (StringUtils.containsIgnoreCase(string, search)) {
                        return true;
                    }
                } else {
                    if (string.equals(search)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
