package org.mskcc.cbio.oncokb.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorForm;
import org.mskcc.cbio.oncokb.model.oncotree.MainType;
import org.mskcc.cbio.oncokb.model.oncotree.TumorType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 * <p>
 * In OncoTree, there are two categories: cancerType and subtype. In order to distinguish
 * the difference, tumorType will be used to include both.
 */
public class TumorTypeUtils {
    private static final String ONCO_TREE_ONCOKB_VERSION = "oncotree_2019_03_01";
    private static final String ACCESS_ERROR_ONCO_TREE_MESSAGE = "Error: Cannot access OncoTree service.";
    private static String ONCO_TREE_API_URL = null;
    private static List<TumorType> allOncoTreeCancerTypes = new ArrayList<TumorType>() {{
        addAll(getOncoTreeCancerTypesFromSource());
        addAll(getAllSpecialTumorOncoTreeTypes());
    }};

    private static List<TumorType> allOncoTreeSubtypes = getOncoTreeSubtypesFromSource();
    private static Map<String, TumorType> allNestedOncoTreeSubtypes = getAllNestedOncoTreeSubtypesFromSource();
    private static Map<String, List<TumorType>> questTumorTypeMap = null;
    private static Map<String, List<TumorType>> cbioTumorTypeMap = null;
    private static final ImmutableList<String> LiquidTumorTissues = ImmutableList.of(
        "Lymph", "Blood", "Lymphoid", "Myeloid"
    );
    private static final ImmutableList<String> LiquidTumorMainTypes = ImmutableList.of(
        "Blastic Plasmacytoid Dendritic Cell Neoplasm", "Histiocytosis", "Leukemia", "Multiple Myeloma",
        "Myelodysplasia", "Myeloproliferative Neoplasm", "Mastocytosis", "Hodgkin Lymphoma", "Non-Hodgkin Lymphoma",
        "Blood Cancer, NOS", "Myelodysplastic Syndromes", "Lymphatic Cancer, NOS", " B-Lymphoblastic Leukemia/Lymphoma",
        "Mature B-Cell Neoplasms", "Mature T and NK Neoplasms", "Posttransplant Lymphoproliferative Disorders",
        "T-Lymphoblastic Leukemia/Lymphoma", "Histiocytic Disorder");

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
        if (name != null) {
            List<TumorType> matchedSubtypes = getOncoTreeSubtypesByName(Collections.singletonList(name));
            if (matchedSubtypes != null && matchedSubtypes.size() > 0) {
                return matchedSubtypes.get(0);
            }

            List<TumorType> matchedCancerTypes = getOncoTreeCancerTypes(Collections.singletonList(name));
            if (matchedCancerTypes != null && matchedCancerTypes.size() > 0) {
                return matchedCancerTypes.get(0);
            }
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
                if (code != null) {
                    for (TumorType oncoTreeType : allOncoTreeSubtypes) {
                        if (code.equalsIgnoreCase(oncoTreeType.getCode())) {
                            mapped.add(oncoTreeType);
                            break;
                        }
                    }
                }
            }
        }

        return mapped;
    }

    public static TumorType getOncoTreeSubtypeByCode(String code) {
        if (code != null) {
            List<TumorType> matchedSubtypes = getOncoTreeSubtypesByCode(Collections.singletonList(code));
            if (matchedSubtypes != null && matchedSubtypes.size() > 0) {
                return matchedSubtypes.get(0);
            }
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

    public static Boolean isSolidTumor(TumorType tumorType) {
        TumorForm tumorForm = checkTumorForm(tumorType);
        return tumorForm != null && tumorForm.equals(TumorForm.SOLID);
    }

    public static Boolean isLiquidTumor(TumorType tumorType) {
        TumorForm tumorForm = checkTumorForm(tumorType);
        return tumorForm != null && tumorForm.equals(TumorForm.LIQUID);
    }

    public static Boolean hasSolidTumor(Set<TumorType> tumorTypes) {
        if (tumorTypes == null)
            return null;
        for (TumorType tumorType : tumorTypes) {
            if (isSolidTumor(tumorType)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean hasLiquidTumor(Set<TumorType> tumorTypes) {
        if (tumorTypes == null)
            return null;
        for (TumorType tumorType : tumorTypes) {
            if (isLiquidTumor(tumorType)) {
                return true;
            }
        }
        return false;
    }

    public static List<TumorType> findTumorTypes(String tumorType, String source) {
        LinkedHashSet<TumorType> mappedTumorTypesFromSource = new LinkedHashSet<>();

        // Include exact matched tumor type
        LinkedHashSet<TumorType> oncoTreeTypes = getOncoTreeTypesByTumorType(tumorType);
        mappedTumorTypesFromSource.addAll(oncoTreeTypes);

        // Include all parent nodes
        List<TumorType> parentIncludedMatchByCode = findTumorType(
            allNestedOncoTreeSubtypes.get("TISSUE"), allNestedOncoTreeSubtypes.get("TISSUE"),
            new ArrayList<TumorType>(), "code", tumorType, true, true);
        List<TumorType> parentIncludedMatchByName = findTumorType(
            allNestedOncoTreeSubtypes.get("TISSUE"), allNestedOncoTreeSubtypes.get("TISSUE"),
            new ArrayList<TumorType>(), "name", tumorType, true, true);

        mappedTumorTypesFromSource.addAll(parentIncludedMatchByCode);
        mappedTumorTypesFromSource.addAll(parentIncludedMatchByName);

        // Include all solid tumors
        if (hasSolidTumor(new HashSet<>(mappedTumorTypesFromSource))) {
            mappedTumorTypesFromSource.add(getMappedSpecialTumor(SpecialTumorType.ALL_SOLID_TUMORS));
        }

        // Include all liquid tumors
        if (hasLiquidTumor(new HashSet<>(mappedTumorTypesFromSource))) {
            mappedTumorTypesFromSource.add(getMappedSpecialTumor(SpecialTumorType.ALL_LIQUID_TUMORS));
        }

        if (mappedTumorTypesFromSource.size() == 0 && !com.mysql.jdbc.StringUtils.isNullOrEmpty(tumorType)) {
            // When there is no OncoTree tumor type mapped, temporary check the tumor form
            // TODO: need to find a way for different version of the OncoTree usage.
            TumorType tt = new TumorType();
            tt.setMainType(new MainType(tumorType));
            TumorForm tumorForm = checkTumorForm(tt);
            if (tumorForm.equals(TumorForm.SOLID)) {
                mappedTumorTypesFromSource.add(getMappedSpecialTumor(SpecialTumorType.ALL_SOLID_TUMORS));
            } else {
                mappedTumorTypesFromSource.add(getMappedSpecialTumor(SpecialTumorType.ALL_LIQUID_TUMORS));
            }
        }

        // Include all tumors
        TumorType allTumor = getMappedSpecialTumor(SpecialTumorType.ALL_TUMORS);
        if (allTumor != null) {
            mappedTumorTypesFromSource.add(allTumor);
        }

        return new ArrayList<>(new LinkedHashSet<>(mappedTumorTypesFromSource));
    }

    public static String getOncoTreeVersion() {
        return ONCO_TREE_ONCOKB_VERSION;
    }

    /*-- PRIVATE --*/
    private static List<TumorType> filterOutDiffMainType(List<TumorType> tumorTypes, TumorType searchedTumorType) {
        List<TumorType> filteredResult = new ArrayList<>();
        if (searchedTumorType == null || searchedTumorType.getMainType() == null) {
            return filteredResult;
        }
        for (TumorType tumorType : tumorTypes) {
            if (tumorType.getMainType() != null && tumorType.getMainType().equals(searchedTumorType.getMainType())) {
                filteredResult.add(tumorType);
            }
        }
        return filteredResult;
    }

    private static Integer convertStringToInteger(String string) {
        if (string == null) {
            return null;
        }
        try {
            Integer integer = Integer.parseInt(string.trim());
            return integer;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<TumorType> findTumorType(TumorType allTumorTypes, TumorType currentTumorType, List<TumorType> matchedTumorTypes,
                                                 String key, String keyword, Boolean exactMatch, Boolean includeParent) {
        Map<String, TumorType> childrenTumorTypes = currentTumorType.getChildren();
        Boolean match = false;

        if (includeParent == null) {
            includeParent = false;
        }

        if (exactMatch == null) {
            exactMatch = true;
        }

        switch (key) {
            case "code":
                if (exactMatch) {
                    match = currentTumorType.getCode() == null ? false : currentTumorType.getCode().equalsIgnoreCase(keyword);
                } else {
                    match = currentTumorType.getCode() == null ?
                        false :
                        StringUtils.containsIgnoreCase(currentTumorType.getCode(), keyword);
                }
                break;
            case "color":
                if (exactMatch) {
                    match = currentTumorType.getColor() == null ? false : currentTumorType.getColor().equalsIgnoreCase(keyword);
                } else {
                    match = currentTumorType.getColor() == null ?
                        false :
                        StringUtils.containsIgnoreCase(currentTumorType.getColor(), keyword);
                }
                break;
            case "name":
                if (exactMatch) {
                    match = currentTumorType.getName() == null ? false : currentTumorType.getName().equalsIgnoreCase(keyword);
                } else {
                    match = currentTumorType.getName() == null ?
                        false :
                        StringUtils.containsIgnoreCase(currentTumorType.getName(), keyword);
                }
                break;
            case "nci":
                if (exactMatch) {
                    match = currentTumorType.getNCI() == null ? false : hasMatchingElementIgnoreCase(currentTumorType.getNCI(), keyword);
                } else {
                    match = currentTumorType.getNCI() == null ?
                        false :
                        hasElementWhichContainsStringIgnoreCase(currentTumorType.getNCI(), keyword);
                }
                break;
            case "umls":
                if (exactMatch) {
                    match = currentTumorType.getUMLS() == null ? false : hasMatchingElementIgnoreCase(currentTumorType.getUMLS(), keyword);
                } else {
                    match = currentTumorType.getUMLS() == null ?
                        false :
                        hasElementWhichContainsStringIgnoreCase(currentTumorType.getUMLS(), keyword);
                }
                break;
            case "maintype":
                if (exactMatch) {
                    match = currentTumorType == null ? false :
                        (currentTumorType.getMainType() == null ? false :
                            (currentTumorType.getMainType().getName() == null ? false :
                                currentTumorType.getMainType().getName().equals(keyword)));
                } else {
                    match = currentTumorType == null ? false :
                        (currentTumorType.getMainType() == null ? false :
                            (currentTumorType.getMainType().getName() == null ? false :
                                StringUtils.containsIgnoreCase(currentTumorType.getMainType().getName(), keyword)));
                }
                break;
            case "level":
                Integer keywordAsInteger = convertStringToInteger(keyword);
                match = currentTumorType == null ? false :
                    (currentTumorType.getLevel() == null ? false :
                        (currentTumorType.getLevel() == null ? false :
                            currentTumorType.getLevel().equals(keywordAsInteger)));
                break;
            default:
                if (exactMatch) {
                    match = currentTumorType.getCode() == null ? false : currentTumorType.getCode().equalsIgnoreCase(keyword);
                } else {
                    match = currentTumorType.getCode() == null ?
                        false :
                        StringUtils.containsIgnoreCase(currentTumorType.getCode(), keyword);
                }
        }

        if (match && currentTumorType.getLevel() > 0) {
            TumorType tumorType = new TumorType();
            tumorType.setTissue(currentTumorType.getTissue());
            tumorType.setCode(currentTumorType.getCode());
            tumorType.setName(currentTumorType.getName());
            tumorType.setUMLS(currentTumorType.getUMLS());
            tumorType.setNCI(currentTumorType.getNCI());
            tumorType.setMainType(currentTumorType.getMainType());
            tumorType.setColor(currentTumorType.getColor());
            tumorType.setLevel(currentTumorType.getLevel());
            tumorType.setParent(currentTumorType.getParent());

            matchedTumorTypes.add(tumorType);

            if (includeParent) {
                String code = currentTumorType.getParent();
                List<TumorType> parentTumorTypes = findTumorType(allTumorTypes, allTumorTypes, new ArrayList<TumorType>(), "code", code, true, true);
                if (parentTumorTypes != null && parentTumorTypes.size() > 0) {
                    TumorType parentNode = parentTumorTypes.get(0);
                    matchedTumorTypes.add(parentNode);
                    if (parentNode.getParent() != null) {
                        matchedTumorTypes = findTumorType(allTumorTypes, allTumorTypes, matchedTumorTypes, "code", parentNode.getParent(), true, true);
                    }
                }
            }
        }

        if (childrenTumorTypes.size() > 0) {
            Iterator it = childrenTumorTypes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                matchedTumorTypes = findTumorType(allTumorTypes, (TumorType) pair.getValue(), matchedTumorTypes, key, keyword, exactMatch, includeParent);
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(matchedTumorTypes));
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

    private static LinkedHashSet<TumorType> getOncoTreeTypesByTumorType(String tumorType) {
        LinkedHashSet<TumorType> types = new LinkedHashSet<>();
        TumorType mapped;

        mapped = getOncoTreeSubtypeByCode(tumorType);
        if (mapped == null) {
            mapped = getOncoTreeSubtypeByName(tumorType);
        }

        if (mapped == null) {
            mapped = getOncoTreeCancerType(tumorType);
            if (mapped != null) {
                types.add(mapped);
            }
        } else if (mapped.getMainType() != null) {
            //Also include the cancer type into relevant types
            types.add(mapped);
            types.add(getOncoTreeCancerType(mapped.getMainType().getName()));
        }
        return types;
    }

    private static List<TumorType> getOncoTreeCancerTypesFromSource() {
        List<TumorType> cancerTypes = new ArrayList<>();
        try {
            String json = IOUtils.toString(new InputStreamReader(TumorTypeUtils.class.getResourceAsStream("/data/oncotree-maintypes.json")));
            List<String> mainTypes = JsonUtils.jsonToArray(json);
            for (String str : mainTypes) {
                TumorType cancerType = new TumorType();
                MainType mainType = new MainType(str);
                cancerType.setMainType(mainType);
                cancerTypes.add(cancerType);
            }
        } catch (Exception e) {
            System.out.println("You need to include oncotree nested file. Endpoint: mainTypes?version=" + ONCO_TREE_ONCOKB_VERSION);
            e.printStackTrace();
        }
        return cancerTypes;
    }

    private static Map<String, TumorType> getAllNestedOncoTreeSubtypesFromSource() {
        String url = getOncoTreeApiUrl() + "tumorTypes?version=" + ONCO_TREE_ONCOKB_VERSION + "&flat=false";
        Map<String, TumorType> result = new HashMap<>();
        try {
            String json = IOUtils.toString(new InputStreamReader(TumorTypeUtils.class.getResourceAsStream("/data/oncotree-tumortypes.json")));
            Map map = JsonUtils.jsonToMap(json);
            Map<String, org.mskcc.oncotree.model.TumorType> data = (Map<String, org.mskcc.oncotree.model.TumorType>) map;
            org.mskcc.oncotree.model.TumorType tumorType = new ObjectMapper().convertValue(data.get("TISSUE"), org.mskcc.oncotree.model.TumorType.class);
            result.put("TISSUE", new TumorType(tumorType));
        } catch (Exception e) {
            System.out.println("You need to include oncotree nested file. Endpoint: tumorTypes?version=" + ONCO_TREE_ONCOKB_VERSION + "&flat=false");
            e.printStackTrace();
        }
        return result;
    }

    private static List<TumorType> getOncoTreeSubtypesFromSource() {
        List<TumorType> tumorTypes = new ArrayList<>();
        try {
            Gson gson = new GsonBuilder().create();
            org.mskcc.oncotree.model.TumorType[] tumorType = gson.fromJson(new BufferedReader(new InputStreamReader(TumorTypeUtils.class.getResourceAsStream("/data/oncotree-tumortypes-flat.json"))), org.mskcc.oncotree.model.TumorType[].class);
            for (org.mskcc.oncotree.model.TumorType tt : Arrays.asList(tumorType)) {
                tumorTypes.add(new TumorType(tt));
            }
        } catch (Exception e) {
            System.out.println("You need to include oncotree flat file. Endpoint: tumorTypes?version=" + ONCO_TREE_ONCOKB_VERSION + "&flat=true");
            e.printStackTrace();
        }
        return tumorTypes;
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
            ONCO_TREE_API_URL = PropertiesUtils.getProperties("oncotree.api");
            if (ONCO_TREE_API_URL != null) {
                ONCO_TREE_API_URL = ONCO_TREE_API_URL.trim();
            }
            if (ONCO_TREE_API_URL == null || ONCO_TREE_API_URL.isEmpty()) {
                ONCO_TREE_API_URL = "http://oncotree.mskcc.org/oncotree/api/";
            }
        }
        return ONCO_TREE_API_URL;
    }

    private static TumorForm checkTumorForm(TumorType tumorType) {
        if (tumorType == null)
            return null;
        // This is mainly for subtypes
        if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(tumorType.getTissue())) {
            if (LiquidTumorTissues.contains(tumorType.getTissue()))
                return TumorForm.LIQUID;
            else
                return TumorForm.SOLID;
        }
        if (tumorType.getMainType() != null && !com.mysql.jdbc.StringUtils.isNullOrEmpty(tumorType.getMainType().getName())) {
            if (LiquidTumorMainTypes.contains(tumorType.getMainType().getName()))
                return TumorForm.LIQUID;
            else
                return TumorForm.SOLID;
        }
        return null;
    }

    private static boolean hasMatchingElementIgnoreCase(List<String> list, String soughtFor) {
        for (String current : list) {
            if (current.equalsIgnoreCase(soughtFor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasElementWhichContainsStringIgnoreCase(List<String> list, String soughtFor) {
        for (String current : list) {
            if (StringUtils.containsIgnoreCase(current, soughtFor)) {
                return true;
            }
        }
        return false;
    }
}
