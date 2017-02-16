package org.mskcc.cbio.oncokb.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.oncotree.model.MainType;
import org.mskcc.oncotree.model.TumorType;
import org.mskcc.oncotree.utils.TumorTypesUtil;

import java.io.IOException;
import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 * <p>
 * In OncoTree, there are two categories: cancerType and subtype. In order to distinguish
 * the difference, tumorType will be used to include both.
 */
public class TumorTypeUtils {
    private static String ONCO_TREE_API_URL = null;
    private static List<TumorType> allOncoTreeCancerTypes = new ArrayList<TumorType>() {{
        addAll(getOncoTreeCancerTypesFromSource());
        addAll(getAllSpecialTumorOncoTreeTypes());
    }};

    private static List<TumorType> allOncoTreeSubtypes =
        getOncoTreeSubtypesByCancerTypesFromSource(allOncoTreeCancerTypes);
    private static Map<String, TumorType> allNestedOncoTreeSubtypes = getAllNestedOncoTreeSubtypesFromSource();
    private static Map<String, List<TumorType>> questTumorTypeMap = null;
    private static Map<String, List<TumorType>> cbioTumorTypeMap = null;

    /**
     * Get all exist OncoTree tumor types including cancer types and subtypes
     *
     * @return
     */
    public static List<TumorType> getAllTumorTypes() {
        List<TumorType> oncoTreeTypes = new ArrayList<>();

        oncoTreeTypes.addAll(getAllCancerTypes());
        oncoTreeTypes.addAll(getAllSubtypes());

        return oncoTreeTypes;
    }

    /**
     * Get all exist OncoTree cancer types
     *
     * @return
     */
    public static List<TumorType> getAllCancerTypes() {
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getAllCancerTypes();
        } else {
            return getOncoTreeCancerTypes(ApplicationContextSingleton.getEvidenceBo().findAllCancerTypes());
        }
    }

    /**
     * Get all exist OncoTree subtypes
     *
     * @return
     */
    public static List<TumorType> getAllSubtypes() {
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getAllSubtypes();
        } else {
            return getOncoTreeSubtypesByCode(ApplicationContextSingleton.getEvidenceBo().findAllSubtypes());
        }
    }

    /**
     * Get all OncoTree tumor types including cancer types and subtypes
     *
     * @return
     */
    public static List<TumorType> getAllOncoTreeTypes() {
        List<TumorType> cancerTypes = new ArrayList<>();

        cancerTypes.addAll(getAllOncoTreeCancerTypes());
        cancerTypes.addAll(getAllOncoTreeSubtypes());

        return cancerTypes;
    }

    /**
     * Get all OncoTree cancer types
     *
     * @return
     */
    public static List<TumorType> getAllOncoTreeCancerTypes() {
        return allOncoTreeCancerTypes;
    }

    /**
     * Get all OncoTree subtypes
     *
     * @return
     */
    public static List<TumorType> getAllOncoTreeSubtypes() {
        return allOncoTreeSubtypes;
    }

    /**
     * Get mapped OncoTree cancer types based on cancer type names
     *
     * @param cancerTypes Caner type name queries
     * @return
     */
    public static List<TumorType> getOncoTreeCancerTypes(List<String> cancerTypes) {
        List<TumorType> mapped = new ArrayList<>();

        if (cancerTypes != null) {
            for (String cancerType : cancerTypes) {
                for (TumorType oncoTreeType : allOncoTreeCancerTypes) {
                    if (oncoTreeType.getMainType() != null && cancerType.equalsIgnoreCase(oncoTreeType.getMainType().getName())) {
                        mapped.add(oncoTreeType);
                        break;
                    }
                }
            }
        }

        return mapped;
    }

    /**
     * Get mapped OncoTree cancer type based on cancer type name
     *
     * @param cancerType Cancer type name query
     * @return
     */
    public static TumorType getOncoTreeCancerType(String cancerType) {
        List<TumorType> mapped = getOncoTreeCancerTypes(Collections.singletonList(cancerType));

        if (mapped != null && mapped.size() > 0) {
            return mapped.get(0);
        }

        return null;
    }

    /**
     * Get mapped OncoTree Subtypes based on subtype names
     *
     * @param names Subtype name
     * @return
     */
    public static List<TumorType> getOncoTreeSubtypesByName(List<String> names) {
        List<TumorType> mapped = new ArrayList<>();

        for (String name : names) {
            for (TumorType oncoTreeType : allOncoTreeSubtypes) {
                if (name.equalsIgnoreCase(oncoTreeType.getName())) {
                    mapped.add(oncoTreeType);
                    break;
                }
            }
        }

        return mapped;
    }

    public static TumorType getOncoTreeSubtypeByName(String name) {
        List<TumorType> matchedSubtypes = getOncoTreeSubtypesByName(Collections.singletonList(name));
        if (matchedSubtypes != null && matchedSubtypes.size() > 0) {
            return matchedSubtypes.get(0);
        }

        List<TumorType> matchedCancerTypes = getOncoTreeCancerTypes(Collections.singletonList(name));
        if (matchedCancerTypes != null && matchedCancerTypes.size() > 0) {
            return matchedCancerTypes.get(0);
        }

        return null;
    }

    /**
     * Get mapped OncoTree Subtypes based on subtype code
     *
     * @param codes
     * @return
     */
    public static List<TumorType> getOncoTreeSubtypesByCode(List<String> codes) {
        List<TumorType> mapped = new ArrayList<>();

        if (codes != null) {
            for (String code : codes) {
                for (TumorType oncoTreeType : allOncoTreeSubtypes) {
                    if (code.equalsIgnoreCase(oncoTreeType.getCode())) {
                        mapped.add(oncoTreeType);
                        break;
                    }
                }
            }
        }

        return mapped;
    }

    public static TumorType getOncoTreeSubtypeByCode(String code) {
        List<TumorType> matchedSubtypes = getOncoTreeSubtypesByCode(Collections.singletonList(code));
        if (matchedSubtypes != null && matchedSubtypes.size() > 0) {
            return matchedSubtypes.get(0);
        }

        List<TumorType> matchedCancerTypes = getOncoTreeCancerTypes(Collections.singletonList(code));
        if (matchedCancerTypes != null && matchedCancerTypes.size() > 0) {
            return matchedCancerTypes.get(0);
        }

        return null;
    }

    /**
     * The 'All Tumors' OncoTree instance
     *
     * @return
     */
    public static TumorType getMappedSpecialTumor(SpecialTumorType specialTumorType) {
        for (TumorType cancerType : allOncoTreeCancerTypes) {
            if (cancerType.getMainType() != null && cancerType.getMainType().getName().equalsIgnoreCase(specialTumorType.getTumorType())) {
                return cancerType;
            }
        }
        return null;
    }

    /**
     * TODO:
     *
     * @param cancerTypeQueries
     * @return
     */
    public static List<TumorType> generalMapping(List<TumorType> cancerTypeQueries) {
        List<TumorType> matches = new ArrayList<>();
        List<TumorType> allCancerTypes = getAllCancerTypes();
        List<TumorType> allSubtypes = getAllSubtypes();
        TumorType allTumor = getMappedSpecialTumor(SpecialTumorType.ALL_TUMORS);

        for (TumorType query : cancerTypeQueries) {
            if (query != null && !query.equals(allTumor)) {
                for (TumorType subtype : allSubtypes) {
                    if (subtype.equals(query)) {
                        matches.add(subtype);
                    }
                }
                for (TumorType cancerType : allCancerTypes) {
                    if (cancerType.equals(query)) {
                        matches.add(cancerType);
                    }
                }
            }
        }
        matches.add(allTumor);
        return matches;
    }

    /**
     * Get list of OncoTreeTypes based on query which constructed by name and source
     *
     * @param tumorType
     * @param source
     * @return
     */
    public static List<TumorType> getMappedOncoTreeTypesBySource(String tumorType, String source) {
        if (CacheUtils.isEnabled()) {
            if (!CacheUtils.containMappedTumorTypes(tumorType, source)) {
                CacheUtils.setMappedTumorTypes(tumorType, source, findTumorTypes(tumorType, source));
            }
            return CacheUtils.getMappedTumorTypes(tumorType, source);
        } else {
            return findTumorTypes(tumorType, source);
        }
    }

    /*-- PRIVATE --*/
    public static List<TumorType> findTumorTypes(String tumorType, String source) {
        List<TumorType> mappedTumorTypesFromSource = new ArrayList<>();
        if (source.equals("cbioportal")) {
            mappedTumorTypesFromSource.addAll(fromCbioportalTumorType(tumorType));
        } else {
            mappedTumorTypesFromSource.addAll(fromQuestTumorType(tumorType));
        }

        // Include exact matched tumor type
        if (mappedTumorTypesFromSource.size() == 0) {
            Set<TumorType> oncoTreeTypes = getOncoTreeTypesByTumorType(tumorType);
            if (oncoTreeTypes != null) {
                mappedTumorTypesFromSource = new ArrayList<>(oncoTreeTypes);
            }
        }

        // Include all parent nodes
        List<TumorType> parentIncludedMatchByCode = TumorTypesUtil.findTumorType(
            allNestedOncoTreeSubtypes.get("TISSUE"), allNestedOncoTreeSubtypes.get("TISSUE"),
            new ArrayList<TumorType>(), "code", tumorType, true, true);
        List<TumorType> parentIncludedMatchByName = TumorTypesUtil.findTumorType(
            allNestedOncoTreeSubtypes.get("TISSUE"), allNestedOncoTreeSubtypes.get("TISSUE"),
            new ArrayList<TumorType>(), "name", tumorType, true, true);

        mappedTumorTypesFromSource.addAll(parentIncludedMatchByCode);
        mappedTumorTypesFromSource.addAll(parentIncludedMatchByName);

        // Include all tumors
        TumorType allTumor = getMappedSpecialTumor(SpecialTumorType.ALL_TUMORS);
        if (allTumor != null) {
            mappedTumorTypesFromSource.add(allTumor);
        }

        return new ArrayList<>(new LinkedHashSet<>(mappedTumorTypesFromSource));
    }

    private static Set<TumorType> fromQuestTumorType(String questTumorType) {
        if (questTumorTypeMap == null) {
            questTumorTypeMap = new HashMap<String, List<TumorType>>();

            TumorType tumorTypeAll = getMappedSpecialTumor(SpecialTumorType.ALL_TUMORS);

            List<String> lines;
            try {
                lines = FileUtils.readTrimedLinesStream(TumorTypeUtils.class.getResourceAsStream("/data/quest-tumor-types.txt"));
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.singleton(tumorTypeAll);
            }
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                String questType = parts[0].toLowerCase();
                TumorType oncokbType = getOncoTreeSubtypeByCode(parts[1]);
                if (oncokbType == null) {
                    oncokbType = getOncoTreeCancerType(parts[1]);

                    if (oncokbType == null) {
                        System.err.println("no " + parts[1] + " as tumor type in oncokb");
                        continue;
                    }
                }

                List<TumorType> types = questTumorTypeMap.get(questType);
                if (types == null) {
                    types = new LinkedList<>();
                    questTumorTypeMap.put(questType, types);
                }
                types.add(oncokbType);
            }

            if (tumorTypeAll != null) {
                for (List<TumorType> list : questTumorTypeMap.values()) {
                    list.add(tumorTypeAll);
                }
            }
        }

        questTumorType = questTumorType == null ? null : questTumorType.toLowerCase();

        if (questTumorType == null) {
            return new LinkedHashSet<>();
        }

        List<TumorType> ret = questTumorTypeMap.get(questTumorType);

        return ret == null ? new LinkedHashSet<TumorType>() : new LinkedHashSet<>(ret);
    }

    private static Set<TumorType> getOncoTreeTypesByTumorType(String tumorType) {
        Set<TumorType> types = new HashSet<>();
        TumorType mapped;

        mapped = getOncoTreeSubtypeByCode(tumorType);
        if (mapped == null) {
            mapped = getOncoTreeSubtypeByName(tumorType);
        }

        if (mapped == null) {
            mapped = getOncoTreeCancerType(tumorType);
        } else if (mapped.getMainType() != null) {
            //Also include the cancer type into relevant types
            types.add(getOncoTreeCancerType(mapped.getMainType().getName()));
        }
        if (mapped != null) {
            types.add(mapped);
        }
        return types;
    }

    private static Set<TumorType> fromCbioportalTumorType(String cbioTumorType) {
        if (cbioTumorTypeMap == null) {
            cbioTumorTypeMap = new HashMap<String, List<TumorType>>();

            TumorType tumorTypeAll = getMappedSpecialTumor(SpecialTumorType.ALL_TUMORS);

            List<String> lines;
            try {
                lines = FileUtils.readTrimedLinesStream(
                    TumorTypeUtils.class.getResourceAsStream("/data/cbioportal-tumor-types.txt"));
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.singleton(tumorTypeAll);
            }
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                if (parts.length > 2) {
                    String cbioType = parts[0].toLowerCase();
                    List<TumorType> types = cbioTumorTypeMap.get(cbioType);
                    if (types == null) {
                        types = new LinkedList<>();
                        cbioTumorTypeMap.put(cbioType, types);
                    }

                    for (int i = 1; i < parts.length; i++) {
                        TumorType oncokbType = getOncoTreeSubtypeByCode(parts[i]);
                        if (oncokbType == null) {
                            oncokbType = getOncoTreeCancerType(parts[i]);

                            if (oncokbType == null) {
                                System.err.println("no " + parts[i] + " as tumor type in oncokb");
                                continue;
                            }
                        }
                        types.add(oncokbType);
                    }
                }
            }

            if (tumorTypeAll != null) {
                for (List<TumorType> list : cbioTumorTypeMap.values()) {
                    list.add(tumorTypeAll);
                }
            }
        }

        cbioTumorType = cbioTumorType == null ? null : cbioTumorType.toLowerCase();

        if (cbioTumorType == null) {
            return new LinkedHashSet<>();
        }

        List<TumorType> ret = cbioTumorTypeMap.get(cbioTumorType);

        return ret == null ? new LinkedHashSet<TumorType>() : new LinkedHashSet<>(ret);
    }

    private static List<TumorType> getOncoTreeCancerTypesFromSource() {
        String url = getOncoTreeApiUrl() + "mainTypes?version=oncokb";
        List<TumorType> cancerTypes = new ArrayList<>();

        try {
            String json = FileUtils.readRemote(url);
            Map map = JsonUtils.jsonToMap(json);
            List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
            if (data != null) {
                for (Map<String, Object> datum : data) {
                    TumorType cancerType = new TumorType();
                    MainType mainType = new MainType();
                    mainType.setName((String) datum.get("name"));
                    cancerType.setMainType(mainType);
                    cancerTypes.add(cancerType);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cancerTypes;
    }

    private static Map<String, TumorType> getAllNestedOncoTreeSubtypesFromSource() {
        String url = getOncoTreeApiUrl() + "tumorTypes?version=oncokb&flat=false&deprecated=false";

        try {
            String json = FileUtils.readRemote(url);
            Map map = JsonUtils.jsonToMap(json);
            Map<String, TumorType> result = new HashMap<>();
            Map<String, TumorType> data = (Map<String, TumorType>) map.get("data");
            result.put("TISSUE", new ObjectMapper().convertValue(data.get("TISSUE"), TumorType.class));
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<TumorType> getAllOncoTreeSubtypesFromSource() {
        String url = getOncoTreeApiUrl() + "tumorTypes?version=oncokb&flat=true&deprecated=false";

        try {
            String json = FileUtils.readRemote(url);
            Map map = JsonUtils.jsonToMap(json);
            return (List<TumorType>) map.get("data");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<TumorType> getOncoTreeSubtypesByCancerTypesFromSource(List<TumorType> cancerTypes) {
        List<TumorType> subtypes = new ArrayList<>();
        try {
            String url = getOncoTreeApiUrl() + "tumorTypes/search";
            JSONObject postData = new JSONObject();
            postData.put("version", "oncokb");

            JSONArray queries = new JSONArray();
            for (TumorType cancerType : cancerTypes) {
                if (cancerType.getMainType() != null) {
                    JSONObject query = new JSONObject();
                    query.put("exactMatch", "true");
                    query.put("query", cancerType.getMainType().getName());
                    query.put("type", "maintype");
                    queries.put(query);
                }
            }
            postData.put("queries", queries);

            String response = HttpUtils.postRequest(url, postData.toString());
            if (response != null && !response.equals("TIMEOUT")) {
                Map map = JsonUtils.jsonToMap(response);

                if (map.get("data") != null) {
                    for (List<Map<String, Object>> queryResult : (List<List<Map<String, Object>>>) map.get("data")) {
                        for (Map<String, Object> datum : queryResult) {
                            subtypes.add(new ObjectMapper().convertValue(datum, TumorType.class));
                        }
                    }
                }
            } else {
                System.out.println("Error access OncoTree Service.");
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return subtypes;
    }

    private static Set<TumorType> getAllSpecialTumorOncoTreeTypes() {
        Set<TumorType> types = new HashSet<>();
        for (SpecialTumorType specialTumorType : SpecialTumorType.values()) {
            TumorType tumorType = new TumorType();
            MainType mainType = new MainType();
            mainType.setName(specialTumorType.getTumorType());
            tumorType.setMainType(mainType);
            types.add(tumorType);
        }
        return types;
    }

    private static String getOncoTreeApiUrl() {
        if (ONCO_TREE_API_URL == null) {
            try {
                ONCO_TREE_API_URL = PropertiesUtils.getProperties("oncotree.api");
            } catch (IOException e) {
                System.out.print("No oncotree.api specified, will use default setting.");
            }
            if (ONCO_TREE_API_URL != null) {
                ONCO_TREE_API_URL = ONCO_TREE_API_URL.trim();
            }
            if (ONCO_TREE_API_URL == null || ONCO_TREE_API_URL.isEmpty()) {
                ONCO_TREE_API_URL = "http://oncotree.mskcc.org/oncotree/api/";
            }
        }
        return ONCO_TREE_API_URL;
    }
}
