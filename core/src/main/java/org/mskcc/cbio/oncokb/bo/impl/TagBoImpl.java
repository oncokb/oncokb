package org.mskcc.cbio.oncokb.bo.impl;

import java.util.List;

import org.mskcc.cbio.oncokb.bo.TagBo;
import org.mskcc.cbio.oncokb.dao.TagDao;
import org.mskcc.cbio.oncokb.model.Tag;

public class TagBoImpl extends GenericBoImpl<Tag, TagDao> implements TagBo  {
    @Override
    public List<Tag> findTagsByEntrezGeneId(int entrezGeneId) {
        return getDao().findTagsByEntrezGeneId(entrezGeneId);
    }
}
