package org.mskcc.cbio.oncokb.apiModels;

import java.io.Serializable;

/**
 * Created by Hongxin Zhang on 2/21/18.
 */
public class MutationEffectResp implements Serializable {
    String knownEffect = "";
    String description = "";
    Citations citations = new Citations();

    public String getKnownEffect() {
        return knownEffect;
    }

    public void setKnownEffect(String knownEffect) {
        this.knownEffect = knownEffect;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Citations getCitations() {
        return citations;
    }

    public void setCitations(Citations citations) {
        this.citations = citations;
    }
}
