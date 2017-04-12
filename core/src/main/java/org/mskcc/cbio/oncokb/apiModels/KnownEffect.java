package org.mskcc.cbio.oncokb.apiModels;

import java.util.Date;
import java.util.List;

/**
 * Created by Hongxin on 4/12/17.
 */
public class KnownEffect {
    private String knownEffect;
    private List<String> pmids;
    private String description;
    private Date lastUpdate;

    public KnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
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
