package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;

import org.mskcc.cbio.oncokb.dao.TagDao;
import org.mskcc.cbio.oncokb.model.Tag;

public class TagDaoImpl extends GenericDaoImpl<Tag, Integer> implements TagDao  {
    @Override
    public List<Tag> findTagsByEntrezGeneId(int entrezGeneId) {
        return findByNamedQuery("findTagsByEntrezGeneId", entrezGeneId);
    }

    @Override
    public Tag findTagByHugoSymbolAndName(String hugoSymbol, String name) throws Exception {
        List<Tag> tags = findByNamedQuery("findTagsByHugoSymbolAndName", hugoSymbol, name);
        if (tags.isEmpty()) {
            throw new Exception("No tag with hugo symbol " + hugoSymbol + " and name " + name);
        }
        if (tags.size() != 1) {
            throw new Exception("Duplicate tags encountered with hugo symbol " + hugoSymbol + " and name " + name);
        }
        return tags.get(0);
    }
}
