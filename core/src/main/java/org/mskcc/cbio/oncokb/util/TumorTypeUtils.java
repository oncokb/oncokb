package org.mskcc.cbio.oncokb.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.RelevantTumorTypeDirection;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorForm;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;


public class TumorTypeUtils {
    private static final String ONCO_TREE_ONCOKB_VERSION = "oncotree_2019_12_01";
    private static String ONCO_TREE_API_URL = null;
    private static final ImmutableList<String> LiquidTumorTissues = ImmutableList.of(
        "Lymph", "Blood", "Lymphoid", "Myeloid"
    );

    public static List<TumorType> getAllTumorTypes() {
        return ApplicationContextSingleton.getTumorTypeBo().findAll().stream().filter(tumorType -> StringUtils.isEmpty(tumorType.getCode()) || tumorType.getLevel() > 1).collect(Collectors.toList());
    }

    /**
     * Get all OncoTree subtypes
     *
     * @return
     */
    public static List<TumorType> getAllSubtypes() {
        return ApplicationContextSingleton.getTumorTypeBo().findAll().stream().filter(tumorType -> StringUtils.isNotEmpty(tumorType.getCode()) && tumorType.getLevel() > 1).collect(Collectors.toList());
    }

    /**
     * Get all OncoTree main types
     */
    public static List<TumorType> getAllMainTypes() {
        return ApplicationContextSingleton.getTumorTypeBo().findAll().stream().filter(tumorType -> StringUtils.isEmpty(tumorType.getCode())).collect(Collectors.toList());
    }

    public static TumorType getByName(String name) {
        if (StringUtils.isEmpty(name)) return null;
        TumorType subtype = getBySubtype(name);
        return subtype == null ? getByMainType(name) : subtype;
    }

    public static TumorType getBySubtype(String subtype) {
        if (StringUtils.isEmpty(subtype)) return null;
        String lowercaseName = subtype.toLowerCase();
        Optional<TumorType> matchedSubtypeOptional = getAllSubtypes().stream().filter(tumorType -> StringUtils.isNotEmpty(tumorType.getSubtype()) && tumorType.getSubtype().toLowerCase().equals(lowercaseName)).findAny();
        return matchedSubtypeOptional.isPresent() ? matchedSubtypeOptional.get() : null;
    }

    public static TumorType getByMainType(String mainType) {
        if (StringUtils.isEmpty(mainType)) return null;
        String lowercaseMainType = mainType.toLowerCase();
        Optional<TumorType> matchedMainTypeOptional = getAllMainTypes().stream().filter(tumorType -> StringUtils.isNotEmpty(tumorType.getMainType()) && tumorType.getMainType().toLowerCase().equals(lowercaseMainType)).findAny();
        return matchedMainTypeOptional.isPresent() ? matchedMainTypeOptional.get() : null;
    }

    public static TumorType getByCode(String code) {
        if (StringUtils.isEmpty(code)) return null;
        return getAllSubtypes().stream().filter(subtype -> subtype.getCode().equals(code.toUpperCase())).findAny().orElse(null);
    }

    public static TumorType getBySpecialTumor(SpecialTumorType specialTumorType) {
        if (specialTumorType == null) return null;
        return getAllMainTypes().stream().filter(mainType -> StringUtils.isNotEmpty(mainType.getMainType()) && mainType.getMainType().toLowerCase().equals(specialTumorType.getTumorType().toLowerCase())).findAny().orElse(null);
    }

    public static Boolean isSolidTumor(TumorType tumorType) {
        return isDesiredTumorForm(tumorType, TumorForm.SOLID);
    }

    public static Boolean isLiquidTumor(TumorType tumorType) {
        return isDesiredTumorForm(tumorType, TumorForm.LIQUID);
    }

    private static boolean isDesiredTumorForm(TumorType tumorType, TumorForm tumorForm) {
        if (tumorForm == null || tumorType == null) {
            return false;
        }

        // This is mainly for the tissue
        if (tumorType.getTumorForm() != null) {
            return tumorForm.equals(tumorType.getTumorForm());
        }

        // when the code is null, we need to validate the main type
        if (tumorType.getCode() == null) {
            TumorForm mainTypeTumorForm = getTumorForm(tumorType.getMainType());
            if (tumorType.getMainType() != null && mainTypeTumorForm != null) {
                return mainTypeTumorForm.equals(tumorForm);
            }
        } else {
            return tumorType.getTumorForm() != null && tumorType.getTumorForm().equals(tumorForm);
        }
        return false;
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

    public static List<TumorType> findRelevantTumorTypes(String tumorType) {
        return findRelevantTumorTypes(tumorType, RelevantTumorTypeDirection.UPWARD);
    }

    public static LinkedHashSet<TumorType> getParentTumorTypes(TumorType tumorType, boolean onlySameMaintype) {
        if (tumorType == null || tumorType.getParent() == null) return new LinkedHashSet<>();
        LinkedHashSet parentTumorTypes = new LinkedHashSet();
        if (tumorType.getParent() != null && tumorType.getLevel() > 2) {
            if (!onlySameMaintype || tumorType.getParent().getMainType().equals(tumorType.getMainType())) {
                parentTumorTypes.add(tumorType.getParent());
            }
            parentTumorTypes.addAll(getParentTumorTypes(tumorType.getParent(), onlySameMaintype));
        }
        return parentTumorTypes;
    }

    public static LinkedHashSet<TumorType> getChildTumorTypes(TumorType tumorType, boolean onlySameMaintype) {
        if (tumorType == null || tumorType.getChildren().isEmpty()) return new LinkedHashSet<>();
        LinkedHashSet<TumorType> childTumorTypes = new LinkedHashSet();
        if (onlySameMaintype) {
            childTumorTypes.addAll(tumorType.getChildren().stream().filter(child -> child.getMainType().equals(tumorType.getMainType())).collect(Collectors.toList()));
        } else {
            childTumorTypes.addAll(tumorType.getChildren());
        }
        tumorType.getChildren().forEach(child -> childTumorTypes.addAll(getChildTumorTypes(child, onlySameMaintype)));
        return childTumorTypes;
    }

    public static List<TumorType> findRelevantTumorTypes(String tumorType, RelevantTumorTypeDirection direction) {
        LinkedHashSet<TumorType> mappedTumorTypes = new LinkedHashSet<>();
        TumorType matchedTumorType = getByCode(tumorType);
        if (matchedTumorType == null) {
            matchedTumorType = getBySubtype(tumorType);
        }

        if (direction.equals(RelevantTumorTypeDirection.UPWARD)) {
            if (matchedTumorType != null) {
                // Add matched tumor type
                mappedTumorTypes.add(matchedTumorType);

                // Add main type
                TumorType matchedMainType = getByMainType(matchedTumorType.getMainType());
                if (matchedMainType != null) {
                    mappedTumorTypes.add(matchedMainType);
                }
                // Add matched parent tumor types
                mappedTumorTypes.addAll(getParentTumorTypes(matchedTumorType, true));
            } else {
                matchedTumorType = getByMainType(tumorType);
                if (matchedTumorType != null) {
                    mappedTumorTypes.add(matchedTumorType);
                }
            }
        } else {
            if (matchedTumorType != null) {
                // Add matched tumor type
                mappedTumorTypes.add(matchedTumorType);

                // Add main type
                TumorType matchedMainType = getByMainType(matchedTumorType.getMainType());
                if (matchedMainType != null) {
                    mappedTumorTypes.add(matchedMainType);
                }

                // Add matched parent tumor types
                mappedTumorTypes.addAll(getChildTumorTypes(matchedTumorType, true));
            }
        }

        // Include all solid tumors
        if (hasSolidTumor(new HashSet<>(mappedTumorTypes))) {
            mappedTumorTypes.add(getBySpecialTumor(SpecialTumorType.ALL_SOLID_TUMORS));
        }

        // Include all liquid tumors
        if (hasLiquidTumor(new HashSet<>(mappedTumorTypes))) {
            mappedTumorTypes.add(getBySpecialTumor(SpecialTumorType.ALL_LIQUID_TUMORS));
        }

        // Include all tumors
        TumorType allTumor = getBySpecialTumor(SpecialTumorType.ALL_TUMORS);
        if (allTumor != null) {
            mappedTumorTypes.add(allTumor);
        }

        return new ArrayList<>(new LinkedHashSet<>(mappedTumorTypes));
    }

    public static String getTumorTypeName(TumorType tumorType) {
        if (tumorType == null) {
            return "";
        } else {
            if (tumorType.getSubtype() != null) {
                return tumorType.getSubtype();
            } else if (tumorType.getMainType() != null && tumorType.getMainType() != null) {
                return tumorType.getMainType();
            } else {
                return "";
            }
        }
    }

    public static String getTumorTypesName(Collection<TumorType> tumorTypes) {
        return tumorTypes.stream().map(tumorType -> getTumorTypeName(tumorType)).collect(Collectors.joining(", "));
    }

    public static String getOncoTreeVersion() {
        return ONCO_TREE_ONCOKB_VERSION;
    }

    public static Map<String, org.mskcc.oncotree.model.TumorType> getAllNestedOncoTreeSubtypesFromSource() {
        String url = getOncoTreeApiUrl() + "tumorTypes?version=" + ONCO_TREE_ONCOKB_VERSION + "&flat=false";
        Map<String, org.mskcc.oncotree.model.TumorType> result = new HashMap<>();
        try {
            String json = IOUtils.toString(new InputStreamReader(TumorTypeUtils.class.getResourceAsStream("/data/oncotree/tumortypes.json")));
            Map map = JsonUtils.jsonToMap(json);
            Map<String, org.mskcc.oncotree.model.TumorType> data = (Map<String, org.mskcc.oncotree.model.TumorType>) map;
            org.mskcc.oncotree.model.TumorType oncoTreeTumorType = new ObjectMapper().convertValue(data.get("TISSUE"), org.mskcc.oncotree.model.TumorType.class);
            result.put("TISSUE", oncoTreeTumorType);
        } catch (Exception e) {
            System.out.println("You need to include oncotree nested file. Endpoint: tumorTypes?version=" + ONCO_TREE_ONCOKB_VERSION + "&flat=false");
            e.printStackTrace();
        }
        return result;
    }

    public static List<TumorType> getOncoTreeSubtypesFromSource() {
        List<TumorType> tumorTypes = new ArrayList<>();
        try {
            Gson gson = new GsonBuilder().create();
            org.mskcc.oncotree.model.TumorType[] oncoTreeTumorTypes = gson.fromJson(new BufferedReader(new InputStreamReader(TumorTypeUtils.class.getResourceAsStream("/data/oncotree/tumortypes-flat.json"))), org.mskcc.oncotree.model.TumorType[].class);
            for (org.mskcc.oncotree.model.TumorType oncotreeTumorType : Arrays.asList(oncoTreeTumorTypes)) {
                TumorType tumorType = new TumorType(oncotreeTumorType);
                if (tumorType.getLevel() != null && tumorType.getLevel() == 0) {
                    tumorType.setTumorForm(TumorForm.MIXED);
                } else {
                    tumorType.setTumorForm(getTumorForm(tumorType.getTissue()));
                }
                tumorTypes.add(tumorType);
            }
        } catch (Exception e) {
            System.out.println("You need to include oncotree flat file. Endpoint: tumorTypes?version=" + ONCO_TREE_ONCOKB_VERSION + "&flat=true");
            e.printStackTrace();
        }
        return tumorTypes;
    }

    public static Set<TumorType> getAllSpecialTumorOncoTreeTypes() {
        Set<TumorType> types = new HashSet<>();
        for (SpecialTumorType specialTumorType : SpecialTumorType.values()) {
            TumorType tumorType = new TumorType();
            tumorType.setMainType(specialTumorType.getTumorType());
            tumorType.setTumorForm(getTumorForm(specialTumorType));
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

    public static TumorForm getTumorForm(SpecialTumorType specialTumorType) {
        if (specialTumorType == null)
            return null;

        if (specialTumorType.equals(SpecialTumorType.ALL_LIQUID_TUMORS) || specialTumorType.equals(SpecialTumorType.OTHER_LIQUID_TUMOR_TYPES)) {
            return TumorForm.LIQUID;
        } else if (specialTumorType.equals(SpecialTumorType.ALL_SOLID_TUMORS) || specialTumorType.equals(SpecialTumorType.OTHER_SOLID_TUMOR_TYPES)) {
            return TumorForm.SOLID;
        } else {
            return TumorForm.MIXED;
        }
    }

    public static TumorForm getTumorForm(String tissue) {
        if (!com.mysql.jdbc.StringUtils.isNullOrEmpty(tissue)) {
            if (LiquidTumorTissues.contains(tissue))
                return TumorForm.LIQUID;
            else
                return TumorForm.SOLID;
        }
        return null;
    }

    private static TumorForm getTumorForm(TumorType tumorType) {
        if (tumorType.getTumorForm() != null) {
            return tumorType.getTumorForm();
        }
        return null;
    }

    public static TumorForm checkTumorForm(Set<TumorType> tumorTypes) {
        if (tumorTypes == null)
            return null;
        Set<TumorForm> uniqueTumorForms = tumorTypes.stream()
            .filter(tumorType -> {
                TumorForm tumorForm = getTumorForm(tumorType);
                return tumorForm != null;
            })
            .map(tumorType -> getTumorForm(tumorType)).collect(Collectors.toSet());
        if (uniqueTumorForms.size() > 1 || uniqueTumorForms.size() == 0) {
            // There are ambiguous tumor forms
            return null;
        } else {
            return uniqueTumorForms.iterator().next();
        }
    }
}
