package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.*;


/**
 * Created by Hongxin on 4/1/16.
 */
public class CacheUtils {
    private static Map<String, Map<String, String>> variantSummary = new HashMap<>();
    private static Map<String, Map<String, List<Alteration>>> relevantAlterations = new HashMap<>();
    private static Map<String, List<Alteration>> alterations = new HashMap<>();
    private static Map<String, List<TumorType>> mappedTumorTypes = new HashMap<>();

    private static Observer variantSummaryObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            variantSummary.remove((String) arg);
        }
    };
    private static Observer relevantAlterationsObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            relevantAlterations.remove((String) arg);
        }
    };
    private static Observer alterationsObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            alterations.remove((String) arg);
        }
    };

    static {
        GeneObservable.getInstance().addObserver(variantSummaryObserver);
        GeneObservable.getInstance().addObserver(relevantAlterationsObserver);
        GeneObservable.getInstance().addObserver(alterationsObserver);
    }

    public static String getVariantSummary(String gene, String variant) {
        if (variantSummary.containsKey(gene) && variantSummary.get(gene).containsKey(variant)) {
            return variantSummary.get(gene).get(variant);
        } else {
            return null;
        }
    }

    public static Boolean containVariantSummary(String gene, String variant) {
        return (variantSummary.containsKey(gene) &&
                variantSummary.get(gene).containsKey(variant)) ? true : false;
    }

    public static void setVariantSummary(String gene, String variant, String summary) {
        if (!variantSummary.containsKey(gene)) {
            variantSummary.put(gene, new HashMap<String, String>());
        }
        variantSummary.get(gene).put(variant, summary);
    }

    public static List<Alteration> getRelevantAlterations(String gene, String variant) {
        if (relevantAlterations.containsKey(gene) && relevantAlterations.get(gene).containsKey(variant)) {
            return relevantAlterations.get(gene).get(variant);
        } else {
            return null;
        }
    }

    public static Boolean containRelevantAlterations(String gene, String variant) {
        return (relevantAlterations.containsKey(gene) &&
                relevantAlterations.get(gene).containsKey(variant)) ? true : false;
    }

    public static void setRelevantAlterations(String gene, String variant, List<Alteration> alts) {
        if (!relevantAlterations.containsKey(gene)) {
            relevantAlterations.put(gene, new HashMap<String, List<Alteration>>());
        }
        relevantAlterations.get(gene).put(variant, alts);
    }

    public static List<Alteration> getAlterations(String gene) {
        return alterations.get(gene);
    }

    public static Boolean containAlterations(String gene) {
        return alterations.containsKey(gene) ? true : false;
    }

    public static void setAlterations(String gene, List<Alteration> alts) {
        alterations.put(gene, alts);
    }

    public static List<TumorType> getMappedTumorTypes(String query) {
        return mappedTumorTypes.get(query);
    }

    public static Boolean containMappedTumorTypes(String query) {
        return mappedTumorTypes.containsKey(query) ? true : false;
    }

    public static void setMappedTumorTypes(String query, List<TumorType> tumorTypes) {
        mappedTumorTypes.put(query, tumorTypes);
    }

    public static void updateGene(String gene) {
        GeneObservable.getInstance().update(gene);
    }
}