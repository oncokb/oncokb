package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.io.IOException;
import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 */
public class TumorTypeUtils {

    private static final String TUMOR_TYPE_ALL_TUMORS = "all tumors";
    private static Map<String, List<TumorType>> questTumorTypeMap = null;
    private static Map<String, List<TumorType>> cbioTumorTypeMap = null;

    public static Set<TumorType> fromQuestTumorType(String questTumorType) {
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        if (questTumorTypeMap==null) {
            questTumorTypeMap = new HashMap<String, List<TumorType>>();

            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);

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
                TumorType oncokbType = tumorTypeBo.findTumorTypeByName(parts[1]);
                if (oncokbType==null) {
                    System.err.println("no "+parts[1]+" as tumor type in oncokb");
                    continue;
                }

                List<TumorType> types = questTumorTypeMap.get(questType);
                if (types==null) {
                    types = new LinkedList<TumorType>();
                    questTumorTypeMap.put(questType, types);
                }
                types.add(oncokbType);
            }

            if(tumorTypeAll != null) {
                for (List<TumorType> list : questTumorTypeMap.values()) {
                    list.add(tumorTypeAll);
                }
            }
        }

        questTumorType = questTumorType==null ? null : questTumorType.toLowerCase();

        List<TumorType> ret = questTumorTypeMap.get(questTumorType);
        if (ret == null) {
            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);
            ret = new LinkedList<TumorType>();
            if(tumorTypeAll != null) {
                ret.add(tumorTypeAll);
            }
        }

        TumorType extactMatchedTumorType = tumorTypeBo.findTumorTypeByName(questTumorType);
        if(extactMatchedTumorType!=null && !ret.contains(extactMatchedTumorType)) {
            ret.add(0, extactMatchedTumorType);
        }

        return new LinkedHashSet<TumorType>(ret);
    }

    public static Set<TumorType> fromCbioportalTumorType(String cbioTumorType) {
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        if (cbioTumorTypeMap==null) {
            cbioTumorTypeMap = new HashMap<String, List<TumorType>>();

            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);

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
                if(parts.length > 1) {
                    String cbioType = parts[0].toLowerCase();
                    List<TumorType> types = cbioTumorTypeMap.get(cbioType);
                    if (types==null) {
                        types = new LinkedList<TumorType>();
                        cbioTumorTypeMap.put(cbioType, types);
                    }

                    for (int i = 1; i < parts.length; i++) {
                        TumorType oncokbType = tumorTypeBo.findTumorTypeByName(parts[i]);
                        if (oncokbType==null) {
                            System.err.println("no " + parts[i] + " as tumor type in oncokb");
                            continue;
                        }
                        types.add(oncokbType);
                    }
                }
            }

            if(tumorTypeAll != null) {
                for (List<TumorType> list : cbioTumorTypeMap.values()) {
                    list.add(tumorTypeAll);
                }
            }
        }

        cbioTumorType = cbioTumorType==null ? null : cbioTumorType.toLowerCase();

        List<TumorType> ret = cbioTumorTypeMap.get(cbioTumorType);
        if (ret == null) {
            TumorType tumorTypeAll = tumorTypeBo.findTumorTypeByName(TUMOR_TYPE_ALL_TUMORS);
            ret = new LinkedList<TumorType>();
            if(tumorTypeAll != null) {
                ret.add(tumorTypeAll);
            }
        }

        TumorType extactMatchedTumorType = tumorTypeBo.findTumorTypeByName(cbioTumorType);
        if(extactMatchedTumorType!=null && !ret.contains(extactMatchedTumorType)) {
            ret.add(0, extactMatchedTumorType);
        }

        return new LinkedHashSet<TumorType>(ret);
    }

    public static List<TumorType> getTumorTypes(String tumorType, String source) {
        if(CacheUtils.isEnabled()) {
            if (!CacheUtils.containMappedTumorTypes(tumorType, source)) {
                CacheUtils.setMappedTumorTypes(tumorType, source, findTumorTypes(tumorType, source));
            }
            return  CacheUtils.getMappedTumorTypes(tumorType, source);
        }else {
            return findTumorTypes(tumorType, source);
        }
    }

    private static List<TumorType> findTumorTypes(String tumorType, String source) {
        List<TumorType> tumorTypes = new ArrayList<>();
        switch (source) {
            case "cbioportal":
                tumorTypes.addAll(fromCbioportalTumorType(tumorType));
                break;
            default:
                tumorTypes.addAll(fromQuestTumorType(tumorType));
                break;
        }

        return tumorTypes;
    }
}
