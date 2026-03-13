package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.apiModels.download.DownloadAvailability;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.health.InMemoryCacheSizes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * Created by Hongxin on 4/1/16.
 *
 * CacheUtils is used to manage cached variant summaries, relevant alterations, alterations which all gene based.
 * It also includes mapped tumor types which is based on query tumor type name.
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
    private static final AtomicBoolean cacheRefreshInProgress = new AtomicBoolean(false);

    public static InMemoryCacheSizes getCurrentCacheSizes() {

        return new InMemoryCacheSizes(
            genes.size(),
            new HashMap<>(alterations).values().stream().mapToInt(List::size).sum(),
            drugs.size(),
            cancerTypes.size(),
            allEvidencesSize
        );
    }

    public static boolean isCacheRefreshInProgress() {
        return cacheRefreshInProgress.get();
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

    static {
        try {
            registerOtherServices();
            refreshAllCaches();
        } catch (Exception e) {
            LOGGER.error("Unexpected Error", e);
        }
    }

    private static void registerOtherServices() throws IOException {
        Long current = MainUtils.getCurrentTimestamp();
        String services = PropertiesUtils.getProperties("cache.update");
        if (services != null) {
            otherServices = Arrays.asList(services.split(","));
        }
        LOGGER.info("Register other services {}", CacheUtils.getCacheCompletionMessage(current));
    }

    private static void cacheAllTumorTypes() {
        Long current = MainUtils.getCurrentTimestamp();
        try {
            cancerTypes = loadAllTumorTypesFromTables();
            LOGGER.info("Loaded tumor types via SQL table mapping.");
        } catch (Exception exception) {
            LOGGER.warn("Failed to load tumor types via SQL mapping. Falling back to DAO findAll().", exception);
            cancerTypes = ApplicationContextSingleton.getTumorTypeBo().findAll();
        }
        cancerTypesByCode.clear();
        cancerTypesByMainType.clear();
        cancerTypesByLowercaseSubtype.clear();
        cancerTypes.forEach(ct -> {
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
        specialCancerTypes = Arrays.stream(SpecialTumorType.values())
            .map(specialTumorType -> cancerTypes.stream()
                .filter(cancerType -> !StringUtils.isNullOrEmpty(cancerType.getMainType()) && cancerType.getMainType().equals(specialTumorType.getTumorType()))
                .findAny()
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        LOGGER.info("Cached {} special tumor types {}", specialCancerTypes.size(), CacheUtils.getCacheCompletionMessage(current));
    }

    private static List<TumorType> loadAllTumorTypesFromTables() throws SQLException {
        Map<Integer, TumorType> tumorTypesById = new LinkedHashMap<>();
        Map<Integer, Integer> parentByTumorTypeId = new HashMap<>();

        try (Connection connection = ApplicationContextSingleton.getDataSource().getConnection()) {
            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, subtype, code, color, main_type, level, tissue, parent, tumor_form FROM cancer_type");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    TumorType tumorType = new TumorType();
                    tumorType.setId(id);
                    tumorType.setSubtype(resultSet.getString("subtype"));
                    tumorType.setCode(resultSet.getString("code"));
                    tumorType.setColor(resultSet.getString("color"));
                    tumorType.setMainType(resultSet.getString("main_type"));
                    Integer level = resultSet.getInt("level");
                    tumorType.setLevel(resultSet.wasNull() ? null : level);
                    tumorType.setTissue(resultSet.getString("tissue"));
                    tumorType.setTumorForm(toEnum(TumorForm.class, resultSet.getString("tumor_form")));
                    tumorType.setChildren(new HashSet<>());
                    tumorTypesById.put(id, tumorType);

                    Integer parentId = resultSet.getInt("parent");
                    if (!resultSet.wasNull()) {
                        parentByTumorTypeId.put(id, parentId);
                    }
                }
            }

            for (Map.Entry<Integer, Integer> entry : parentByTumorTypeId.entrySet()) {
                TumorType tumorType = tumorTypesById.get(entry.getKey());
                TumorType parent = tumorTypesById.get(entry.getValue());
                if (tumorType != null) {
                    tumorType.setParent(parent);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT cancer_type_id, cancer_type_child_id FROM cancer_type_child");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    TumorType parent = tumorTypesById.get(resultSet.getInt("cancer_type_id"));
                    TumorType child = tumorTypesById.get(resultSet.getInt("cancer_type_child_id"));
                    if (parent != null && child != null && parent.getChildren() != null) {
                        parent.getChildren().add(child);
                    }
                }
            }
        }

        return new ArrayList<>(tumorTypesById.values());
    }

    private static synchronized void refreshAllCaches() throws IOException {
        cacheRefreshInProgress.set(true);
        try {
            Long current = MainUtils.getCurrentTimestamp();
            LOGGER.info("Refreshing in memory caches");

            numbers.clear();
            VUS.clear();

            cacheAllGenes();
            setAllAlterations();

            current = MainUtils.getCurrentTimestamp();
            drugs = new HashSet<>(ApplicationContextSingleton.getDrugBo().findAll());
            LOGGER.info("Cached {} drugs {}", drugs.size(), CacheUtils.getCacheCompletionMessage(current));

            cacheAllTumorTypes();

            current = MainUtils.getCurrentTimestamp();
            cacheAllEvidencesByGenes();
            LOGGER.info("Cached {} evidences {}", allEvidencesSize, CacheUtils.getCacheCompletionMessage(current));

            current = MainUtils.getCurrentTimestamp();
            for (Map.Entry<Integer, List<Evidence>> entry : evidences.entrySet()) {
                setVUS(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            LOGGER.info("Cached {} VUSs {}", VUS.size(), CacheUtils.getCacheCompletionMessage(current));

            current = MainUtils.getCurrentTimestamp();
            NamingUtils.cacheAllAbbreviations();
            LOGGER.info("Cached abbreviation ontology {}", CacheUtils.getCacheCompletionMessage(current));

            current = MainUtils.getCurrentTimestamp();
            cacheDownloadAvailability();
            LOGGER.info("Cached downloadable files availability on github {}", CacheUtils.getCacheCompletionMessage(current));

            current = MainUtils.getCurrentTimestamp();
            oncokbInfo = ApplicationContextSingleton.getInfoBo().get();
            LOGGER.info("Cached oncokb info {}", CacheUtils.getCacheCompletionMessage(current));
        } finally {
            cacheRefreshInProgress.set(false);
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

        List<Gene> allGenes;
        try {
            allGenes = loadAllGenesFromTables();
            LOGGER.info("Loaded genes via SQL table mapping.");
        } catch (Exception exception) {
            LOGGER.warn("Failed to load genes via SQL mapping. Falling back to DAO findAll().", exception);
            allGenes = ApplicationContextSingleton.getGeneBo().findAll();
        }

        genes = new HashSet<>(allGenes);
        genesByEntrezId = new HashMap<>();
        hugoSymbolToEntrez = new HashMap<>();
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
            cacheAllGenes();
        }
        return genes;
    }

    private static void setAllAlterations() {
        Long current = MainUtils.getCurrentTimestamp();
        List<Alteration> allAlterations;
        alterations.clear();
        alterationsByReferenceGenome.clear();
        try {
            allAlterations = loadAllAlterationsFromTables();
            LOGGER.info("Loaded alterations via SQL table mapping.");
        } catch (Exception exception) {
            LOGGER.warn("Failed to load alterations via SQL mapping. Falling back to DAO findAll().", exception);
            allAlterations = ApplicationContextSingleton.getAlterationBo().findAll();
        }

        for (Alteration alteration : allAlterations) {
            Gene gene = alteration.getGene();
            if (gene == null || gene.getEntrezGeneId() == null) {
                continue;
            }
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
        LOGGER.info("updateGene compatibility path now performs a full cache refresh.");
        refreshAllCaches();
        if (propagate) {
            notifyOtherServices("update", entrezGeneIds);
        }else{
            LOGGER.info("Do not propagate.");
        }
    }

    public static void resetAll() throws IOException {
        resetAll(true);
    }

    public static void resetAll(Boolean propagate) throws IOException {
        LOGGER.info("Reset all genes cache on instance {}", PropertiesUtils.getProperties("app.name"));
        refreshAllCaches();
        if (propagate == null) {
            propagate = false;
        }
        if (propagate) {
            notifyOtherServices("reset", null);
        }
    }


    private static void cacheAllEvidencesByGenes() {
        Long current = MainUtils.getCurrentTimestamp();
        List<Evidence> allEvidences;
        evidences.clear();
        evidenceRelevantCancerTypes.clear();
        try {
            allEvidences = loadAllEvidencesFromTables();
            LOGGER.info("Loaded evidences via SQL table mapping.");
        } catch (Exception exception) {
            LOGGER.warn("Failed to load evidences via SQL mapping. Falling back to DAO findAll().", exception);
            allEvidences = ApplicationContextSingleton.getEvidenceBo().findAll();
        }
        allEvidencesSize = allEvidences.size();

        Map<Gene, List<Evidence>> mappedEvidence = EvidenceUtils.separateEvidencesByGene(genes, new HashSet<>(allEvidences));
        Iterator it = mappedEvidence.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Gene, List<Evidence>> pair = (Map.Entry) it.next();
            int entrezGeneId = pair.getKey().getEntrezGeneId();
            evidences.put(entrezGeneId, pair.getValue());
            updateEvidenceRelevantCancerTypes(entrezGeneId, pair.getValue());
        }
        LOGGER.info("Cached all evidences of {} genes {}",mappedEvidence.size(), CacheUtils.getCacheCompletionMessage(current));
    }

    private static List<Alteration> loadAllAlterationsFromTables() throws SQLException {
        Map<Integer, Alteration> alterationsById = new LinkedHashMap<>();
        Map<Integer, PortalAlteration> portalAlterationsById = new HashMap<>();
        Map<String, VariantConsequence> variantConsequencesByTerm = ApplicationContextSingleton.getVariantConsequenceBo().findAll().stream()
            .collect(Collectors.toMap(VariantConsequence::getTerm, variantConsequence -> variantConsequence));

        try (Connection connection = ApplicationContextSingleton.getDataSource().getConnection()) {
            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, uuid, entrez_gene_id, alteration_type, consequence, alteration, protein_change, name, ref_residues, protein_start, protein_end, variant_residues, for_germline FROM alteration");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    Alteration alteration = new Alteration();
                    alteration.setId(id);
                    alteration.setUuid(resultSet.getString("uuid"));
                    alteration.setGene(genesByEntrezId.get(resultSet.getInt("entrez_gene_id")));
                    alteration.setAlterationType(toEnum(AlterationType.class, resultSet.getString("alteration_type")));
                    alteration.setConsequence(variantConsequencesByTerm.get(resultSet.getString("consequence")));
                    alteration.setAlteration(resultSet.getString("alteration"));
                    alteration.setProteinChange(resultSet.getString("protein_change"));
                    alteration.setName(resultSet.getString("name"));
                    alteration.setRefResidues(resultSet.getString("ref_residues"));
                    Integer proteinStart = resultSet.getInt("protein_start");
                    alteration.setProteinStart(resultSet.wasNull() ? null : proteinStart);
                    Integer proteinEnd = resultSet.getInt("protein_end");
                    alteration.setProteinEnd(resultSet.wasNull() ? null : proteinEnd);
                    alteration.setVariantResidues(resultSet.getString("variant_residues"));
                    Boolean forGermline = getNullableBoolean(resultSet, "for_germline");
                    alteration.setForGermline(Boolean.TRUE.equals(forGermline));
                    alteration.setReferenceGenomes(new HashSet<>());
                    alteration.setPortalAlterations(new HashSet<>());
                    alterationsById.put(id, alteration);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT alteration_id, reference_genome FROM alteration_reference_genome");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Alteration alteration = alterationsById.get(resultSet.getInt("alteration_id"));
                    if (alteration != null) {
                        ReferenceGenome referenceGenome = toEnum(ReferenceGenome.class, resultSet.getString("reference_genome"));
                        if (referenceGenome != null) {
                            alteration.getReferenceGenomes().add(referenceGenome);
                        }
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, cancer_type, cancer_study, sample_id, entrez_gene_id, protein_change, protein_start, protein_end, alteration_type FROM portal_alteration");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    PortalAlteration portalAlteration = new PortalAlteration();
                    portalAlteration.setId(id);
                    portalAlteration.setCancerType(resultSet.getString("cancer_type"));
                    portalAlteration.setCancerStudy(resultSet.getString("cancer_study"));
                    portalAlteration.setSampleId(resultSet.getString("sample_id"));
                    portalAlteration.setGene(genesByEntrezId.get(resultSet.getInt("entrez_gene_id")));
                    portalAlteration.setProteinChange(resultSet.getString("protein_change"));
                    Integer proteinStart = resultSet.getInt("protein_start");
                    portalAlteration.setProteinStartPosition(resultSet.wasNull() ? null : proteinStart);
                    Integer proteinEnd = resultSet.getInt("protein_end");
                    portalAlteration.setProteinEndPosition(resultSet.wasNull() ? null : proteinEnd);
                    portalAlteration.setAlterationType(resultSet.getString("alteration_type"));
                    portalAlterationsById.put(id, portalAlteration);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT oncokb_alteration_id, portal_alteration_id FROM portal_alteration_oncokb_alteration");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Alteration alteration = alterationsById.get(resultSet.getInt("oncokb_alteration_id"));
                    PortalAlteration portalAlteration = portalAlterationsById.get(resultSet.getInt("portal_alteration_id"));
                    if (alteration != null && portalAlteration != null) {
                        alteration.getPortalAlterations().add(portalAlteration);
                    }
                }
            }
        }

        return new ArrayList<>(alterationsById.values());
    }

    private static List<Gene> loadAllGenesFromTables() throws SQLException {
        Map<Integer, Gene> genesById = new LinkedHashMap<>();

        try (Connection connection = ApplicationContextSingleton.getDataSource().getConnection()) {
            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT entrez_gene_id, hugo_symbol, gene_type, grch37_isoform, grch37_ref_seq, grch38_isoform, grch38_ref_seq FROM gene");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer entrezGeneId = resultSet.getInt("entrez_gene_id");
                    Gene gene = new Gene();
                    gene.setEntrezGeneId(entrezGeneId);
                    gene.setHugoSymbol(resultSet.getString("hugo_symbol"));
                    gene.setGeneType(toEnum(GeneType.class, resultSet.getString("gene_type")));
                    gene.setGrch37Isoform(resultSet.getString("grch37_isoform"));
                    gene.setGrch37RefSeq(resultSet.getString("grch37_ref_seq"));
                    gene.setGrch38Isoform(resultSet.getString("grch38_isoform"));
                    gene.setGrch38RefSeq(resultSet.getString("grch38_ref_seq"));
                    gene.setGeneAliases(new HashSet<>());
                    gene.setGenesets(new HashSet<>());
                    genesById.put(entrezGeneId, gene);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT entrez_gene_id, alias FROM gene_alias");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Gene gene = genesById.get(resultSet.getInt("entrez_gene_id"));
                    if (gene != null) {
                        String alias = resultSet.getString("alias");
                        if (alias != null) {
                            gene.getGeneAliases().add(alias);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(genesById.values());
    }

    private static List<Evidence> loadAllEvidencesFromTables() throws SQLException {
        Map<Integer, Evidence> evidencesById = new LinkedHashMap<>();
        Map<Integer, Article> articlesById = new HashMap<>();
        Map<Integer, Treatment> treatmentsById = new HashMap<>();
        Map<Integer, Tag> tagsById = new HashMap<>();
        Map<Integer, OncogenicityEntity> oncogenicitiesById = new HashMap<>();
        Map<Integer, MutationTypeEntity> mutationTypesById = new HashMap<>();

        Map<Integer, Alteration> alterationsById = getCachedAlterationsById();
        Map<Integer, TumorType> tumorTypesById = cancerTypes.stream()
            .collect(Collectors.toMap(TumorType::getId, tumorType -> tumorType));
        Map<Integer, Drug> drugsById = drugs.stream()
            .collect(Collectors.toMap(Drug::getId, drug -> drug));

        try (Connection connection = ApplicationContextSingleton.getDataSource().getConnection()) {
            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, uuid, evidence_type, for_germline, entrez_gene_id, name, description, additional_info, known_effect, last_edit, last_review, level_of_evidence, fda_level, solid_propagation_level, liquid_propagation_level FROM evidence");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    Evidence evidence = new Evidence();
                    evidence.setId(id);
                    evidence.setUuid(resultSet.getString("uuid"));
                    evidence.setEvidenceType(toEnum(EvidenceType.class, resultSet.getString("evidence_type")));
                    Boolean forGermline = getNullableBoolean(resultSet, "for_germline");
                    evidence.setForGermline(Boolean.TRUE.equals(forGermline));
                    evidence.setGene(genesByEntrezId.get(resultSet.getInt("entrez_gene_id")));
                    evidence.setName(resultSet.getString("name"));
                    evidence.setDescription(resultSet.getString("description"));
                    evidence.setAdditionalInfo(resultSet.getString("additional_info"));
                    evidence.setKnownEffect(resultSet.getString("known_effect"));
                    Timestamp lastEdit = resultSet.getTimestamp("last_edit");
                    evidence.setLastEdit(lastEdit == null ? null : new Date(lastEdit.getTime()));
                    Timestamp lastReview = resultSet.getTimestamp("last_review");
                    evidence.setLastReview(lastReview == null ? null : new Date(lastReview.getTime()));
                    evidence.setLevelOfEvidence(toEnum(LevelOfEvidence.class, resultSet.getString("level_of_evidence")));
                    evidence.setFdaLevel(toEnum(LevelOfEvidence.class, resultSet.getString("fda_level")));
                    evidence.setSolidPropagationLevel(toEnum(LevelOfEvidence.class, resultSet.getString("solid_propagation_level")));
                    evidence.setLiquidPropagationLevel(toEnum(LevelOfEvidence.class, resultSet.getString("liquid_propagation_level")));
                    evidence.setCancerTypes(new HashSet<>());
                    evidence.setExcludedCancerTypes(new HashSet<>());
                    evidence.setRelevantCancerTypes(new HashSet<>());
                    evidence.setAlterations(new HashSet<>());
                    evidence.setTags(new HashSet<>());
                    evidence.setArticles(new HashSet<>());
                    evidence.setTreatments(new ArrayList<>());
                    evidencesById.put(id, evidence);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT evidence_id, alteration_id FROM evidence_alteration");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Evidence evidence = evidencesById.get(resultSet.getInt("evidence_id"));
                    Alteration alteration = alterationsById.get(resultSet.getInt("alteration_id"));
                    if (evidence != null && alteration != null) {
                        evidence.getAlterations().add(alteration);
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT evidence_id, cancer_type_id FROM evidence_cancer_type");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Evidence evidence = evidencesById.get(resultSet.getInt("evidence_id"));
                    TumorType tumorType = tumorTypesById.get(resultSet.getInt("cancer_type_id"));
                    if (evidence != null && tumorType != null) {
                        evidence.getCancerTypes().add(tumorType);
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT evidence_id, cancer_type_id FROM evidence_excluded_cancer_type");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Evidence evidence = evidencesById.get(resultSet.getInt("evidence_id"));
                    TumorType tumorType = tumorTypesById.get(resultSet.getInt("cancer_type_id"));
                    if (evidence != null && tumorType != null) {
                        evidence.getExcludedCancerTypes().add(tumorType);
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT evidence_id, cancer_type_id FROM evidence_relevant_cancer_type");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Evidence evidence = evidencesById.get(resultSet.getInt("evidence_id"));
                    TumorType tumorType = tumorTypesById.get(resultSet.getInt("cancer_type_id"));
                    if (evidence != null && tumorType != null) {
                        evidence.getRelevantCancerTypes().add(tumorType);
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, name, description, start AS start_position, end AS end_position, entrez_gene_id FROM tag");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    Tag tag = new Tag();
                    tag.setId(id);
                    tag.setName(resultSet.getString("name"));
                    tag.setDescription(resultSet.getString("description"));
                    Integer start = resultSet.getInt("start_position");
                    tag.setStart(resultSet.wasNull() ? null : start);
                    Integer end = resultSet.getInt("end_position");
                    tag.setEnd(resultSet.wasNull() ? null : end);
                    Integer entrezGeneId = resultSet.getInt("entrez_gene_id");
                    if (!resultSet.wasNull()) {
                        tag.setGene(genesByEntrezId.get(entrezGeneId));
                    }
                    tag.setOncogenicities(new HashSet<>());
                    tag.setMutationTypes(new HashSet<>());
                    tag.setEvidences(new HashSet<>());
                    tagsById.put(id, tag);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, oncogenicity FROM oncogenicity");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    OncogenicityEntity oncogenicityEntity = new OncogenicityEntity();
                    oncogenicityEntity.setId(id);
                    Oncogenicity oncogenicity = Oncogenicity.getByEffect(resultSet.getString("oncogenicity"));
                    if (oncogenicity != null) {
                        oncogenicityEntity.setOncogenicity(oncogenicity);
                    }
                    oncogenicityEntity.setTags(new HashSet<>());
                    oncogenicitiesById.put(id, oncogenicityEntity);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, mutation_type FROM mutation_type");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    MutationTypeEntity mutationTypeEntity = new MutationTypeEntity();
                    mutationTypeEntity.setId(id);
                    MutationType mutationType = MutationType.fromString(resultSet.getString("mutation_type"));
                    if (mutationType != null) {
                        mutationTypeEntity.setMutationType(mutationType);
                    }
                    mutationTypeEntity.setTags(new HashSet<>());
                    mutationTypesById.put(id, mutationTypeEntity);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT tag_id, oncogenicity_id FROM oncogenicity_tag");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Tag tag = tagsById.get(resultSet.getInt("tag_id"));
                    OncogenicityEntity oncogenicityEntity = oncogenicitiesById.get(resultSet.getInt("oncogenicity_id"));
                    if (tag != null && oncogenicityEntity != null) {
                        tag.getOncogenicities().add(oncogenicityEntity);
                        oncogenicityEntity.getTags().add(tag);
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT tag_id, mutation_type_id FROM mutation_type_tag");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Tag tag = tagsById.get(resultSet.getInt("tag_id"));
                    MutationTypeEntity mutationTypeEntity = mutationTypesById.get(resultSet.getInt("mutation_type_id"));
                    if (tag != null && mutationTypeEntity != null) {
                        tag.getMutationTypes().add(mutationTypeEntity);
                        mutationTypeEntity.getTags().add(tag);
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT evidence_id, tag_id FROM evidence_tag");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Evidence evidence = evidencesById.get(resultSet.getInt("evidence_id"));
                    Tag tag = tagsById.get(resultSet.getInt("tag_id"));
                    if (evidence != null && tag != null) {
                        evidence.getTags().add(tag);
                        tag.getEvidences().add(evidence);
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, uuid, pmid, title, journal, pub_date, volume, issue, pages, authors, elocationId AS elocation_id, abstract_content, link FROM article");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    Article article = new Article();
                    article.setId(id);
                    article.setUuid(resultSet.getString("uuid"));
                    article.setPmid(resultSet.getString("pmid"));
                    article.setTitle(resultSet.getString("title"));
                    article.setJournal(resultSet.getString("journal"));
                    article.setPubDate(resultSet.getString("pub_date"));
                    article.setVolume(resultSet.getString("volume"));
                    article.setIssue(resultSet.getString("issue"));
                    article.setPages(resultSet.getString("pages"));
                    article.setAuthors(resultSet.getString("authors"));
                    article.setElocationId(resultSet.getString("elocation_id"));
                    article.setAbstractContent(resultSet.getString("abstract_content"));
                    article.setLink(resultSet.getString("link"));
                    articlesById.put(id, article);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT evidence_id, article_id FROM evidence_article");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Evidence evidence = evidencesById.get(resultSet.getInt("evidence_id"));
                    Article article = articlesById.get(resultSet.getInt("article_id"));
                    if (evidence != null && article != null) {
                        evidence.getArticles().add(article);
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, uuid, priority, evidence_id FROM treatment");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    Treatment treatment = new Treatment();
                    treatment.setId(id);
                    treatment.setUuid(resultSet.getString("uuid"));
                    Integer priority = resultSet.getInt("priority");
                    treatment.setPriority(resultSet.wasNull() ? null : priority);
                    treatment.setApprovedIndications(new HashSet<>());
                    Evidence evidence = evidencesById.get(resultSet.getInt("evidence_id"));
                    treatment.setEvidence(evidence);
                    if (evidence != null) {
                        evidence.getTreatments().add(treatment);
                    }
                    treatmentsById.put(id, treatment);
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT treatment_id, approved_indications FROM treatment_approved_indications");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Treatment treatment = treatmentsById.get(resultSet.getInt("treatment_id"));
                    if (treatment != null) {
                        treatment.getApprovedIndications().add(resultSet.getString("approved_indications"));
                    }
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT treatment_id, drug_id, priority FROM treatment_drug");
                ResultSet resultSet = statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    Treatment treatment = treatmentsById.get(resultSet.getInt("treatment_id"));
                    Drug drug = drugsById.get(resultSet.getInt("drug_id"));
                    if (treatment != null && drug != null) {
                        TreatmentDrug treatmentDrug = new TreatmentDrug();
                        treatmentDrug.setTreatment(treatment);
                        treatmentDrug.setDrug(drug);
                        Integer priority = resultSet.getInt("priority");
                        treatmentDrug.setPriority(resultSet.wasNull() ? null : priority);
                        treatment.getTreatmentDrugs().add(treatmentDrug);
                    }
                }
            }
        }

        return new ArrayList<>(evidencesById.values());
    }

    private static Map<Integer, Alteration> getCachedAlterationsById() {
        Map<Integer, Alteration> alterationsById = new HashMap<>();
        for (List<Alteration> mappedAlterations : alterations.values()) {
            for (Alteration alteration : mappedAlterations) {
                if (alteration != null && alteration.getId() != null) {
                    alterationsById.put(alteration.getId(), alteration);
                }
            }
        }
        return alterationsById;
    }

    private static <T extends Enum<T>> T toEnum(Class<T> enumType, String value) {
        if (org.apache.commons.lang3.StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("Unable to map enum value '{}' for type '{}'", value, enumType.getSimpleName());
            return null;
        }
    }

    private static Boolean getNullableBoolean(ResultSet resultSet, String columnName) throws SQLException {
        Object value = resultSet.getObject(columnName);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return Boolean.parseBoolean(value.toString());
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
