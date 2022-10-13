package org.mskcc.cbio.oncokb.bo.impl;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.CacheUtils;

import java.util.*;
import java.util.stream.Collectors;


public class TumorTypeBoImpl extends GenericBoImpl<TumorType, TumorTypeDao> implements TumorTypeBo {

    @Override
    public TumorType getByCode(String code) {
        if (StringUtils.isEmpty(code)) return null;
        String finalCode = code.toUpperCase();
        return CacheUtils.getCodedTumorTypeMap().get(finalCode);
    }

    @Override
    public TumorType getByMainType(String mainType) {
        if (StringUtils.isEmpty(mainType)) return null;
        String lowercaseMainType = mainType.toLowerCase();
        return CacheUtils.getMainTypeTumorTypeMap().get(lowercaseMainType);
    }

    @Override
    public TumorType getByName(String name) {
        if (StringUtils.isEmpty(name)) return null;
        TumorType tumorType = getByCode(name);
        if (tumorType != null) {
            return tumorType;
        }
        tumorType = getBySubtype(name);
        tumorType = tumorType == null ? getByMainType(name) : tumorType;
        tumorType = tumorType == null ? getBySpecialTumor(getSpecialTumorTypeByName(name)) : tumorType;
        return tumorType;
    }

    @Override
    public TumorType getBySpecialTumor(SpecialTumorType specialTumorType) {
        if (specialTumorType == null) return null;
        return getAllSpecialTumorOncoTreeTypes().stream().filter(cancerType -> cancerType.getMainType().equals(specialTumorType.getTumorType())).findAny().orElse(null);
    }

    @Override
    public TumorType getBySubtype(String subtype) {
        if (StringUtils.isEmpty(subtype)) return null;
        String lowercaseName = subtype.toLowerCase();
        return CacheUtils.getLowercaseSubtypeTumorTypeMap().get(lowercaseName);
    }

    @Override
    public List<TumorType> getAllSubtypesByMainType(String mainType) {
        if (StringUtils.isEmpty(mainType)) return new ArrayList<>();
        return getAllSubtypes().stream().filter(cancerType -> mainType.equals(cancerType.getMainType())).collect(Collectors.toList());
    }

    @Override
    public List<TumorType> getAllMainTypes() {
        return CacheUtils.getAllMainTypes();
    }

    @Override
    public List<TumorType> getAllSpecialTumorOncoTreeTypes() {
        return CacheUtils.getAllSpecialCancerTypes();
    }

    @Override
    public List<TumorType> getAllSubtypes() {
        return CacheUtils.getAllSubtypes();
    }

    @Override
    public List<TumorType> getAllTumorTypes() {
        return CacheUtils.getAllCancerTypes().stream().filter(tumorType -> StringUtils.isEmpty(tumorType.getCode()) || tumorType.getLevel() > 0).collect(Collectors.toList());
    }

    @Override
    public SpecialTumorType getSpecialTumorTypeByName(String name) {
        return SpecialTumorType.getByTumorType(name);
    }
}
