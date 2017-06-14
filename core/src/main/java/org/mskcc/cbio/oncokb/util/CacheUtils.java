package org.mskcc.cbio.oncokb.util;

import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.oncotree.model.TumorType;

import java.io.IOException;
import java.util.*;
import com.mysql.jdbc.StringUtils;


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
    private static Map<Integer, Map<String, String>> variantSummary = new HashMap<>();
    private static Map<Integer, Map<String, String>> variantTumorTypeSummary = new HashMap<>();
    private static Map<Integer, Map<String, List<Integer>>> relevantAlterations = new HashMap<>();
    private static Map<Integer, Gene> genesByEntrezId = new HashMap<>();
    private static Map<String, Integer> hugoSymbolToEntrez = new HashMap<>();
    // The key would be entrezGeneId, variant name and evidence ID. -1 will be used to store gene irrelevant evidences.
    private static Map<Integer, Map<String, Set<Integer>>> relevantEvidences = new HashMap<>();

    private static Map<String, List<TumorType>> mappedTumorTypes = new HashMap<>();
    private static Map<String, List<TumorType>> allOncoTreeTypes = new HashMap<>(); //Tag by different categories. main or subtype
    private static Map<String, Object> numbers = new HashMap<>();

    private static String status = "enabled"; //Current cacheUtils status. Applicable value: disabled enabled

    // Cache data from database
    private static Set<Gene> genes = new HashSet<>();
    private static Set<Drug> drugs = new HashSet<>();
    private static Map<Integer, Set<Evidence>> evidences = new HashMap<>(); //Gene based evidences
    private static Map<Integer, Set<Alteration>> alterations = new HashMap<>(); //Gene based alterations
    private static Map<Integer, Set<Alteration>> VUS = new HashMap<>(); //Gene based VUSs

    // Other services which will be defined in the property cache.update separated by comma
    // Every time the observer is triggered, all other services will be triggered as well
    private static List<String> otherServices = new ArrayList<>();

    private static Observer variantSummaryObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                Integer entrezGeneId = Integer.parseInt(operation.get("val"));
                variantSummary.remove(entrezGeneId);
            } else if (operation.get("cmd") == "reset") {
                variantSummary.clear();
            }
        }
    };

    private static Observer variantTumorTypeSummaryObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                Integer entrezGeneId = Integer.parseInt(operation.get("val"));
                variantTumorTypeSummary.remove(entrezGeneId);
            } else if (operation.get("cmd") == "reset") {
                variantTumorTypeSummary.clear();
            }
        }
    };

    private static Observer relevantAlterationsObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                Integer entrezGeneId = Integer.parseInt(operation.get("val"));
                relevantAlterations.remove(entrezGeneId);
            } else if (operation.get("cmd") == "reset") {
                relevantAlterations.clear();
            }
        }
    };

    private static Observer numbersObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            numbers.clear();
        }
    };

    private static Observer VUSObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                Integer entrezGeneId = Integer.parseInt(operation.get("val"));
                VUS.remove(entrezGeneId);
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
                Integer entrezGeneId = Integer.parseInt(operation.get("val"));
                alterations.remove(entrezGeneId);
            } else if (operation.get("cmd") == "reset") {
                alterations.clear();
            }
        }
    };

    // Always update genes since everything is relying on this.
    private static Observer genesObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            cacheAllGenes();
        }
    };

    private static Observer drugsObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            drugs = new HashSet<>(ApplicationContextSingleton.getDrugBo().findAll());
        }
    };

    private static Observer relevantEvidencesObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                Integer entrezGeneId = Integer.parseInt(operation.get("val"));
                relevantEvidences.remove(entrezGeneId);
                //Remove gene irrelevant evidences
                relevantEvidences.remove(-1);
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
                Integer entrezGeneId = Integer.parseInt(operation.get("val"));
                evidences.remove(entrezGeneId);
            } else if (operation.get("cmd") == "reset") {
                evidences.clear();
                cacheAllEvidencesByGenes();
            }
        }
    };

    private static Observer allCancerTypesObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            allOncoTreeTypes.put("main", TumorTypeUtils.getOncoTreeCancerTypes(ApplicationContextSingleton.getEvidenceBo().findAllCancerTypes()));
            allOncoTreeTypes.put("subtype", TumorTypeUtils.getOncoTreeSubtypesByCode(ApplicationContextSingleton.getEvidenceBo().findAllSubtypes()));
        }
    };

    private static void notifyOtherServices(String cmd, Integer entrezGeneId) {
        if (cmd == null) {
            cmd = "";
        }
        System.out.println("Notify other services..." + " at " + MainUtils.getCurrentTime());
        if (cmd == "update" && entrezGeneId != null) {
            Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);
            if (gene != null) {
                for (String service : otherServices) {
                    if(!StringUtils.isNullOrEmpty(service)) {
                        HttpUtils.postRequest(service + "?cmd=updateGene&hugoSymbol=" +
                            gene.getHugoSymbol(), "", true);
                    }
                }
            }
        } else if (cmd == "reset") {
            for (String service : otherServices) {
                if (!StringUtils.isNullOrEmpty(service)) {
                    HttpUtils.postRequest(service + "?cmd=reset", "", true);
                }
            }
        }
    }

    static {
        try {
            Long current = MainUtils.getCurrentTimestamp();
            GeneObservable.getInstance().addObserver(variantSummaryObserver);
            GeneObservable.getInstance().addObserver(variantTumorTypeSummaryObserver);
            GeneObservable.getInstance().addObserver(relevantAlterationsObserver);
            GeneObservable.getInstance().addObserver(alterationsObserver);
            GeneObservable.getInstance().addObserver(relevantEvidencesObserver);
            GeneObservable.getInstance().addObserver(allCancerTypesObserver);
            GeneObservable.getInstance().addObserver(genesObserver);
            GeneObservable.getInstance().addObserver(evidencesObserver);
            GeneObservable.getInstance().addObserver(VUSObserver);
            GeneObservable.getInstance().addObserver(numbersObserver);
            GeneObservable.getInstance().addObserver(drugsObserver);

            if (status.equals("enabled")) {
                System.out.println("Observer: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());

                cacheAllGenes();

                setAllAlterations();

                current = MainUtils.getCurrentTimestamp();
                drugs = new HashSet<>(ApplicationContextSingleton.getDrugBo().findAll());
                System.out.println("Cache all drugs: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
                current = MainUtils.getCurrentTimestamp();

                Set<Evidence> geneEvidences = new HashSet<>(ApplicationContextSingleton.getEvidenceBo().findAll());
                System.out.println("Get all evidences: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
                current = MainUtils.getCurrentTimestamp();

                Map<Gene, Set<Evidence>> mappedEvidence = EvidenceUtils.separateEvidencesByGene(genes, geneEvidences);

                System.out.println("Separate all evidences: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
                current = MainUtils.getCurrentTimestamp();

                Iterator it = mappedEvidence.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Gene, Set<Evidence>> pair = (Map.Entry) it.next();
                    evidences.put(pair.getKey().getEntrezGeneId(), pair.getValue());
                }
                System.out.println("Cache all evidences: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
                current = MainUtils.getCurrentTimestamp();

                for (Map.Entry<Integer, Set<Evidence>> entry : evidences.entrySet()) {
                    setVUS(entry.getKey(), entry.getValue());
                }
                System.out.println("Cache all VUSs: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
            } else {
                System.out.println("CacheUtil is disabled at " + MainUtils.getCurrentTime());
            }
            current = MainUtils.getCurrentTimestamp();

            allOncoTreeTypes.put("main", TumorTypeUtils.getOncoTreeCancerTypes(ApplicationContextSingleton.getEvidenceBo().findAllCancerTypes()));
            allOncoTreeTypes.put("subtype", TumorTypeUtils.getOncoTreeSubtypesByCode(ApplicationContextSingleton.getEvidenceBo().findAllSubtypes()));

            System.out.println("Cache all tumor types: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
            current = MainUtils.getCurrentTimestamp();

            HotspotUtils.getHotspots();
            System.out.println("Cache all hotspots: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
            current = MainUtils.getCurrentTimestamp();

            registerOtherServices();
            System.out.println("Register other services: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
            current = MainUtils.getCurrentTimestamp();
        } catch (Exception e) {
            System.out.println(e + " at " + MainUtils.getCurrentTime());
        }
    }

    private static void registerOtherServices() throws IOException {
        String services = PropertiesUtils.getProperties("cache.update");
        if (services != null) {
            otherServices = Arrays.asList(services.split(","));
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

    public static void setGeneByEntrezId(Gene gene) {
        if (gene != null) {
            genesByEntrezId.put(gene.getEntrezGeneId(), gene);
            hugoSymbolToEntrez.put(gene.getHugoSymbol(), gene.getEntrezGeneId());
        }
    }

    private static void cacheAllGenes() {
        Long current = MainUtils.getCurrentTimestamp();

        genes = new HashSet<>(ApplicationContextSingleton.getGeneBo().findAll());
        genesByEntrezId = new HashedMap();
        hugoSymbolToEntrez = new HashedMap();
        for (Gene gene : genes) {
            genesByEntrezId.put(gene.getEntrezGeneId(), gene);
            hugoSymbolToEntrez.put(gene.getHugoSymbol(), gene.getEntrezGeneId());
        }

        System.out.println("Cache all genes: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
    }

    public static Gene getGeneByHugoSymbol(String hugoSymbol) {
        Integer entrezGeneId = hugoSymbolToEntrez.get(hugoSymbol);
        if (entrezGeneId == null)
            return null;

        return genesByEntrezId.get(entrezGeneId);
    }

    public static Boolean containGeneByHugoSymbol(String hugoSymbol) {
        Integer entrezGeneId = hugoSymbolToEntrez.get(hugoSymbol);
        if (entrezGeneId == null)
            return false;

        if (genesByEntrezId.get(entrezGeneId) == null) {
            return false;
        } else {
            return true;
        }
    }

    public static Set<Evidence> getRelevantEvidences(Integer entrezGeneId, String variant) {
        if (relevantEvidences.containsKey(entrezGeneId) && relevantEvidences.get(entrezGeneId).containsKey(variant)) {
            Set<Integer> mappedEntrez = relevantEvidences.get(entrezGeneId).get(variant);
            Set<Evidence> geneEvidences = new HashSet<>();
            Set<Evidence> mappedEvidences = new HashSet<>();

            if (entrezGeneId == -1) {
                for (Map.Entry<Integer, Set<Evidence>> map : evidences.entrySet()) {
                    geneEvidences.addAll(map.getValue());
                }
            } else {
                geneEvidences = getEvidences(genesByEntrezId.get(entrezGeneId));
            }
            for (Evidence evidence : geneEvidences) {
                if (mappedEntrez.contains(evidence.getId())) {
                    mappedEvidences.add(evidence);
                }
            }
            return mappedEvidences;
        } else {
            return new HashSet<>();
        }
    }

    public static Boolean containRelevantEvidences(Integer entrezGeneId, String variant) {
        return (relevantEvidences.containsKey(entrezGeneId) &&
            relevantEvidences.get(entrezGeneId).containsKey(variant)) ? true : false;
    }

    private static void setVUS(Integer entrezGeneId, Set<Evidence> evidences) {
        if (!VUS.containsKey(entrezGeneId)) {
            VUS.put(entrezGeneId, new HashSet<Alteration>());
        }
        VUS.put(entrezGeneId, AlterationUtils.findVUSFromEvidences(evidences));
    }

    public static Set<Alteration> getVUS(Integer entrezGeneId) {
        if (entrezGeneId == null) {
            return new HashSet<>();
        }
        if (VUS.containsKey(entrezGeneId)) {
            return VUS.get(entrezGeneId);
        } else {
            Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);
            if (gene != null) {
                synEvidences();
            }
            return VUS.get(entrezGeneId);
        }
    }

    public static void setNumbers(String type, Object number) {
        numbers.put(type, number);
    }

    public static Object getNumbers(String type) {
        return numbers.get(type);
    }

    public static void setRelevantEvidences(Integer entrezGeneId, String variant, Set<Evidence> evidences) {
        if (!relevantEvidences.containsKey(entrezGeneId)) {
            relevantEvidences.put(entrezGeneId, new HashMap<String, Set<Integer>>());
        }
        Set<Integer> mappedEvidenceIds = new HashSet<>();
        for (Evidence evidence : evidences) {
            mappedEvidenceIds.add(evidence.getId());
        }
        relevantEvidences.get(entrezGeneId).put(variant, mappedEvidenceIds);
    }

    public static String getVariantSummary(Integer entrezGeneId, String variant) {
        if (variantSummary.containsKey(entrezGeneId) && variantSummary.get(entrezGeneId).containsKey(variant)) {
            return variantSummary.get(entrezGeneId).get(variant);
        } else {
            return null;
        }
    }

    public static Boolean containVariantSummary(Integer entrezGeneId, String variant) {
        return (variantSummary.containsKey(entrezGeneId) &&
            variantSummary.get(entrezGeneId).containsKey(variant)) ? true : false;
    }

    public static void setVariantSummary(Integer entrezGeneId, String variant, String summary) {
        if (!variantSummary.containsKey(entrezGeneId)) {
            variantSummary.put(entrezGeneId, new HashMap<String, String>());
        }
        variantSummary.get(entrezGeneId).put(variant, summary);
    }

    public static String getVariantTumorTypeSummary(Integer entrezGeneId, String key) {
        if (variantTumorTypeSummary.containsKey(entrezGeneId)
            && variantTumorTypeSummary.get(entrezGeneId).containsKey(key)) {
            return variantTumorTypeSummary.get(entrezGeneId).get(key);
        } else {
            return null;
        }
    }

    public static Boolean containVariantTumorTypeSummary(Integer entrezGeneId, String key) {
        return variantTumorTypeSummary.containsKey(entrezGeneId)
            && variantTumorTypeSummary.get(entrezGeneId).containsKey(key);
    }

    public static void setVariantTumorTypeSummary(Integer entrezGeneId, String key, String summary) {
        if (!variantTumorTypeSummary.containsKey(entrezGeneId)) {
            variantTumorTypeSummary.put(entrezGeneId, new HashMap<String, String>());
        }
        variantTumorTypeSummary.get(entrezGeneId).put(key, summary);
    }

    public static List<Alteration> getRelevantAlterations(Integer entrezGeneId, String variant) {
        if (relevantAlterations.containsKey(entrezGeneId) && relevantAlterations.get(entrezGeneId).containsKey(variant)) {
            List<Integer> mappedAltsIds = relevantAlterations.get(entrezGeneId).get(variant);
            List<Alteration> mappedAlts = new ArrayList<>();
            Set<Alteration> geneAlts = getAlterations(entrezGeneId);

            // Need to keep the order of mappedAltsIds
            for (Integer mappedAlt : mappedAltsIds) {
                for (Alteration alteration : geneAlts) {
                    if (mappedAlt.equals(alteration.getId())) {
                        mappedAlts.add(alteration);
                    }
                }
            }

            return mappedAlts;
        } else {
            return new ArrayList<>();
        }
    }

    public static Boolean containRelevantAlterations(Integer entrezGeneId, String variant) {
        return (relevantAlterations.containsKey(entrezGeneId) &&
            relevantAlterations.get(entrezGeneId).containsKey(variant)) ? true : false;
    }

    public static void setRelevantAlterations(Integer entrezGeneId, String variant, List<Alteration> alts) {
        if (!relevantAlterations.containsKey(entrezGeneId)) {
            relevantAlterations.put(entrezGeneId, new HashMap<String, List<Integer>>());
        }
        List<Integer> mappedAltsIds = new ArrayList<>();
        for (Alteration alteration : alts) {
            mappedAltsIds.add(alteration.getId());
        }
        relevantAlterations.get(entrezGeneId).put(variant, mappedAltsIds);
    }

    public static Set<Alteration> getAlterations(Integer entrezGeneId) {
        synAlterations();
        Set<Alteration> result = alterations.get(entrezGeneId);
        if (result == null) {
            result = new HashSet<>();
        }
        return result;
    }

    public static Boolean containAlterations(Integer entrezGeneId) {
        synAlterations();
        return alterations.containsKey(entrezGeneId) ? true : false;
    }

    public static void setAlterations(Gene gene) {
        if (gene != null && genes.contains(gene)) {
            alterations.put(gene.getEntrezGeneId(), new HashSet<>(ApplicationContextSingleton.getAlterationBo().findAlterationsByGene(Collections.singleton(gene))));
        }
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

    public static List<TumorType> getAllCancerTypes() {
        if (isEnabled()) {
            return allOncoTreeTypes.get("main");
        } else {
            return TumorTypeUtils.getOncoTreeCancerTypes(ApplicationContextSingleton.getEvidenceBo().findAllCancerTypes());
        }
    }

    public static List<TumorType> getAllSubtypes() {
        if (isEnabled()) {
            return allOncoTreeTypes.get("subtype");
        } else {
            return TumorTypeUtils.getOncoTreeSubtypesByCode(ApplicationContextSingleton.getEvidenceBo().findAllSubtypes());
        }
    }

    public static Set<Gene> getAllGenes() {
        if (genes.size() == 0) {
            genes = new HashSet<>(ApplicationContextSingleton.getGeneBo().findAll());
        }
        return genes;
    }

    private static void setAllAlterations() {
        Long current = MainUtils.getCurrentTimestamp();
        List<Alteration> allAlterations = ApplicationContextSingleton.getAlterationBo().findAll();

        for (Alteration alteration : allAlterations) {
            Gene gene = alteration.getGene();
            if (!alterations.containsKey(gene.getEntrezGeneId())) {
                alterations.put(gene.getEntrezGeneId(), new HashSet<Alteration>());
            }
            alterations.get(gene.getEntrezGeneId()).add(alteration);
        }
        System.out.println("Cache all alterations: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
    }

    public static Set<Drug> getAllDrugs() {
        if (drugs.size() == 0) {
            drugs = new HashSet<>(ApplicationContextSingleton.getDrugBo().findAll());
        }
        return drugs;
    }

    public static Set<Evidence> getEvidences(Gene gene) {
        if (gene == null) {
            return new HashSet<>();
        }

        synEvidences();

        if (evidences.containsKey(gene.getEntrezGeneId())) {
            Set<Evidence> result = evidences.get(gene.getEntrezGeneId());
            return result;
        } else {
            return new HashSet<>();
        }
    }

    public static Set<Evidence> getEvidencesByIds(Set<Integer> ids) {
        synEvidences();

        Set<Evidence> mappedEvis = new HashSet<>();
        if (ids != null) {
            for (Map.Entry<Integer, Set<Evidence>> map : evidences.entrySet()) {
                for (Evidence evidence : map.getValue()) {
                    if (ids.contains(evidence.getId())) {
                        mappedEvis.add(evidence);
                    }
                }
            }
        }
        return mappedEvis;
    }

    public static Set<Evidence> getEvidencesByUUID(String uuid) {
        synEvidences();

        Set<Evidence> mappedEvis = new HashSet<>();
        if (uuid != null) {
            for (Map.Entry<Integer, Set<Evidence>> map : evidences.entrySet()) {
                for (Evidence evidence : map.getValue()) {
                    if (uuid.equals(evidence.getUuid())) {
                        mappedEvis.add(evidence);
                    }
                }
            }
        }
        return mappedEvis;
    }

    public static Set<Evidence> getEvidencesByUUIDs(Set<String> uuids) {
        synEvidences();

        Set<Evidence> mappedEvis = new HashSet<>();
        if (uuids != null) {
            for (Map.Entry<Integer, Set<Evidence>> map : evidences.entrySet()) {
                for (Evidence evidence : map.getValue()) {
                    if (evidence != null && uuids.contains(evidence.getUuid())) {
                        mappedEvis.add(evidence);
                    }
                }
            }
        }
        return mappedEvis;
    }

    private static void setEvidences(Gene gene) {
        evidences.put(gene.getEntrezGeneId(), new HashSet<>(ApplicationContextSingleton.getEvidenceBo().findEvidencesByGene(Collections.singleton(gene))));
    }

    private static void synEvidences() {
        Long current = MainUtils.getCurrentTimestamp();
        if (evidences == null || evidences.size() == 0) {
            cacheAllEvidencesByGenes();
        }

        if (evidences.keySet().size() != genes.size()) {
            for (Gene gene : genes) {
                if (!evidences.containsKey(gene.getEntrezGeneId())) {
                    setEvidences(gene);
                    setVUS(gene.getEntrezGeneId(), getEvidences(gene));
                }
            }
        }
    }

    private static void synAlterations() {
        Long current = MainUtils.getCurrentTimestamp();
        if (alterations == null || alterations.size() == 0) {
            setAllAlterations();
        }

        if (alterations.keySet().size() != genes.size()) {
            for (Gene gene : genes) {
                if (!alterations.containsKey(gene.getEntrezGeneId())) {
                    setAlterations(gene);
                }
            }
        }
    }

    public static void updateGene(Integer entrezGeneId) {
        try {
            System.out.println("Update gene on instance " + PropertiesUtils.getProperties("app.name") + " at " + MainUtils.getCurrentTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
        GeneObservable.getInstance().update("update", entrezGeneId.toString());
        notifyOtherServices("update", entrezGeneId);
    }

    public static void updateGene(Integer entrezGeneId, Boolean propagate) {
        try {
            System.out.println("Update gene on instance " + PropertiesUtils.getProperties("app.name") + " at " + MainUtils.getCurrentTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (propagate == null) {
            propagate = false;
        }
        GeneObservable.getInstance().update("update", entrezGeneId.toString());
        if (propagate) {
            notifyOtherServices("update", entrezGeneId);
        }
    }

    public static void resetAll() {
        try {
            System.out.println("Reset all genes cache on instance " + PropertiesUtils.getProperties("app.name") + " at " + MainUtils.getCurrentTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
        GeneObservable.getInstance().update("reset", null);
        notifyOtherServices("reset", null);
    }

    public static void resetAll(Boolean propagate) {
        try {
            System.out.println("Reset all genes cache on instance " + PropertiesUtils.getProperties("app.name") + " at " + MainUtils.getCurrentTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
        GeneObservable.getInstance().update("reset", null);
        if (propagate == null) {
            propagate = false;
        }
        if (propagate) {
            notifyOtherServices("reset", null);
        }
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

    private static void cacheAllEvidencesByGenes() {
        Long current = MainUtils.getCurrentTimestamp();

        Map<Gene, Set<Evidence>> mappedEvidence =
            EvidenceUtils.separateEvidencesByGene(genes, new HashSet<>(
                ApplicationContextSingleton.getEvidenceBo().findAll()));
        Iterator it = mappedEvidence.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Gene, Set<Evidence>> pair = (Map.Entry) it.next();
            evidences.put(pair.getKey().getEntrezGeneId(), pair.getValue());
        }
        System.out.println("Cache all evidences by gene: " + MainUtils.getTimestampDiff(current) + " at " + MainUtils.getCurrentTime());
    }
}
