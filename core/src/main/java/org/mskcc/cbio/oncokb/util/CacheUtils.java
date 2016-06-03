package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.*;

import java.util.*;


/**
 * Created by Hongxin on 4/1/16.
 * <p/>
 * CacheUtils is used to manage cached variant summaries, relevant alterations, alterations which all gene based.
 * It also includes mapped tumor types which is based on query tumor type name + source.
 * <p/>
 * The GeneObservable manages all gene based caches. Any updates happen on gene will automatically trigger
 * GeneObservable to notify all observers to update relative cache.
 * <p/>
 * TODO:
 * Ideally, we should place cache functions in the cache BAO with a factory which controls the source of data.
 * In this way, user can easily to choose to get data from cache or database directly.
 */


public class CacheUtils {
    private static Map<String, Map<String, String>> variantSummary = new HashMap<>();
    private static Map<String, Map<String, String>> variantCustomizedSummary = new HashMap<>();
    private static Map<String, Map<String, List<Alteration>>> relevantAlterations = new HashMap<>();
    private static Map<String, List<Alteration>> alterations = new HashMap<>();
    private static Map<String, List<TumorType>> mappedTumorTypes = new HashMap<>();
    private static Map<Integer, Gene> genesByEntrezId = new HashMap<>();
    private static Map<String, Gene> genesByHugoSymbol = new HashMap<>();
    private static Map<String, Map<String, List<Evidence>>> relevantEvidences = new HashMap<>();
    private static Set<Gene> genes = new HashSet<>();
    private static Map<Gene, Set<Alteration>> VUS = new HashMap<>();
    private static Map<String, Object> numbers = new HashMap<>();
    private static Map<Gene, Set<Evidence>> evidences = new HashMap<>(); //Gene based evidences 
    private static String status = "enabled"; //Current cacheUtils status. Applicable value: disabled enabled

    private static Observer variantSummaryObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                variantSummary.remove(operation.get("val"));
            } else if (operation.get("cmd") == "reset") {
                variantSummary.clear();
            }
        }
    };

    private static Observer variantCustomizedSummaryObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                variantCustomizedSummary.remove(operation.get("val"));
            } else if (operation.get("cmd") == "reset") {
                variantCustomizedSummary.clear();
            }
        }
    };

    private static Observer relevantAlterationsObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                relevantAlterations.remove(operation.get("val"));
            } else if (operation.get("cmd") == "reset") {
                relevantAlterations.clear();
            }
        }
    };

    private static Observer numbersObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            numbers.clear();
        }
    };

    private static Observer VUSObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            Gene gene = GeneUtils.getGeneByHugoSymbol(operation.get("val"));
            if (operation.get("cmd") == "update" && gene != null) {
                VUS.remove(gene);
                VUS.put(gene, AlterationUtils.findVUSFromEvidences(EvidenceUtils.getEvidenceByGenes(Collections.singleton(gene)).get(gene)));
            } else if (operation.get("cmd") == "reset") {
                VUS.clear();
            }
        }
    };

    private static Observer alterationsObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                alterations.remove(operation.get("val"));
            } else if (operation.get("cmd") == "reset") {
                alterations.clear();
            }
        }
    };

    private static Observer geneObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                String gene = operation.get("val");
                genesByEntrezId.remove(gene);
                genesByHugoSymbol.remove(gene);
            } else if (operation.get("cmd") == "reset") {
                genesByEntrezId.clear();
                genesByHugoSymbol.clear();
            }
        }
    };

    private static Observer genesObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            genes = new HashSet<Gene>(geneBo.findAll());
        }
    };

    private static Observer relevantEvidencesObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                relevantEvidences.remove(operation.get("val"));
            } else if (operation.get("cmd") == "reset") {
                relevantEvidences.clear();
            }
        }
    };

    private static Observer evidencesObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                Gene gene = GeneUtils.getGeneByHugoSymbol(operation.get("val"));
                if(gene != null) {
                    evidences.remove(gene);
                    evidences.put(gene, new HashSet<>(ApplicationContextSingleton.getEvidenceBo().findEvidencesByGene(Collections.singleton(gene))));
                }
            } else if (operation.get("cmd") == "reset") {
                evidences.clear();
                evidences = EvidenceUtils.separateEvidencesByGene(genes, new HashSet<>(ApplicationContextSingleton.getEvidenceBo().findAll()));
            }
        }
    };

    static {
        try {
            GeneObservable.getInstance().addObserver(variantSummaryObserver);
            GeneObservable.getInstance().addObserver(variantCustomizedSummaryObserver);
            GeneObservable.getInstance().addObserver(relevantAlterationsObserver);
            GeneObservable.getInstance().addObserver(alterationsObserver);
            GeneObservable.getInstance().addObserver(geneObserver);
            GeneObservable.getInstance().addObserver(relevantEvidencesObserver);
            GeneObservable.getInstance().addObserver(genesObserver);
            GeneObservable.getInstance().addObserver(evidencesObserver);
            GeneObservable.getInstance().addObserver(VUSObserver);
            GeneObservable.getInstance().addObserver(numbersObserver);

            Long oldTime = new Date().getTime();
            genes = new HashSet<Gene>(ApplicationContextSingleton.getGeneBo().findAll());
            oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all genes");
            evidences = EvidenceUtils.separateEvidencesByGene(genes, new HashSet<>(ApplicationContextSingleton.getEvidenceBo().findAll()));
            oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all gene based evidences");
            for(Map.Entry<Gene, Set<Evidence>> entry : evidences.entrySet()) {
                setVUS(entry.getKey(), entry.getValue());
            }
            oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all VUS");

        }catch (Exception e) {
            System.out.println(e);
        }
    }

    public static Gene getGeneByEntrezId(Integer entrezId) {
        if (genesByEntrezId.containsKey(entrezId)) {
            return genesByEntrezId.get(entrezId);
        } else {
            return null;
        }
    }

    public static Boolean containGeneByEntrezId(Integer entrezId) {
        return genesByEntrezId.containsKey(entrezId) ? true : false;
    }

    public static void setGeneByEntrezId(Integer entrezId, Gene gene) {
        genesByEntrezId.put(entrezId, gene);
    }

    public static Gene getGeneByHugoSymbol(String hugoSymbol) {
        if (genesByHugoSymbol.containsKey(hugoSymbol)) {
            return genesByHugoSymbol.get(hugoSymbol);
        } else {
            return null;
        }
    }

    public static Boolean containGeneByHugoSymbol(String hugoSymbol) {
        return genesByHugoSymbol.containsKey(hugoSymbol) ? true : false;
    }

    public static void setGeneByHugoSymbol(String hugoSymbol, Gene gene) {
        genesByHugoSymbol.put(hugoSymbol, gene);
    }

    public static List<Evidence> getRelevantEvidences(String gene, String variant) {
        if (relevantEvidences.containsKey(gene) && relevantEvidences.get(gene).containsKey(variant)) {
            return relevantEvidences.get(gene).get(variant);
        } else {
            return null;
        }
    }

    public static Boolean containRelevantEvidences(String gene, String variant) {
        return (relevantEvidences.containsKey(gene) &&
            relevantEvidences.get(gene).containsKey(variant)) ? true : false;
    }

    public static void setVUS(Gene gene, Set<Evidence> evidences) {
        if (!VUS.containsKey(gene)) {
            VUS.put(gene, new HashSet<Alteration>());
        }
        VUS.put(gene, AlterationUtils.findVUSFromEvidences(evidences));
    }

    public static Set<Alteration> getVUS(Gene gene) {
        if (VUS.containsKey(gene)) {
            return VUS.get(gene);
        } else {
            return null;
        }
    }
    
    public static void setNumbers(String type, Object number) {
        numbers.put(type, number);
    }

    public static Object getNumbers(String type) {
        return numbers.get(type);
    }
    
    public static void setRelevantEvidences(String gene, String variant, List<Evidence> evidences) {
        if (!relevantEvidences.containsKey(gene)) {
            relevantEvidences.put(gene, new HashMap<String, List<Evidence>>());
        }
        relevantEvidences.get(gene).put(variant, evidences);
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

    public static String getVariantCustomizedSummary(String gene, String variant) {
        if (variantCustomizedSummary.containsKey(gene) && variantCustomizedSummary.get(gene).containsKey(variant)) {
            return variantCustomizedSummary.get(gene).get(variant);
        } else {
            return null;
        }
    }

    public static Boolean containVariantCustomizedSummary(String gene, String variant) {
        return (variantCustomizedSummary.containsKey(gene) &&
            variantCustomizedSummary.get(gene).containsKey(variant)) ? true : false;
    }

    public static void setVariantCustomizedSummary(String gene, String variant, String summary) {
        if (!variantCustomizedSummary.containsKey(gene)) {
            variantCustomizedSummary.put(gene, new HashMap<String, String>());
        }
        variantCustomizedSummary.get(gene).put(variant, summary);
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

    public static List<TumorType> getMappedTumorTypes(String queryTumorType, String source) {
        return mappedTumorTypes.get(queryTumorType + "&" + source);
    }

    public static Boolean containMappedTumorTypes(String queryTumorType, String source) {
        return mappedTumorTypes.containsKey(queryTumorType + "&" + source) ? true : false;
    }

    public static void setMappedTumorTypes(String queryTumorType, String source, List<TumorType> tumorTypes) {
        mappedTumorTypes.put(queryTumorType + "&" + source, tumorTypes);
    }

    public static Set<Gene> getAllGenes() {
        if (genes.size() == 0) {
            genes = new HashSet<>(ApplicationContextSingleton.getGeneBo().findAll());
        }
        return genes;
    }

    public static Set<Evidence> getEvidences(Gene gene) {
        if(evidences == null || evidences.size() == 0) {
            evidences = EvidenceUtils.separateEvidencesByGene(genes, new HashSet<>(ApplicationContextSingleton.getEvidenceBo().findAll()));
        }
        
        if (evidences.containsKey(gene)) {
            Set<Evidence> result = evidences.get(gene);
            return result;
        } else {
            return null;
        }
    }

    public static Boolean containEvidences(Gene gene) {
        return (evidences.containsKey(gene)) ? true : false;
    }

    public static void setEvidences(Gene gene, Set<Evidence> newEvidences) {
        evidences.put(gene, newEvidences);
    }

    public static Set<ShortGene> getAllShortGenes() {
        return ShortGeneUtils.getShortGenesFromGenes(genes);
    }

    public static void updateGene(String gene) {
        GeneObservable.getInstance().update("update", gene);
    }

    public static void resetAll() {
        GeneObservable.getInstance().update("reset", null);
    }

    public static void enableCacheUtils() {
        status = "enabled";
    }

    public static void disableCacheUtils() {
        status = "disabled";
    }

    public static String getCacheUtilsStatus() {
        return status;
    }

    public static Boolean isEnabled() {
        if (status == "enabled") {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean isDisabled() {
        if (status == "disabled") {
            return true;
        } else {
            return false;
        }
    }
}