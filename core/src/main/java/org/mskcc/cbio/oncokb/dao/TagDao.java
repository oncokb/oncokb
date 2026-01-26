package org.mskcc.cbio.oncokb.dao;

import java.util.List;

import org.mskcc.cbio.oncokb.model.Tag;

public interface TagDao extends GenericDao<Tag, Integer> {
    List<Tag> findTagsByEntrezGeneId(int entrezGeneId);
}
