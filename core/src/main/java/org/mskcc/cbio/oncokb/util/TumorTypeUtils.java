package org.mskcc.cbio.oncokb.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.OncoTreeType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 * <p>
 * In OncoTree, there are two categories: cancerType and subtype. In order to distinguish
 * the difference, tumorType will be used to include both.
 */
public class TumorTypeUtils {

    private static final String TUMOR_TYPE_ALL_TUMORS = "all tumors";
    private static final String ONCO_TREE_API_URL = "http://oncotree.mskcc.org/oncotree/api/";
    private static List<OncoTreeType> allOncoTreeCancerTypes = new ArrayList<OncoTreeType>() {{
        addAll(getOncoTreeCancerTypesFromSource());
    }};
    private static List<OncoTreeType> allOncoTreeSubtypes = new ArrayList<OncoTreeType>() {{
        addAll(getOncoTreeSubtypesByCancerTypesFromSource(allOncoTreeCancerTypes));
    }};

    private static Map<String, List<OncoTreeType>> questTumorTypeMap = null;
    private static Map<String, List<OncoTreeType>> cbioTumorTypeMap = null;

    /**
     * Get all exist OncoTree tumor types including cancer types and subtypes
     *
     * @return
     */
    public static List<OncoTreeType> getAllTumorTypes() {
        List<OncoTreeType> oncoTreeTypes = new ArrayList<>();

        oncoTreeTypes.addAll(getAllCancerTypes());
        oncoTreeTypes.addAll(getAllSubtypes());

        return oncoTreeTypes;
    }

    /**
     * Get all exist OncoTree cancer types
     *
     * @return
     */
    public static List<OncoTreeType> getAllCancerTypes() {
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
    public static List<OncoTreeType> getAllSubtypes() {
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
    public static List<OncoTreeType> getAllOncoTreeTypes() {
        List<OncoTreeType> cancerTypes = new ArrayList<>();

        cancerTypes.addAll(getAllOncoTreeCancerTypes());
        cancerTypes.addAll(getAllOncoTreeSubtypes());

        return cancerTypes;
    }

    /**
     * Get all OncoTree cancer types
     *
     * @return
     */
    public static List<OncoTreeType> getAllOncoTreeCancerTypes() {
        return allOncoTreeCancerTypes;
    }

    /**
     * Get all OncoTree subtypes
     *
     * @return
     */
    public static List<OncoTreeType> getAllOncoTreeSubtypes() {
        return allOncoTreeSubtypes;
    }

    /**
     * Get mapped OncoTree cancer types based on cancer type names
     *
     * @param cancerTypes Caner type name queries
     * @return
     */
    public static List<OncoTreeType> getOncoTreeCancerTypes(List<String> cancerTypes) {
        List<OncoTreeType> mapped = new ArrayList<>();

        for (String cancerType : cancerTypes) {
            for (OncoTreeType oncoTreeType : allOncoTreeCancerTypes) {
                if (cancerType.equalsIgnoreCase(oncoTreeType.getCancerType())) {
                    mapped.add(oncoTreeType);
                    break;
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
    public static OncoTreeType getOncoTreeCancerType(String cancerType) {
        List<OncoTreeType> mapped = getOncoTreeCancerTypes(Collections.singletonList(cancerType));

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
    public static List<OncoTreeType> getOncoTreeSubtypesByName(List<String> names) {
        List<OncoTreeType> mapped = new ArrayList<>();

        for (String name : names) {
            for (OncoTreeType oncoTreeType : allOncoTreeSubtypes) {
                if (name.equalsIgnoreCase(oncoTreeType.getSubtype())) {
                    mapped.add(oncoTreeType);
                    break;
                }
            }
        }

        return mapped;
    }

    public static OncoTreeType getOncoTreeSubtypeByName(String name) {
        List<OncoTreeType> matchedSubtypes = getOncoTreeSubtypesByName(Collections.singletonList(name));
        if (matchedSubtypes != null && matchedSubtypes.size() > 0) {
            return matchedSubtypes.get(0);
        }

        List<OncoTreeType> matchedCancerTypes = getOncoTreeCancerTypes(Collections.singletonList(name));
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
    public static List<OncoTreeType> getOncoTreeSubtypesByCode(List<String> codes) {
        List<OncoTreeType> mapped = new ArrayList<>();

        for (String code : codes) {
            for (OncoTreeType oncoTreeType : allOncoTreeSubtypes) {
                if (code.equalsIgnoreCase(oncoTreeType.getCode())) {
                    mapped.add(oncoTreeType);
                    break;
                }
            }
        }

        return mapped;
    }

    public static OncoTreeType getOncoTreeSubtypeByCode(String code) {
        List<OncoTreeType> matchedSubtypes = getOncoTreeSubtypesByCode(Collections.singletonList(code));
        if (matchedSubtypes != null && matchedSubtypes.size() > 0) {
            return matchedSubtypes.get(0);
        }

        List<OncoTreeType> matchedCancerTypes = getOncoTreeCancerTypes(Collections.singletonList(code));
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
    public static OncoTreeType getMappedOncoTreeAllTumor() {
        for (OncoTreeType cancerType : allOncoTreeCancerTypes) {
            if (cancerType.getCancerType().equalsIgnoreCase(TUMOR_TYPE_ALL_TUMORS)) {
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
    public static List<OncoTreeType> generalMapping(List<OncoTreeType> cancerTypeQueries) {
        List<OncoTreeType> matches = new ArrayList<>();
        List<OncoTreeType> allCancerTypes = getAllCancerTypes();
        List<OncoTreeType> allSubtypes = getAllSubtypes();
        OncoTreeType allTumor = getMappedOncoTreeAllTumor();

        for (OncoTreeType query : cancerTypeQueries) {
            if (query != null && !query.equals(allTumor)) {
                for (OncoTreeType subtype : allSubtypes) {
                    if (subtype.equals(query)) {
                        matches.add(subtype);
                    }
                }
                for (OncoTreeType cancerType : allCancerTypes) {
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
    public static List<OncoTreeType> getMappedOncoTreeTypesBySource(String tumorType, String source) {
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
    private static List<OncoTreeType> findTumorTypes(String tumorType, String source) {
        if (source == "cbioportal") {
            return new ArrayList<>(fromCbioportalTumorType(tumorType));
        } else {
            return new ArrayList<>(fromQuestTumorType(tumorType));
        }
    }

    private static Set<OncoTreeType> fromQuestTumorType(String questTumorType) {
        if (questTumorTypeMap == null) {
            questTumorTypeMap = new HashMap<String, List<OncoTreeType>>();

            OncoTreeType tumorTypeAll = getMappedOncoTreeAllTumor();

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
                OncoTreeType oncokbType = getOncoTreeSubtypeByCode(parts[1]);
                if (oncokbType == null) {
                    oncokbType = getOncoTreeCancerType(parts[1]);

                    if (oncokbType == null) {
                        System.err.println("no " + parts[1] + " as tumor type in oncokb");
                        continue;
                    }
                }

                List<OncoTreeType> types = questTumorTypeMap.get(questType);
                if (types == null) {
                    types = new LinkedList<>();
                    questTumorTypeMap.put(questType, types);
                }
                types.add(oncokbType);
            }

            if (tumorTypeAll != null) {
                for (List<OncoTreeType> list : questTumorTypeMap.values()) {
                    list.add(tumorTypeAll);
                }
            }
        }

        questTumorType = questTumorType == null ? null : questTumorType.toLowerCase();

        if (questTumorType == null) {
            return new LinkedHashSet<>();
        }

        List<OncoTreeType> ret = questTumorTypeMap.get(questTumorType);

        if (ret == null || ret.size() == 0) {
            Set<OncoTreeType> oncoTreeTypes = getOncoTreeTypesByTumorType(questTumorType);
            if (oncoTreeTypes != null) {
                ret = new ArrayList<>(oncoTreeTypes);
            }
        }

        return ret == null ? new LinkedHashSet<OncoTreeType>() : new LinkedHashSet<>(ret);
    }

    private static Set<OncoTreeType> getOncoTreeTypesByTumorType(String tumorType) {
        Set<OncoTreeType> types = new HashSet<>();
        OncoTreeType mapped;

        mapped = getOncoTreeSubtypeByCode(tumorType);
        if (mapped == null) {
            mapped = getOncoTreeSubtypeByCode(tumorType);
        }
        if (mapped == null) {
            mapped = getOncoTreeCancerType(tumorType);
        }
        if (mapped != null) {
            types.add(mapped);
        }
        return types;
    }

    private static Set<OncoTreeType> fromCbioportalTumorType(String cbioTumorType) {
        if (cbioTumorTypeMap == null) {
            cbioTumorTypeMap = new HashMap<String, List<OncoTreeType>>();

            OncoTreeType tumorTypeAll = getMappedOncoTreeAllTumor();

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
                    List<OncoTreeType> types = cbioTumorTypeMap.get(cbioType);
                    if (types == null) {
                        types = new LinkedList<>();
                        cbioTumorTypeMap.put(cbioType, types);
                    }

                    for (int i = 1; i < parts.length; i++) {
                        OncoTreeType oncokbType = getOncoTreeSubtypeByCode(parts[i]);
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
                for (List<OncoTreeType> list : cbioTumorTypeMap.values()) {
                    list.add(tumorTypeAll);
                }
            }
        }

        cbioTumorType = cbioTumorType == null ? null : cbioTumorType.toLowerCase();
        
        if (cbioTumorType == null) {
            return new LinkedHashSet<>();
        }
        
        List<OncoTreeType> ret = cbioTumorTypeMap.get(cbioTumorType);

        if (ret == null || ret.size() == 0) {
            Set<OncoTreeType> oncoTreeTypes = getOncoTreeTypesByTumorType(cbioTumorType);
            if (oncoTreeTypes != null) {
                ret = new ArrayList<>(oncoTreeTypes);
            }
        }

        return ret == null ? new LinkedHashSet<OncoTreeType>() : new LinkedHashSet<>(ret);
    }

    private static List<OncoTreeType> getOncoTreeCancerTypesFromSource() {
        String url = ONCO_TREE_API_URL + "mainTypes?version=oncokb";
        List<OncoTreeType> cancerTypes = new ArrayList<>();

        try {
            String json = FileUtils.readRemote(url);
            Map map = JsonUtils.jsonToMap(json);
            List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
            if (data != null) {
                for (Map<String, Object> datum : data) {
                    OncoTreeType cancerType = new OncoTreeType();
                    cancerType.setCancerType((String) datum.get("name"));
                    cancerTypes.add(cancerType);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cancerTypes;
    }

    private static List<OncoTreeType> getOncoTreeSubtypesByCancerTypesFromSource(List<OncoTreeType> cancerTypes) {
        List<OncoTreeType> subtypes = new ArrayList<>();
        try {
            String url = ONCO_TREE_API_URL + "tumorTypes/search";
            URL obj = new URL(url);
            JSONObject postData = new JSONObject();
            postData.put("version", "oncokb");

            JSONArray queries = new JSONArray();
            for (OncoTreeType cancerType : cancerTypes) {
                JSONObject query = new JSONObject();
                query.put("exactMatch", "true");
                query.put("query", cancerType.getCancerType());
                query.put("type", "maintype");
                queries.put(query);
            }
            postData.put("queries", queries);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postData.toString());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            Map map = JsonUtils.jsonToMap(FileUtils.readStream(con.getInputStream()));

            if (map.get("data") != null) {
                for (List<Map<String, Object>> queryResult : (List<List<Map<String, Object>>>) map.get("data")) {
                    for (Map<String, Object> datum : queryResult) {
                        OncoTreeType subtype = new OncoTreeType();
                        Map<String, String> mainType = (Map<String, String>) datum.get("mainType");
                        if (mainType != null) {
                            subtype.setCancerType(mainType.get("name"));
                        }
                        subtype.setSubtype((String) datum.get("name"));
                        subtype.setCode((String) datum.get("code"));
                        subtype.setLevel((String) datum.get("level"));
                        subtype.setTissue((String) datum.get("tissue"));
                        subtypes.add(subtype);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return subtypes;
    }
}