
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.TumorType;

public interface TumorTypeBo extends GenericBo<TumorType> {
    TumorType findTumorTypeByCode(String code);
}
