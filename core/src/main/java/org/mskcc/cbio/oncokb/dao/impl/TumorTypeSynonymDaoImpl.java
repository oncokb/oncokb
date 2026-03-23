package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;

import org.mskcc.cbio.oncokb.dao.TumorTypeSynonymDao;
import org.mskcc.cbio.oncokb.model.TumorTypeSynonym;

public class TumorTypeSynonymDaoImpl extends GenericDaoImpl<TumorTypeSynonym, Integer> implements TumorTypeSynonymDao {

    @Override
    public List<TumorTypeSynonym> findByName(String name) {
        List<TumorTypeSynonym> synoynms = findByNamedQuery("findByCode", name.toUpperCase());
        if (synoynms.size() > 0) {
            return synoynms;
        }

        synoynms = findByNamedQuery("findByMainType", name.toLowerCase());
        if (synoynms.size() > 0) {
            return synoynms;
        }

        return findByNamedQuery("findBySubtype", name.toLowerCase());
    }
    
}