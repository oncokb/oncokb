package org.mskcc.cbio.oncokb.apiModels;

import java.util.Date;
import java.util.List;

/**
 * Created by Hongxin on 4/12/17.
 */
public class KnownEffect {
    private String knownEffect;
    private List<String> pmids;
    private List<Abstract> abstracts;
    private String description;
    private Date lastUpdate;

    public KnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
    }

    public KnownEffect(String knownEffect, List<String> pmids, List<Abstract> abstracts, String description, Date lastUpdate) {
        this.knownEffect = knownEffect;
        this.pmids = pmids;
        this.abstracts = abstracts;
        this.description = description;
        this.lastUpdate = lastUpdate;
    }

    public String getKnownEffect() {
        return knownEffect;
    }

    public void setKnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
    }

    public List<String> getPmids() {
        return pmids;
    }

    public void setPmids(List<String> pmids) {
        this.pmids = pmids;
    }

    public List<Abstract> getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(List<Abstract> abstracts) {
        this.abstracts = abstracts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
