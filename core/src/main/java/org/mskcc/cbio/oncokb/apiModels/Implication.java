package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.ArticleAbstract;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.apiModels.TumorType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2019-05-29.
 */
public class Implication implements Serializable {
    LevelOfEvidence levelOfEvidence;
    Set<String> alterations = new HashSet<>();
    TumorType tumorType;
    private Set<String> pmids = new HashSet<String>(0);
    private Set<ArticleAbstract> abstracts = new HashSet<ArticleAbstract>(0);
    String description = "";

    public LevelOfEvidence getLevelOfEvidence() {
        return levelOfEvidence;
    }

    public void setLevelOfEvidence(LevelOfEvidence levelOfEvidence) {
        this.levelOfEvidence = levelOfEvidence;
    }

    public Set<String> getAlterations() {
        return alterations;
    }

    public void setAlterations(Set<String> alterations) {
        this.alterations = alterations;
    }

    public TumorType getTumorType() {
        return tumorType;
    }

    public void setTumorType(TumorType tumorType) {
        this.tumorType = tumorType;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
