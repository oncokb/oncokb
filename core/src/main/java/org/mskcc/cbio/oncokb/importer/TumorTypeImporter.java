package org.mskcc.cbio.oncokb.importer;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.model.TumorForm;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TumorTypeImporter {
    private TumorTypeImporter() {
        throw new AssertionError();
    }

    public static void main(String[] args) {
        List<TumorType> tumorTypes = TumorTypeUtils.getOncoTreeSubtypesFromSource();

        // Save all subtypes
        tumorTypes.forEach(tumorType -> ApplicationContextSingleton.getTumorTypeBo().save(tumorType));

        // Save all mainType
        tumorTypes.stream()
            .map(mainType -> {
                TumorType tumorType = new TumorType();
                tumorType.setName(mainType.getMainType());
                return tumorType;
            })
            .distinct()
            .filter(mainType -> StringUtils.isNotEmpty(mainType.getName()))
            .forEach(mainType -> {
                Set<TumorType> tumorTypesWithSameMainType = tumorTypes.stream().filter(tumorType -> StringUtils.isNotEmpty(tumorType.getMainType()) && tumorType.getMainType().equals(mainType.getName())).collect(Collectors.toSet());

                Set<TumorForm> tumorForms = tumorTypesWithSameMainType.stream().map(tumorType -> tumorType.getTumorForm()).collect(Collectors.toSet());
                if (tumorForms.size() > 0) {
                    if (tumorForms.size() == 1) {
                        mainType.setTumorForm(tumorForms.iterator().next());
                    } else {
                        mainType.setTumorForm(TumorForm.MIXED);
                    }
                }

                Set<String> tissues = tumorTypesWithSameMainType.stream().map(tumorType -> tumorType.getTissue()).collect(Collectors.toSet());
                if (tissues.size() > 0) {
                    if (tissues.size() == 1) {
                        mainType.setTissue(tissues.iterator().next());
                    } else {
                        mainType.setTissue(TumorForm.MIXED.name());
                    }
                }

                Set<String> colors = tumorTypesWithSameMainType.stream().map(tumorType -> tumorType.getColor()).collect(Collectors.toSet());
                if (colors.size() == 1) {
                    mainType.setColor(colors.iterator().next());
                }

                mainType.setLevel(0);
                ApplicationContextSingleton.getTumorTypeBo().save(mainType);
            });

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
