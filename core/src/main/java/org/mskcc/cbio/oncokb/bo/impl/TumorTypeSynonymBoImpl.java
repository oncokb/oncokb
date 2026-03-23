package org.mskcc.cbio.oncokb.bo.impl;

import java.util.List;

import org.mskcc.cbio.oncokb.bo.TumorTypeSynonymBo;
import org.mskcc.cbio.oncokb.dao.TumorTypeSynonymDao;
import org.mskcc.cbio.oncokb.model.TumorTypeSynonym;

public class TumorTypeSynonymBoImpl extends GenericBoImpl<TumorTypeSynonym, TumorTypeSynonymDao> implements TumorTypeSynonymBo {
    @Override
    public List<TumorTypeSynonym> findByName(String name) {
        return getDao().findByName(name);
    }
    
}
