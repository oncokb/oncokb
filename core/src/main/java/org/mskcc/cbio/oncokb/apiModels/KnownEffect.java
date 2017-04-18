package org.mskcc.cbio.oncokb.apiModels;

import java.util.Date;

/**
 * Created by Hongxin on 4/12/17.
 */
public class KnownEffect {
    private String knownEffect;
    private References references;
    private String description;
    private Date lastUpdate;

    public KnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
    }

    public KnownEffect(String knownEffect, References references, String description, Date lastUpdate) {
        this.knownEffect = knownEffect;
        this.references = references;
        this.description = description;
        this.lastUpdate = lastUpdate;
    }

    public String getKnownEffect() {
        return knownEffect;
    }

    public void setKnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
    }

    public References getReferences() {
        return references;
    }

    public void setReferences(References references) {
        this.references = references;
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
