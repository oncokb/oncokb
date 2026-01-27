package org.mskcc.cbio.oncokb.bo;

import java.util.List;

import org.mskcc.cbio.oncokb.model.Tag;

public interface TagBo extends GenericBo<Tag> {
    List<Tag> findTagsByEntrezGeneId(int entrezGeneId);
}
