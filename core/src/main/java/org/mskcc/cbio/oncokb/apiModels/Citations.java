package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.ArticleAbstract;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2/21/18.
 */
public class Citations implements Serializable {
    Set<String> pmids = new HashSet<String>(0);
    Set<ArticleAbstract> abstracts = new HashSet<ArticleAbstract>(0);

    public Set<String> getPmids() {
        return pmids;
    }

    public void setPmids(Set<String> pmids) {
        this.pmids = pmids;
    }

    public Set<ArticleAbstract> getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(Set<ArticleAbstract> abstracts) {
        this.abstracts = abstracts;
    }
}
