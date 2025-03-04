package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.apiModels.download.DownloadAvailability;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.health.InMemoryCacheSizes;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by Hongxin on 4/1/16.
 *
 * CacheUtils is used to manage cached variant summaries, relevant alterations, alterations which all gene based.
 * It also includes mapped tumor types which is based on query tumor type name.
 *
 * The GeneObservable manages all gene based caches. Any updates happen on gene will automatically trigger
 * GeneObservable to notify all observers to update relative cache.
 *
 * TODO:
 * Ideally, we should place cache functions in the cache BAO with a factory which controls the source of data.
 * In this way, user can easily to choose to get data from cache or database directly.
 */


public class CacheUtils {
    private static Map<Integer, Gene> genesByEntrezId = new HashMap<>();
    private static Map<String, Integer> hugoSymbolToEntrez = new HashMap<>();

    private static List<CancerGene> cancerGeneList = null;
    private static Map<String, Object> numbers = new HashMap<>();

    private static List<DownloadAvailability> downloadAvailabilities = new ArrayList<>();

    private static Integer allEvidencesSize;

    // Cache data from database
    private static Set<Gene> genes = new HashSet<>();
    private static Set<Drug> drugs = new HashSet<>();
    private static Map<Integer, List<Evidence>> evidences = new HashMap<>(); //Gene based evidences
    private static Map<Integer, Map<Integer, Set<TumorType>>> evidenceRelevantCancerTypes = new HashedMap();
    private static Map<Integer, List<Alteration>> alterations = new HashMap<>(); //Gene based alterations
    private static Map<Integer, Map<ReferenceGenome, List<Alteration>>> alterationsByReferenceGenome = new HashMap<>(); //Gene based alterations
    private static Map<Integer, Set<Alteration>> VUS = new HashMap<>(); //Gene based VUSs

    private static List<TumorType> cancerTypes = new ArrayList<>();
    private static Map<String, TumorType> cancerTypesByCode = new HashMap<>();
    private static Map<String, TumorType> cancerTypesByMainType = new HashMap<>();
    private static Map<String, TumorType> cancerTypesByLowercaseSubtype = new HashMap<>();
    private static List<TumorType> subtypes = new ArrayList<>();
    private static List<TumorType> mainTypes = new ArrayList<>();
    private static List<TumorType> specialCancerTypes = new ArrayList<>();

    // Other services which will be defined in the property cache.update separated by comma
    // Every time the observer is triggered, all other services will be triggered as well
    private static List<String> otherServices = new ArrayList<>();

    private static Map<String, Long> recordTime = new HashedMap();

    private static Info oncokbInfo;

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
                alterationsByReferenceGenome.remove(entrezGeneId);
            } else if (operation.get("cmd") == "reset") {
                alterations.clear();
                alterationsByReferenceGenome.clear();
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

    private static Observer evidencesObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Map<String, String> operation = (Map<String, String>) arg;
            if (operation.get("cmd") == "update") {
                Integer entrezGeneId = Integer.parseInt(operation.get("val"));
                evidences.remove(entrezGeneId);
                evidenceRelevantCancerTypes.remove(entrezGeneId);
                Gene gene = ApplicationContextSingleton.getGeneBo().findGeneByEntrezGeneId(entrezGeneId);
                if (gene != null) {
                    setEvidences(gene);
                }
            } else if (operation.get("cmd") == "reset") {
                evidences.clear();
                evidenceRelevantCancerTypes.clear();
                cacheAllEvidencesByGenes();
            }
        }
    };

    public static InMemoryCacheSizes getCurrentCacheSizes() {

        return new InMemoryCacheSizes(
            genes.size(),
            alterations.values().stream().mapToInt(List::size).sum(),
            drugs.size(),
            cancerTypes.size(),
            allEvidencesSize
        );
    }

    private static String getCacheCompletionMessage(Long startTime) {
        Long diffInMilliseconds = MainUtils.getTimestampDiff(startTime);
        return "(Completed in " + diffInMilliseconds + "ms" + " at " + MainUtils.getCurrentTime() + ")";
    }

    private static void notifyOtherServices(String cmd, Set<Integer> entrezGeneIds) throws IOException {
        System.out.println("Notify other services..." + " at " + MainUtils.getCurrentTime());
        if (cmd == null) {
            cmd = "";
        }
        System.out.println("\tcmd is " + cmd);
        if (cmd == "update" && entrezGeneIds != null && entrezGeneIds.size() > 0) {
            System.out.println("\t# of other services" + otherServices.size());
            for (String service : otherServices) {
                if (!StringUtils.isNullOrEmpty(service)) {
                    HttpUtils.postRequest(service + "?cmd=updateGene&entrezGeneIds=" +
                        org.apache.commons.lang3.StringUtils.join(entrezGeneIds, ","), "");
                }
            }
        } else if (cmd == "reset") {
            for (String service : otherServices) {
                if (!StringUtils.isNullOrEmpty(service)) {
                    HttpUtils.postRequest(service + "?cmd=reset", "");
                }
            }
        } else {
            System.out.println("\tcmd=" + cmd + ", has gene:" + entrezGeneIds != null && entrezGeneIds.size() > 0);
        }
    }

    static {
        try {
            Long current = MainUtils.getCurrentTimestamp();
            GeneObservable.getInstance().addObserver(alterationsObserver);
            GeneObservable.getInstance().addObserver(genesObserver);
            GeneObservable.getInstance().addObserver(evidencesObserver);
            GeneObservable.getInstance().addObserver(VUSObserver);
            GeneObservable.getInstance().addObserver(numbersObserver);
            GeneObservable.getInstance().addObserver(drugsObserver);

            System.out.println("Add observers " + getCacheCompletionMessage(current));

            cacheAllGenes();

            setAllAlterations();

            current = MainUtils.getCurrentTimestamp();
            drugs = new HashSet<>(ApplicationContextSingleton.getDrugBo().findAll());
            System.out.println("Cached " + drugs.size() + " drugs " + CacheUtils.getCacheCompletionMessage(current));
            current = MainUtils.getCurrentTimestamp();

            cancerTypes = ApplicationContextSingleton.getTumorTypeBo().findAll();
            cancerTypes.stream().forEach(ct -> {
                if (!StringUtils.isNullOrEmpty(ct.getCode())) {
                    cancerTypesByCode.put(ct.getCode(), ct);
                }
                if (StringUtils.isNullOrEmpty(ct.getCode()) && !StringUtils.isNullOrEmpty(ct.getMainType())) {
                    cancerTypesByMainType.put(ct.getMainType().toLowerCase(), ct);
                }
                if (!StringUtils.isNullOrEmpty(ct.getSubtype())) {
                    cancerTypesByLowercaseSubtype.put(ct.getSubtype().toLowerCase(), ct);
                }
            });
            System.out.println("Cached " + cancerTypes.size() +  " tumor types " + CacheUtils.getCacheCompletionMessage(current));
            subtypes = cancerTypes.stream().filter(tumorType -> org.apache.commons.lang3.StringUtils.isNotEmpty(tumorType.getCode()) && tumorType.getLevel() > 0).collect(Collectors.toList());
            System.out.println("Cached " + subtypes.size() +  " tumor sub types " + CacheUtils.getCacheCompletionMessage(current));
            mainTypes = cancerTypes.stream().filter(tumorType -> org.apache.commons.lang3.StringUtils.isEmpty(tumorType.getCode()) || tumorType.getLevel() > 0).collect(Collectors.toList());
            System.out.println("Cached " + mainTypes.size() + " tumor main types " + CacheUtils.getCacheCompletionMessage(current));
            current = MainUtils.getCurrentTimestamp();

            specialCancerTypes = Arrays.stream(SpecialTumorType.values()).map(specialTumorType -> cancerTypes.stream().filter(cancerType -> !StringUtils.isNullOrEmpty(cancerType.getMainType()) && cancerType.getMainType().equals(specialTumorType.getTumorType())).findAny().orElse(null)).filter(cancerType -> cancerType != null).collect(Collectors.toList());
            System.out.println("Cached " + specialCancerTypes.size() + " special tumor types " + CacheUtils.getCacheCompletionMessage(current));

            current = MainUtils.getCurrentTimestamp();
            synEvidences();
            System.out.println("Cached " + allEvidencesSize + " evidences " + CacheUtils.getCacheCompletionMessage(current));
            current = MainUtils.getCurrentTimestamp();

            for (Map.Entry<Integer, List<Evidence>> entry : evidences.entrySet()) {
                setVUS(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            System.out.println("Cached " + VUS.size() + " VUSs " + CacheUtils.getCacheCompletionMessage(current));
            current = MainUtils.getCurrentTimestamp();

            NamingUtils.cacheAllAbbreviations();
            System.out.println("Cached abbreviation ontology " + CacheUtils.getCacheCompletionMessage(current));
            current = MainUtils.getCurrentTimestamp();

            cacheDownloadAvailability();
            System.out.println("Cached downloadable files availability on github " + CacheUtils.getCacheCompletionMessage(current));

            oncokbInfo = ApplicationContextSingleton.getInfoBo().get();
            System.out.println("Cached oncokb info " + CacheUtils.getCacheCompletionMessage(current));

            registerOtherServices();
            System.out.println("Register other services " + CacheUtils.getCacheCompletionMessage(current));
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

    public static Info getInfo() {
        return oncokbInfo;
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
        cancerGeneList = null;
        System.out.println("Cached "+ genesByEntrezId.size() + " genes " + CacheUtils.getCacheCompletionMessage(current));
    }

    public static List<CancerGene> getCancerGeneList() throws IOException {
        if(cancerGeneList == null) {
            updateCancerGeneList();
        }
        return cancerGeneList;
    }

    private static void updateCancerGeneList() throws IOException {
        cancerGeneList = CancerGeneUtils.populateCancerGeneList();
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

    private static void setVUS(Integer entrezGeneId, Set<Evidence> evidences) {
        VUS.put(entrezGeneId, AlterationUtils.findVUSFromEvidences(evidences));
    }

    public static Set<Alteration> getVUS(Integer entrezGeneId) {
        if (entrezGeneId == null) {
            return new HashSet<>();
        }
        if (VUS.containsKey(entrezGeneId)) {
            return VUS.get(entrezGeneId) == null ? new HashSet<Alteration>() : Collections.unmodifiableSet(VUS.get(entrezGeneId));
        } else {
            Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);
            if (gene != null) {
                synEvidences();
                setVUS(entrezGeneId, new HashSet<>(getEvidences(gene)));
            }
            return VUS.get(entrezGeneId) == null ? new HashSet<Alteration>() : Collections.unmodifiableSet(VUS.get(entrezGeneId));
        }
    }

    public static void setNumbers(String type, Object number) {
        numbers.put(type, number);
    }

    public static Object getNumbers(String type) {
        return numbers.get(type);
    }

    public static List<Alteration> getAlterations(Integer entrezGeneId, ReferenceGenome referenceGenome) {
        synAlterations();
        List<Alteration> result = referenceGenome == null ? alterations.get(entrezGeneId) : (alterationsByReferenceGenome.get(entrezGeneId) == null ? null : alterationsByReferenceGenome.get(entrezGeneId).get(referenceGenome));
        if (result == null) {
            return new ArrayList<>();
        }else {
            return Collections.unmodifiableList(result);
        }
    }

    public static Set<Alteration> findRelevantOverlapAlterations(Gene gene, ReferenceGenome referenceGenome, VariantConsequence consequence, int start, int end, String proteinChange) {
        return AlterationUtils.findOverlapAlteration(getAlterations(gene.getEntrezGeneId(), referenceGenome), gene, referenceGenome, consequence, start, end, proteinChange);
    }

    public static Set<Alteration> findMutationsByConsequenceAndPositionOnSamePosition(Gene gene, ReferenceGenome referenceGenome, VariantConsequence consequence, int start, int end, String referenceResidue) {
        Set<Alteration> alterations = new HashSet<>();
        for (Alteration alteration : getAlterations(gene.getEntrezGeneId(), referenceGenome)) {
            if (AlterationUtils.consequenceRelated(alteration.getConsequence(), consequence)
                && (referenceGenome == null || alteration.getReferenceGenomes().contains(referenceGenome))
                && alteration.getProteinStart().equals(alteration.getProteinEnd())
                && alteration.getProteinStart() >= start
                && alteration.getProteinStart() <= end
                && (alteration.getRefResidues() == null || referenceResidue == null || referenceResidue.equals(alteration.getRefResidues()))
            ) {
                alterations.add(alteration);
            }
        }
        return alterations;
    }

    public static Boolean containAlterations(Integer entrezGeneId) {
        synAlterations();
        return alterations.containsKey(entrezGeneId) ? true : false;
    }

    public static void setAlterations(Gene gene) {
        if (gene != null && genes.contains(gene)) {
            List<Alteration> geneAlterations = ApplicationContextSingleton.getAlterationBo().findAlterationsByGene(Collections.singleton(gene));
            if (!alterationsByReferenceGenome.containsKey(gene.getEntrezGeneId())) {
                alterationsByReferenceGenome.put(gene.getEntrezGeneId(), new HashMap<>());
            }
            alterations.put(gene.getEntrezGeneId(), geneAlterations);
            for (Alteration alteration : geneAlterations) {
                for (ReferenceGenome refGenome : alteration.getReferenceGenomes()) {
                    if (!alterationsByReferenceGenome.get(gene.getEntrezGeneId()).containsKey(refGenome)) {
                        alterationsByReferenceGenome.get(gene.getEntrezGeneId()).put(refGenome, new ArrayList<>());
                    }
                    alterationsByReferenceGenome.get(gene.getEntrezGeneId()).get(refGenome).add(alteration);
                }
            }
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
                alterations.put(gene.getEntrezGeneId(), new ArrayList<>());
            }
            if (!alterationsByReferenceGenome.containsKey(gene.getEntrezGeneId())) {
                alterationsByReferenceGenome.put(gene.getEntrezGeneId(), new HashMap<>());
            }
            alterations.get(gene.getEntrezGeneId()).add(alteration);

            for (ReferenceGenome refGenome : alteration.getReferenceGenomes()) {
                if (!alterationsByReferenceGenome.get(gene.getEntrezGeneId()).containsKey(refGenome)) {
                    alterationsByReferenceGenome.get(gene.getEntrezGeneId()).put(refGenome, new ArrayList<>());
                }
                alterationsByReferenceGenome.get(gene.getEntrezGeneId()).get(refGenome).add(alteration);
            }
        }
        System.out.println("Cached " + allAlterations.size() + " alterations " + CacheUtils.getCacheCompletionMessage(current));
    }

    public static Set<Drug> getAllDrugs() {
        if (drugs.size() == 0) {
            drugs = new HashSet<>(ApplicationContextSingleton.getDrugBo().findAll());
        }
        return drugs;
    }

    public static Drug getPersistentDrug(Drug drug) {
        if (drug == null)
            return null;

        for (Drug persistent : drugs) {
            if (persistent.equals(drug))
                return persistent;
        }
        return null;
    }

    public static void addDrug(Drug drug) {
        if (drug != null) {
            drugs.add(drug);
        }
    }

    public static Set<Evidence> getAllEvidences() {
        Set<Evidence> evis = new HashSet<>();
        for (Map.Entry<Integer, List<Evidence>> map : evidences.entrySet()) {
            evis.addAll(map.getValue());
        }
        return evis;
    }

    public static List<Evidence> getEvidences(Gene gene) {
        if (gene == null) {
            return new ArrayList<>();
        }

        synEvidences();

        if (evidences.containsKey(gene.getEntrezGeneId())) {
            return evidences.get(gene.getEntrezGeneId()) == null ? new ArrayList<>() : Collections.unmodifiableList(evidences.get(gene.getEntrezGeneId()));
        } else {
            return new ArrayList<>();
        }
    }

    public static Set<Evidence> getEvidencesByIds(Set<Integer> ids) {
        synEvidences();

        Set<Evidence> mappedEvis = new HashSet<>();
        if (ids != null) {
            for (Map.Entry<Integer, List<Evidence>> map : evidences.entrySet()) {
                for (Evidence evidence : map.getValue()) {
                    if (ids.contains(evidence.getId())) {
                        mappedEvis.add(evidence);
                    }
                }
            }
        }
        return mappedEvis;
    }

    public static Set<Evidence> getEvidencesByGenesAndIds(Set<Gene> genes, Set<Integer> ids) {
        synEvidences();

        Set<Evidence> mappedEvis = new HashSet<>();
        if (ids != null) {
            Set<Evidence> evidences = new HashSet<>();
            for (Gene gene : genes) {
                evidences.addAll(getEvidences(gene));
            }
            for (Evidence evidence : evidences) {
                if (ids.contains(evidence.getId())) {
                    mappedEvis.add(evidence);
                }
            }
        }
        return mappedEvis;
    }

    public static Set<Evidence> getEvidencesByUUID(String uuid) {
        synEvidences();

        Set<Evidence> mappedEvis = new HashSet<>();
        if (uuid != null) {
            for (Map.Entry<Integer, List<Evidence>> map : evidences.entrySet()) {
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
            for (Map.Entry<Integer, List<Evidence>> map : evidences.entrySet()) {
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
        List<Evidence> geneEvidences = ApplicationContextSingleton.getEvidenceBo().findEvidencesByGeneFromDB(Collections.singleton(gene));
        evidences.put(gene.getEntrezGeneId(), geneEvidences);
        updateEvidenceRelevantCancerTypes(gene.getEntrezGeneId(), geneEvidences);
    }

    private static void synEvidences() {
        Long current = MainUtils.getCurrentTimestamp();
        if (evidences == null || evidences.size() == 0) {
            cacheAllEvidencesByGenes();
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

    public static TumorType findTumorTypeByCode(String code) {
        return cancerTypes.stream().filter(cancerType -> !StringUtils.isNullOrEmpty(cancerType.getCode()) && cancerType.getCode().equals(code)).findFirst().orElse(null);
    }

    public static List<TumorType> getAllCancerTypes() {
        return cancerTypes.stream().collect(Collectors.toList());
    }
    public static List<TumorType> getAllMainTypes() {
        return mainTypes.stream().collect(Collectors.toList());
    }
    public static List<TumorType> getAllSubtypes() {
        return subtypes.stream().collect(Collectors.toList());
    }

    public static Map<String, TumorType> getCodedTumorTypeMap() {
        return cancerTypesByCode;
    }

    public static Map<String, TumorType> getLowercaseSubtypeTumorTypeMap() {
        return cancerTypesByLowercaseSubtype;
    }

    public static Map<String, TumorType> getMainTypeTumorTypeMap() {
        return cancerTypesByMainType;
    }

    public static List<TumorType> getAllSpecialCancerTypes() {
        return specialCancerTypes;
    }

    public static void forceUpdateGeneAlterations(Integer entrezGeneId) {
        alterations.remove(entrezGeneId);
        alterationsByReferenceGenome.remove(entrezGeneId);
    }

    public static void updateGene(Set<Integer> entrezGeneIds, Boolean propagate) throws IOException {
        System.out.println("Update gene on instance " + PropertiesUtils.getProperties("app.name") + " at " + MainUtils.getCurrentTime());
        if (propagate == null) {
            propagate = false;
        }
        if(entrezGeneIds == null || entrezGeneIds.size() == 0){
            System.out.println("\tThere is no entrez gene ids specified.");
            return;
        }
        entrezGeneIds.forEach(entrezGeneId -> GeneObservable.getInstance().update("update", entrezGeneId.toString()));
        if (propagate) {
            notifyOtherServices("update", entrezGeneIds);
        }else{
            System.out.println("\tDo not propagate.");
        }
    }

    public static void resetAll() throws IOException {
        System.out.println("Reset all genes cache on instance " + PropertiesUtils.getProperties("app.name") + " at " + MainUtils.getCurrentTime());
        GeneObservable.getInstance().update("reset", null);
        notifyOtherServices("reset", null);
    }

    public static void resetAll(Boolean propagate) throws IOException {
        System.out.println("Reset all genes cache on instance " + PropertiesUtils.getProperties("app.name") + " at " + MainUtils.getCurrentTime());
        GeneObservable.getInstance().update("reset", null);
        if (propagate == null) {
            propagate = false;
        }
        if (propagate) {
            notifyOtherServices("reset", null);
        }
    }


    private static void cacheAllEvidencesByGenes() {
        Long current = MainUtils.getCurrentTimestamp();
        List<Evidence> allEvidences = ApplicationContextSingleton.getEvidenceBo().findAll();
        allEvidencesSize = allEvidences.size();

        Map<Gene, List<Evidence>> mappedEvidence = EvidenceUtils.separateEvidencesByGene(genes, new HashSet<>(allEvidences));
        Iterator it = mappedEvidence.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Gene, List<Evidence>> pair = (Map.Entry) it.next();
            int entrezGeneId = pair.getKey().getEntrezGeneId();
            evidences.put(entrezGeneId, pair.getValue());
            updateEvidenceRelevantCancerTypes(entrezGeneId, pair.getValue());
        }
        System.out.println("Cached all evidences of " + mappedEvidence.size() + " genes" + CacheUtils.getCacheCompletionMessage(current));
    }

    public static void updateEvidenceRelevantCancerTypes(Integer entrezGeneId, List<Evidence> geneEvidences) {
        evidenceRelevantCancerTypes.put(entrezGeneId, new HashMap<>());
        for (Evidence evidence : geneEvidences) {
            if (evidence.getId() != null) {
                evidenceRelevantCancerTypes.get(entrezGeneId).put(evidence.getId(), TumorTypeUtils.findEvidenceRelevantCancerTypes(evidence));
            }
        }
    }

    public static Set<TumorType> getEvidenceRelevantCancerTypes(Integer entrezGeneId, Integer evidenceId) {
        if (entrezGeneId == null || evidenceId == null) {
            return null;
        }
        if (evidenceRelevantCancerTypes.containsKey(entrezGeneId) && evidenceRelevantCancerTypes.get(entrezGeneId).containsKey(evidenceId)) {
            return evidenceRelevantCancerTypes.get(entrezGeneId).get(evidenceId);
        } else {
            return null;
        }
    }

    public static Map<String, Long> getRecordTime() {
        return recordTime;
    }

    public static void emptyRecordTime() {
        recordTime = new HashedMap();
    }

    public static void addRecordTime(String key, Long time) {
        if (!recordTime.containsKey(key))
            recordTime.put(key, (long) 0);
        recordTime.put(key, recordTime.get(key) + time);
    }

    public static List<DownloadAvailability> getDownloadAvailabilities() {
        return downloadAvailabilities;
    }

    private static void cacheDownloadAvailability() {
        try {
            downloadAvailabilities = GitHubUtils.getDownloadAvailability();
        } catch (IOException e) {
            System.out.println("There is an issue connecting to GitHub.");
        } catch (NoPropertyException exception) {
            System.out.println("The data access token is not available");
        }
    }
}
