package org.mskcc.cbio.oncokb.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.model.OncoTreeType;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Hongxin on 8/10/15.
 * <p>
 * In OncoTree, there are two categories: cancerType and subtype. In order to distinguish
 * the difference, tumorType will be used to include both.
 */
public class TumorTypeUtils {

    private static final String TUMOR_TYPE_ALL_TUMORS = "all tumors";
    private static final String ONCO_TREE_API_URL = "http://oncotree.mskcc.org/oncotree/api/";
    private static List<OncoTreeType> cancerTypes = new ArrayList<>();
//    private static Map<String, List<TumorType>> questTumorTypeMap = null;
//    private static Map<String, List<TumorType>> cbioTumorTypeMap = null;

//    public static Set<TumorType> fromQuestTumorType(String questTumorType) {
//        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
//        if (questTumorTypeMap==null) {
//            questTumorTypeMap = new HashMap<String, List<TumorType>>();
//
//            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);
//
//            List<String> lines;
//            try {
//                lines = FileUtils.readTrimedLinesStream(TumorTypeUtils.class.getResourceAsStream("/data/quest-tumor-types.txt"));
//            } catch (IOException e) {
//                e.printStackTrace();
//                return Collections.singleton(tumorTypeAll);
//            }
//            for (String line : lines) {
//                if (line.startsWith("#")) {
//                    continue;
//                }
//
//                String[] parts = line.split("\t");
//                String questType = parts[0].toLowerCase();
//                TumorType oncokbType = tumorTypeBo.findTumorTypeByName(parts[1]);
//                if (oncokbType==null) {
//                    System.err.println("no "+parts[1]+" as tumor type in oncokb");
//                    continue;
//                }
//
//                List<TumorType> types = questTumorTypeMap.get(questType);
//                if (types==null) {
//                    types = new LinkedList<TumorType>();
//                    questTumorTypeMap.put(questType, types);
//                }
//                types.add(oncokbType);
//            }
//
//            if(tumorTypeAll != null) {
//                for (List<TumorType> list : questTumorTypeMap.values()) {
//                    list.add(tumorTypeAll);
//                }
//            }
//        }
//
//        questTumorType = questTumorType==null ? null : questTumorType.toLowerCase();
//
//        List<TumorType> ret = questTumorTypeMap.get(questTumorType);
//        if (ret == null) {
//            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);
//            ret = new LinkedList<TumorType>();
//            if(tumorTypeAll != null) {
//                ret.add(tumorTypeAll);
//            }
//        }
//
//        TumorType extactMatchedTumorType = tumorTypeBo.findTumorTypeByName(questTumorType);
//        if(extactMatchedTumorType!=null && !ret.contains(extactMatchedTumorType)) {
//            ret.add(0, extactMatchedTumorType);
//        }
//
//        return new LinkedHashSet<TumorType>(ret);
//    }

//    public static Set<TumorType> fromCbioportalTumorType(String cbioTumorType) {
//        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
//        if (cbioTumorTypeMap==null) {
//            cbioTumorTypeMap = new HashMap<String, List<TumorType>>();
//
//            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);
//
//            List<String> lines;
//            try {
//                lines = FileUtils.readTrimedLinesStream(
//                        TumorTypeUtils.class.getResourceAsStream("/data/cbioportal-tumor-types.txt"));
//            } catch (IOException e) {
//                e.printStackTrace();
//                return Collections.singleton(tumorTypeAll);
//            }
//            for (String line : lines) {
//                if (line.startsWith("#")) {
//                    continue;
//                }
//
//                String[] parts = line.split("\t");
//                if(parts.length > 2) {
//                    String cbioType = parts[0].toLowerCase();
//                    List<TumorType> types = cbioTumorTypeMap.get(cbioType);
//                    if (types==null) {
//                        types = new LinkedList<TumorType>();
//                        cbioTumorTypeMap.put(cbioType, types);
//                    }
//
//                    for (int i = 1; i < parts.length; i++) {
//                        TumorType oncokbType = tumorTypeBo.findTumorTypeByName(parts[i]);
//                        if (oncokbType==null) {
//                            System.err.println("no " + parts[i] + " as tumor type in oncokb");
//                            continue;
//                        }
//                        types.add(oncokbType);
//                    }
//                }
//            }
//
//            if(tumorTypeAll != null) {
//                for (List<TumorType> list : cbioTumorTypeMap.values()) {
//                    list.add(tumorTypeAll);
//                }
//            }
//        }
//
//        cbioTumorType = cbioTumorType==null ? null : cbioTumorType.toLowerCase();
//
//        List<TumorType> ret = cbioTumorTypeMap.get(cbioTumorType);
//        if (ret == null) {
//            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);
//            ret = new LinkedList<TumorType>();
//            if(tumorTypeAll != null) {
//                ret.add(tumorTypeAll);
//            }
//        }
//
//        TumorType extactMatchedTumorType = tumorTypeBo.findTumorTypeByName(cbioTumorType);
//        if(extactMatchedTumorType!=null && !ret.contains(extactMatchedTumorType)) {
//            ret.add(0, extactMatchedTumorType);
//        }
//
//        return new LinkedHashSet<TumorType>(ret);
//    }

    public static List<OncoTreeType> generalMapping(List<OncoTreeType> cancerTypeQueries) {
        List<OncoTreeType> matches = new ArrayList<>();
        List<OncoTreeType> allCancerTypes = CacheUtils.getAllCancerTypes();
        List<OncoTreeType> allSubtypes = CacheUtils.getAllSubtypes();
        OncoTreeType allTumor = getAllTumor();

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
        matches.add(getAllTumor());
        return matches;
    }

    public static List<OncoTreeType> getTumorTypes(String tumorType, String source) {
        if (CacheUtils.isEnabled()) {
            if (!CacheUtils.containMappedTumorTypes(tumorType, source)) {
                CacheUtils.setMappedTumorTypes(tumorType, source, findTumorTypes(tumorType, source));
            }
            return CacheUtils.getMappedTumorTypes(tumorType, source);
        } else {
            return findTumorTypes(tumorType, source);
        }
    }

    private static List<OncoTreeType> findTumorTypes(String tumorType, String source) {
        List<OncoTreeType> tumorTypes = getOncoTreeSubtypes(Collections.singletonList(tumorType));

        if (tumorTypes == null || tumorTypes.size() == 0) {
            tumorTypes = getOncoTreeCancerTypes(Collections.singletonList(tumorType));
        }

        return tumorTypes;
    }

    public static List<OncoTreeType> getAllTumorTypes() {
        List<OncoTreeType> oncoTreeTypes = new ArrayList<>();
        oncoTreeTypes.addAll(getAllOncoTreeCancerTypes());
        oncoTreeTypes.addAll(getAllOncoTreeSubtypes());
        return oncoTreeTypes;
    }

    public static List<OncoTreeType> getAllOncoTreeCancerTypes() {
        return CacheUtils.getAllCancerTypes();
    }

    public static List<OncoTreeType> getAllOncoTreeSubtypes() {
        return CacheUtils.getAllSubtypes();
    }

    public static List<OncoTreeType> findOncoTreeTypesByCancerTypes(List<String> cancerTypes) {
        List<OncoTreeType> oncoTreeTypes = CacheUtils.getAllCancerTypes();
        List<OncoTreeType> mapped = new ArrayList<>();

        for (String cancerType : cancerTypes) {
            for (OncoTreeType oncoTreeType : oncoTreeTypes) {
                if (cancerType.equalsIgnoreCase(oncoTreeType.getCancerType())) {
                    mapped.add(oncoTreeType);
                    break;
                }
            }
        }

        return mapped;
    }

    public static List<OncoTreeType> findOncoTreeTypesBySubtypeNames(List<String> subtypes) {
        List<OncoTreeType> oncoTreeTypes = CacheUtils.getAllSubtypes();
        List<OncoTreeType> mapped = new ArrayList<>();

        for (String subtype : subtypes) {
            for (OncoTreeType oncoTreeType : oncoTreeTypes) {
                if (subtype.equalsIgnoreCase(oncoTreeType.getSubtype())) {
                    mapped.add(oncoTreeType);
                    break;
                }
            }
        }

        return mapped;
    }
    
    public static List<OncoTreeType> findOncoTreeTypesBySubtypeCodes(List<String> codes) {
        List<OncoTreeType> oncoTreeTypes = CacheUtils.getAllSubtypes();
        List<OncoTreeType> mapped = new ArrayList<>();

        for (String code : codes) {
            for (OncoTreeType oncoTreeType : oncoTreeTypes) {
                if (code.equalsIgnoreCase(oncoTreeType.getCode())) {
                    mapped.add(oncoTreeType);
                    break;
                }
            }
        }

        return mapped;
    }

    public static OncoTreeType getAllTumor() {
        List<OncoTreeType> cancerTypes = CacheUtils.getAllCancerTypes();

        for (OncoTreeType cancerType : cancerTypes) {
            if (cancerType.getCancerType().equalsIgnoreCase(TUMOR_TYPE_ALL_TUMORS)) {
                return cancerType;
            }
        }
        return null;
    }
    
    public static OncoTreeType findSingleOncoTreeTypeByCode(String code) {
        List<OncoTreeType> matchedSubtypes = getOncoTreeSubtypes(Collections.singletonList(code));
        if (matchedSubtypes != null) {
            return matchedSubtypes.get(0);
        }

        List<OncoTreeType> matchedCancerTypes = TumorTypeUtils.getOncoTreeCancerTypes(Collections.singletonList(code));
        if (matchedCancerTypes != null) {
            return matchedCancerTypes.get(0);
        }
        
        return null;
    }

    public static List<OncoTreeType> getOncoTreeCancerTypesFromSource() {
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

    public static List<OncoTreeType> getOncoTreeSubtypesByMainTypesFromSource(List<OncoTreeType> cancerTypes) {
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

            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "con");

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

            List<JSONObject> data = (List<JSONObject>) map.get("data");

            if (data != null) {
                for (JSONObject datum : data) {
                    OncoTreeType subtype = new OncoTreeType();
                    JSONObject mainType = (JSONObject) datum.get("mainType");
                    if (mainType != null) {
                        subtype.setCancerType((String) mainType.get("name"));
                    }
                    subtype.setSubtype((String) datum.get("name"));
                    subtype.setCode((String) datum.get("code"));
                    subtype.setLevel((String) datum.get("level"));
                    subtype.setTissue((String) datum.get("tissue"));
                    subtypes.add(subtype);
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return subtypes;
    }
}