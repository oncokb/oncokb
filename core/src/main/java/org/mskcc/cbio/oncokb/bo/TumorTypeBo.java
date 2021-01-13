
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.List;

public interface TumorTypeBo extends GenericBo<TumorType> {
    TumorType findTumorTypeByCode(String code);
    List<TumorType> findAllCached();
}
