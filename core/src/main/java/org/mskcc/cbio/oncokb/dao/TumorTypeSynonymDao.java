package org.mskcc.cbio.oncokb.dao;

import java.util.List;

import org.mskcc.cbio.oncokb.model.TumorTypeSynonym;

public interface TumorTypeSynonymDao extends GenericDao<TumorTypeSynonym, Integer> {
    List<TumorTypeSynonym> findByName(String name);
}