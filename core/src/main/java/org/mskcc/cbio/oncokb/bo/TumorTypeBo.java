
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.List;
import java.util.Set;

public interface TumorTypeBo extends GenericBo<TumorType> {
    TumorType findTumorTypeByCode(String code);
    List<TumorType> findAllCached();
    Set<TumorType> findAllSpecialCancerTypesCached();
}
