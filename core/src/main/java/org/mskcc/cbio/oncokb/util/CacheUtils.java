package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.apiModels.download.DownloadAvailability;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.health.InMemoryCacheSizes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtils.class);
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

    static {
        try {
            Long current = MainUtils.getCurrentTimestamp();
            GeneObservable.getInstance().addObserver(alterationsObserver);
            GeneObservable.getInstance().addObserver(genesObserver);
            GeneObservable.getInstance().addObserver(evidencesObserver);
            GeneObservable.getInstance().addObserver(VUSObserver);
            GeneObservable.getInstance().addObserver(numbersObserver);
            GeneObservable.getInstance().addObserver(drugsObserver);
            LOGGER.info("Add observers {}", getCacheCompletionMessage(current));
        } catch (Exception e) {
            LOGGER.error("Unexpected Error", e);
        }
    }

    public static void initializeCaches() {
        // NOTE: This assumes a single caller at startup. If multiple callers are introduced,
        // add a guard to prevent concurrent initialization.
        try {
            initializeCachesInternal();
        } catch (Exception e) {
            LOGGER.error("Cache initialization failed", e);
        }
    }

    public static InMemoryCacheSizes getCurrentCacheSizes() {

        return new InMemoryCacheSizes(
            genes.size(),
            new HashMap<>(alterations).values().stream().mapToInt(List::size).sum(),
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
        LOGGER.info("Notify other services...");
        if (cmd == null) {
            cmd = "";
        }
        LOGGER.info("cmd is {}", cmd);
        if (cmd == "update" && entrezGeneIds != null && entrezGeneIds.size() > 0) {
            LOGGER.info("# of other services {}", otherServices.size());
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
            LOGGER.info("cmd={}, has gene: {}", cmd, entrezGeneIds != null && entrezGeneIds.size() > 0);
        }
    }

    private static void initializeCachesInternal() {
        LOGGER.info("Cache initialization starting");

        AtomicInteger count = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(
            getInitializationParallelism(),
            r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setName("cache-init-" + count.incrementAndGet());
                return t;
            }
        );

        try {
            cacheAllGenes();
            cacheOncokbInfo();
            cacheAbbreviations();
            cacheDownloadAvailability();
            LOGGER.info("Cache initialization parallelism={}", getInitializationParallelism());
            Map<String, CompletableFuture<?>> tasks = new LinkedHashMap<>();

            tasks.put("alterations", submitWithTransaction(executor, "alterations", CacheUtils::setAllAlterations));

            CompletableFuture<Void> evidencesFuture =
                    submitWithTransaction(executor, "evidences", CacheUtils::cacheAllEvidencesByGenes);
            tasks.put("evidences", evidencesFuture);
            tasks.put("VUS",
                    evidencesFuture.thenRunAsync(() -> {
                        LOGGER.info("Initialization task 'VUS' starting");
                        cacheVusFromEvidences();
                        LOGGER.info("Initialization task 'VUS' completed");
                    }, executor).whenComplete((ignored, ex) -> {
                        if (ex != null) {
                            Throwable unwrapped = unwrapCompletionException(ex);
                            if (unwrapped instanceof CancellationException) {
                                LOGGER.info("Initialization task 'VUS' cancelled");
                            } else {
                                LOGGER.error("Initialization task 'VUS' failed", unwrapped);
                            }
                        }
                    })
            );

            tasks.put("drugs", submitWithTransaction(executor, "drugs", CacheUtils::cacheAllDrugs));
            tasks.put("tumorTypes", submitWithTransaction(executor, "tumorTypes", CacheUtils::cacheAllTumorTypes));
            tasks.put("registerOtherServices", submitTask(executor, "registerOtherServices", CacheUtils::registerOtherServicesTask));

            CompletableFuture<Void> all =
                    CompletableFuture.allOf(tasks.values().toArray(new CompletableFuture[0]));

            waitForFuture("cacheInitializationAll", all);

            boolean hasFailure = tasks.values().stream().anyMatch(CompletableFuture::isCompletedExceptionally);

            if (!hasFailure) {
                LOGGER.info("Cache initialization completed");
            } else {
                LOGGER.error("Cache initialization completed with failures");
            }
        } finally {
            executor.shutdown();
        }
    }

    private static int getInitializationParallelism() {
        String configured = PropertiesUtils.getProperties("cache.initialization.parallelism");
        if (configured != null) {
            try {
                int parsed = Integer.parseInt(configured);
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid cache.initialization.parallelism value: {}", configured);
            }
        }
        int cpu = Runtime.getRuntime().availableProcessors();
        return Math.max(2, Math.min(4, cpu));
    }

    private static CompletableFuture<Void> submitWithTransaction(Executor executor, String taskName, Runnable task) {
        LOGGER.info("Initialization task '{}' scheduled", taskName);
        return CompletableFuture.runAsync(() -> {
            LOGGER.info("Initialization task '{}' starting", taskName);
            runInTransaction(taskName, task);
            LOGGER.info("Initialization task '{}' completed", taskName);
        }, executor).whenComplete((ignored, ex) -> {
            if (ex != null) {
                Throwable unwrapped = unwrapCompletionException(ex);
                if (unwrapped instanceof CancellationException) {
                    LOGGER.info("Initialization task '{}' cancelled", taskName);
                } else {
                    LOGGER.error("Initialization task '{}' failed", taskName, unwrapped);
                }
            }
        });
    }

    private static CompletableFuture<Void> submitTask(Executor executor, String taskName, Runnable task) {
        LOGGER.info("Initialization task '{}' scheduled", taskName);
        return CompletableFuture.runAsync(() -> {
            LOGGER.info("Initialization task '{}' starting", taskName);
            task.run();
            LOGGER.info("Initialization task '{}' completed", taskName);
        }, executor).whenComplete((ignored, ex) -> {
            if (ex != null) {
                Throwable unwrapped = unwrapCompletionException(ex);
                if (unwrapped instanceof CancellationException) {
                    LOGGER.info("Initialization task '{}' cancelled", taskName);
                } else {
                    LOGGER.error("Initialization task '{}' failed", taskName, unwrapped);
                }
            }
        });
    }

    private static void runInTransaction(String taskName, Runnable task) {
        HibernateTransactionManager txManager = null;
        try {
            txManager = ApplicationContextSingleton.getTransactionManager();
        } catch (Exception e) {
            LOGGER.warn("Transaction manager not available for task '{}'", taskName);
        }

        if (txManager == null) {
            task.run();
            return;
        }

        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setReadOnly(true);
        txTemplate.execute(status -> {
            task.run();
            return null;
        });
    }

    private static void waitForFuture(String taskName, Future<?> future) {
        if (future == null) {
            return;
        }
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Initialization interrupted while waiting for {}", taskName, e);
        } catch (CancellationException e) {
            LOGGER.error("Initialization task '{}' was cancelled", taskName, e);
        } catch (ExecutionException e) {
            // CompletableFuture-based tasks log failures via whenComplete to avoid waiting-order artifacts.
            if (!(future instanceof CompletableFuture)) {
                LOGGER.error("Initialization task '{}' failed", taskName, e.getCause());
            } else {
                LOGGER.debug("Initialization task '{}' failed (already logged)", taskName, e.getCause());
            }
        }
    }

    private static Throwable unwrapCompletionException(Throwable ex) {
        if (ex instanceof CompletionException && ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }

    private static void cacheAllDrugs() {
        Long current = MainUtils.getCurrentTimestamp();
        drugs = new HashSet<>(ApplicationContextSingleton.getDrugBo().findAll());
        LOGGER.info("Cached {} drugs {}", drugs.size(), CacheUtils.getCacheCompletionMessage(current));
    }

    private static void cacheAllTumorTypes() {
        Long current = MainUtils.getCurrentTimestamp();
        cancerTypes = ApplicationContextSingleton.getTumorTypeBo().findAll();
        cancerTypesByCode = new HashMap<>();
        cancerTypesByMainType = new HashMap<>();
        cancerTypesByLowercaseSubtype = new HashMap<>();

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
        LOGGER.info("Cached {} tumor types {}", cancerTypes.size(), CacheUtils.getCacheCompletionMessage(current));

        subtypes = cancerTypes.stream().filter(tumorType -> org.apache.commons.lang3.StringUtils.isNotEmpty(tumorType.getCode()) && tumorType.getLevel() > 0).collect(Collectors.toList());
        LOGGER.info("Cached {} tumor sub types {}", subtypes.size(), CacheUtils.getCacheCompletionMessage(current));
        mainTypes = cancerTypes.stream().filter(tumorType -> org.apache.commons.lang3.StringUtils.isEmpty(tumorType.getCode()) || tumorType.getLevel() > 0).collect(Collectors.toList());
        LOGGER.info("Cached {} tumor main types {}", mainTypes.size(), CacheUtils.getCacheCompletionMessage(current));

        current = MainUtils.getCurrentTimestamp();
        specialCancerTypes = Arrays.stream(SpecialTumorType.values())
            .map(specialTumorType -> cancerTypes.stream()
                .filter(cancerType -> !StringUtils.isNullOrEmpty(cancerType.getMainType()) && cancerType.getMainType().equals(specialTumorType.getTumorType()))
                .findAny().orElse(null))
            .filter(cancerType -> cancerType != null)
            .collect(Collectors.toList());
        LOGGER.info("Cached {} special tumor types {}", specialCancerTypes.size(), CacheUtils.getCacheCompletionMessage(current));
    }

    private static void cacheOncokbInfo() {
        Long current = MainUtils.getCurrentTimestamp();
        oncokbInfo = ApplicationContextSingleton.getInfoBo().get();
        LOGGER.info("Cached oncokb info {}", CacheUtils.getCacheCompletionMessage(current));
    }

    private static void cacheAbbreviations() {
        Long current = MainUtils.getCurrentTimestamp();
        try {
            NamingUtils.cacheAllAbbreviations();
            LOGGER.info("Cached abbreviation ontology {}", CacheUtils.getCacheCompletionMessage(current));
        } catch (IOException e) {
            LOGGER.error("Cached abbreviation ontology failed", e);
        }
    }

    private static void cacheDownloadAvailabilityTask() {
        Long current = MainUtils.getCurrentTimestamp();
        cacheDownloadAvailability();
        LOGGER.info("Cached downloadable files availability on github {}", CacheUtils.getCacheCompletionMessage(current));
    }

    private static void registerOtherServicesTask() {
        Long current = MainUtils.getCurrentTimestamp();
        try {
            registerOtherServices();
            LOGGER.info("Register other services {}", CacheUtils.getCacheCompletionMessage(current));
        } catch (IOException e) {
            LOGGER.error("Register other services failed", e);
        }
    }

    private static void cacheVusFromEvidences() {
        Long current = MainUtils.getCurrentTimestamp();
        for (Map.Entry<Integer, List<Evidence>> entry : evidences.entrySet()) {
            setVUS(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        LOGGER.info("Cached {} VUSs {}", VUS.size(), CacheUtils.getCacheCompletionMessage(current));
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
        LOGGER.info("Cached {} genes {}", genesByEntrezId.size(), CacheUtils.getCacheCompletionMessage(current));
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
        LOGGER.info("Cached {} alterations {}", allAlterations.size(), CacheUtils.getCacheCompletionMessage(current));
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
        LOGGER.info("Update gene on instance {}", PropertiesUtils.getProperties("app.name"));
        if (propagate == null) {
            propagate = false;
        }
        if(entrezGeneIds == null || entrezGeneIds.size() == 0){
            LOGGER.info("There is no entrez gene ids specified.");
            return;
        }
        entrezGeneIds.forEach(entrezGeneId -> GeneObservable.getInstance().update("update", entrezGeneId.toString()));
        if (propagate) {
            notifyOtherServices("update", entrezGeneIds);
        }else{
            LOGGER.info("Do not propagate.");
        }
    }

    public static void resetAll() throws IOException {
        LOGGER.info("Reset all genes cache on instance {}", PropertiesUtils.getProperties("app.name"));
        GeneObservable.getInstance().update("reset", null);
        notifyOtherServices("reset", null);
    }

    public static void resetAll(Boolean propagate) throws IOException {
        LOGGER.info("Reset all genes cache on instance {}", PropertiesUtils.getProperties("app.name"));
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
        LOGGER.info("Cached {} evidences across {} genes {}", allEvidencesSize, mappedEvidence.size(), CacheUtils.getCacheCompletionMessage(current));
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
            LOGGER.error("There is an issue connecting to GitHub.", e);
        } catch (NoPropertyException exception) {
            LOGGER.error("The data access token is not available", exception);
        }
    }
}
