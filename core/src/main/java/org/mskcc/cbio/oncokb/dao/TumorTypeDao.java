package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.List;

public interface TumorTypeDao extends GenericDao<TumorType, Integer> {
    TumorType findTumorTypeByCode(String code);
}
