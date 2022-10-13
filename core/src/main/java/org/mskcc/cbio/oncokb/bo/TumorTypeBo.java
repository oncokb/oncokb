
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TumorTypeBo extends GenericBo<TumorType> {

    List<TumorType> getAllMainTypes();

    List<TumorType> getAllSpecialTumorOncoTreeTypes();

    List<TumorType> getAllSubtypes();

    List<TumorType> getAllSubtypesByMainType(String mainType);

    List<TumorType> getAllTumorTypes();

    TumorType getByCode(String code);

    TumorType getByMainType(String mainType);

    TumorType getByName(String name);

    TumorType getBySpecialTumor(SpecialTumorType specialTumorType);

    TumorType getBySubtype(String subtype);

    SpecialTumorType getSpecialTumorTypeByName(String name);
}
