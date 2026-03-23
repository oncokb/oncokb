package org.mskcc.cbio.oncokb.bo;

import java.util.List;

import org.mskcc.cbio.oncokb.model.TumorTypeSynonym;

public interface TumorTypeSynonymBo extends GenericBo<TumorTypeSynonym> {
    List<TumorTypeSynonym> findByName(String name);
}