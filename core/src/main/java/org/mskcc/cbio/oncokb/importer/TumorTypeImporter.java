package org.mskcc.cbio.oncokb.importer;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;

import java.util.*;

public class TumorTypeImporter {
    private TumorTypeImporter() {
        throw new AssertionError();
    }

    public static void main(String[] args) {
        List<TumorType> tumorTypes = TumorTypeUtils.getOncoTreeSubtypesFromSource();

        // Save all subtypes
        tumorTypes.forEach(tumorType -> ApplicationContextSingleton.getTumorTypeBo().save(tumorType));

        // Save all mainType
        tumorTypes.stream().map(mainType -> {
            TumorType tumorType = new TumorType();
            tumorType.setMainType(mainType.getMainType());
            return tumorType;
        }).forEach(tumorType -> ApplicationContextSingleton.getTumorTypeBo().save(tumorType));

        // save all special types
        TumorTypeUtils.getAllSpecialTumorOncoTreeTypes().forEach(tumorType -> ApplicationContextSingleton.getTumorTypeBo().save(tumorType));

        // Set the tumor type child and parent
        Map<String, org.mskcc.oncotree.model.TumorType> tumorTypeMap = TumorTypeUtils.getAllNestedOncoTreeSubtypesFromSource();
        org.mskcc.oncotree.model.TumorType tissue = tumorTypeMap.get("TISSUE");
        saveAndIterate(tissue);

    }

    private static void saveAndIterate(org.mskcc.oncotree.model.TumorType tumorType) {
        TumorTypeBo tumorTypeBo = ApplicationContextSingleton.getTumorTypeBo();
        if (StringUtils.isNotEmpty(tumorType.getCode())) {
            TumorType matchedTumorType = tumorTypeBo.findTumorTypeByCode(tumorType.getCode());
            TumorType matchedParentTumorType = tumorTypeBo.findTumorTypeByCode(tumorType.getParent());
            if (matchedTumorType != null) {
                matchedTumorType.setParent(matchedParentTumorType);
                Set<TumorType> childTumorTypes = new HashSet<>();
                tumorType.getChildren().values().forEach(child -> {
                    TumorType matchedChild = tumorTypeBo.findTumorTypeByCode(child.getCode());
                    if (matchedChild != null) {
                        childTumorTypes.add(matchedChild);
                    }
                });
                matchedTumorType.setChildren(childTumorTypes);
                tumorTypeBo.update(matchedTumorType);
            }
        }
        tumorType.getChildren().values().forEach(TumorTypeImporter::saveAndIterate);
    }
}
