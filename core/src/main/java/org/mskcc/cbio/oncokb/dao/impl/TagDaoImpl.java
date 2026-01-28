package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;

import org.mskcc.cbio.oncokb.dao.TagDao;
import org.mskcc.cbio.oncokb.model.Tag;

public class TagDaoImpl extends GenericDaoImpl<Tag, Integer> implements TagDao  {
    @Override
    public List<Tag> findTagsByEntrezGeneId(int entrezGeneId) {
        return findByNamedQuery("findTagsByEntrezGeneId", entrezGeneId);
    }
}
